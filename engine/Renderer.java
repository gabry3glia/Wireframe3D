package engine;

import toolbox.math.Vector2;
import toolbox.math.Vector3;

import java.util.Vector;

import toolbox.gfx.Color;
import toolbox.gfx.Screen;

public class Renderer {

    // screen painter
    private Screen screen;
    // rendering parameters
    private static final int ORTHOGRAPHIC = 0, PERSPECTIVE = 1;
    private int projectionMode = PERSPECTIVE;
    private int width, height;
    private float near, far;
    private float fov, aspectRatio;

    public static final int BACK_FACE = 0, FRONT_FACE = 1;
    private boolean cull_face = true;
    private int cull_target = BACK_FACE;

    private Vector3[] localVertices;
    private Vector3[] worldSpaceVertices;
    private Vector3[] viewSpaceVertices;
    private Vector3[] ndcVertices; // normalized device coordinates
    
    private int[] indices;
    private int faceVertexCount;

    private Vector3 previousNDCVertex;
    private Vector3 currentNDCVertex;
    private Vector2 currentScreenSpaceVertex;
    private Vector2 previousScreenSpaceVertex;

    public Renderer(Screen screen) {
        this.screen = screen;

        width = screen.getWidth();
        height = screen.getHeight();
        near = 0.01f;
        far = 100.0f;
        fov = 70.0f;
        aspectRatio = (float) width / (float) height;
    }

    // orthographic projection mode setup
    public void orthographic(int width, int height, float near, float far) {
        projectionMode = ORTHOGRAPHIC;

        this.width = width;
        this.height = height;
        this.near = near;
        this.far = far;
        this.aspectRatio = (float) width / (float) height;
    }

    // perspective projection mode setup
    public void perspective(int width, int height, float near, float far, float fov) {
        projectionMode = PERSPECTIVE;

        this.width = width;
        this.height = height;
        this.near = near;
        this.far = far;
        this.fov = (float) Math.toRadians(fov);
        this.aspectRatio = (float) width / (float) height;
    }

    public void disableFaceCulling() {
        cull_face = false;
    }
    
    public void enableFaceCulling(int cull_target) {
        cull_face = true;
        this.cull_target = cull_target;
    }

    public void drawMesh(Mesh mesh, Transform transform, Camera camera) {
        localVertices = mesh.getVertices();
        worldSpaceVertices = transform.applyTo(localVertices);
        viewSpaceVertices = camera.view(worldSpaceVertices);
        ndcVertices = projectToNDC(viewSpaceVertices);

        int prevIndex, currIndex;

        int i = 0;
        int j = 0;
        int k = 0;
        for (i = 0; i < mesh.getFaceCount(); i++) {
            indices = mesh.getFace(i);
            
            // test for face culling
            if (cull_face) {
                // for each face we retrieve the face normal
                Vector3 face_normal = mesh.getNormal(i);
                Vector3[] viewSpaceFaceVertices = new Vector3[indices.length];
                for (k = 0; k < viewSpaceFaceVertices.length; k++) {
                    viewSpaceFaceVertices[k] = viewSpaceVertices[indices[k]];
                }
                // if the face has to be culled then continue
                if (isFaceToCull(face_normal, viewSpaceFaceVertices, transform, camera)) continue;
            }

            // * TODO: performance: save the already intersected and projected vertices into an array to memoize instead of recalculating them every time

            faceVertexCount = indices.length;
            for (j = 1; j < faceVertexCount; j++) {
                prevIndex = indices[j - 1];
                currIndex = indices[j];

                previousNDCVertex = ndcVertices[prevIndex];
                currentNDCVertex = ndcVertices[currIndex];
                
                // frustum line clipping checks and intersection
                boolean shouldRenderLine = doesLineIntersectFrustum(previousNDCVertex, currentNDCVertex);
                if (shouldRenderLine) {
                    Vector3[] ndcIntersectedVertices = intersectWithNearOrFarPlains(
                        // previous vertex in camera view space
                        viewSpaceVertices[prevIndex],
                        // current vertex in camera view space
                        viewSpaceVertices[indices[j]],
                        camera
                    );

                    previousNDCVertex = ndcIntersectedVertices[0];
                    currentNDCVertex = ndcIntersectedVertices[1];
                    
                    previousScreenSpaceVertex = projectToScreenSpace(previousNDCVertex);
                    currentScreenSpaceVertex = projectToScreenSpace(currentNDCVertex);
                    
                    // connect the current and previous vertices
                    screen.line(
                        (int) previousScreenSpaceVertex.getX(),
                        (int) previousScreenSpaceVertex.getY(),
                        (int) currentScreenSpaceVertex.getX(),
                        (int) currentScreenSpaceVertex.getY(),
                        Color.BLACK
                    );
                }
            }
            // if (faceVertexCount == 3) {
            //     Vector2 v0 = projectToScreenSpace(ndcVertices[0]);
            //     Vector2 v1 = projectToScreenSpace(ndcVertices[1]);
            //     Vector2 v2 = projectToScreenSpace(ndcVertices[2]);
            //     screen.fill(Color.RED);
            //     screen.triangle(v0, v1, v2);
            // }
            // connect the first and last vertices to close the shape (only if there are more than 2 verices, otherwise that's already a line)
            // don't render the line if it gets clipped away by the current view frustum
            if (faceVertexCount > 2) {
                prevIndex = indices[0];
                currIndex = indices[faceVertexCount - 1];

                previousNDCVertex = ndcVertices[prevIndex];
                currentNDCVertex = ndcVertices[currIndex];

                // frustum line clipping checks and intersection
                boolean shouldRenderLine = doesLineIntersectFrustum(previousNDCVertex, currentNDCVertex);
                if (shouldRenderLine) {
                    Vector3[] ndcIntersectedVertices = intersectWithNearOrFarPlains(
                        // previous vertex in camera view space
                        viewSpaceVertices[prevIndex],
                        // current vertex in camera view space
                        viewSpaceVertices[currIndex],
                        camera
                    );

                    previousNDCVertex = ndcIntersectedVertices[0];
                    currentNDCVertex = ndcIntersectedVertices[1];
                 
                    previousScreenSpaceVertex = projectToScreenSpace(previousNDCVertex);
                    currentScreenSpaceVertex = projectToScreenSpace(currentNDCVertex);
                    screen.line(
                        (int) previousScreenSpaceVertex.getX(),
                        (int) previousScreenSpaceVertex.getY(),
                        (int) currentScreenSpaceVertex.getX(),
                        (int) currentScreenSpaceVertex.getY(),
                        Color.BLACK
                    );
                }
            }
        }
    }

    /** Projects the given vertices in camera view space to normalized device coordinates
     * (all components ranged [-1, 1] (or [0, 1] for z) inside frustum space) **/
    private Vector3[] projectToNDC(Vector3[] viewSpaceVertices) {
        int vertexCount = viewSpaceVertices.length;
        float x, y, z;

        float nearDist, frustumDepth;
        float projX, projY;

        float ndcX = 0.0f, ndcY = 0.0f, ndcZ = 0.0f;

        float halfViewportWidth, halfViewportHeight;

        Vector3[] projVerts = new Vector3[vertexCount];

        int i = 0;
        for (i = 0; i < vertexCount; i++) {
            x = viewSpaceVertices[i].getX();
            y = viewSpaceVertices[i].getY();
            z = -viewSpaceVertices[i].getZ(); // reversed because positive z in behind the camera and we want positive z-distance from the near and far plains

            nearDist = z - near; // -z is the z distance form the camera, we need the distance from the near plane
            frustumDepth = far - near;
            ndcZ = nearDist / frustumDepth; // 0.0 is at near, 1.0 is at far

            switch(projectionMode) {
                // orthographic projection
                case ORTHOGRAPHIC:
                    ndcX = x / ((float) width / 2);
                    ndcY = y / ((float) height / 2);
                    break;
                // perspective projection
                case PERSPECTIVE:
                    // since everything revolves around the camera,
                    // the camera is now the origin meaning the vertex x, y and z
                    // are the component distances from the camera

                    // if (z < near) {
                    //     z *= -1; // project as if it was in front of the near plane but keep its normalized z distance
                    // }
                    // if (z == 0) {
                    //     z += 0.001; // !!! I DON'T REALLY LIKE THIS BUT IT WORKS...
                    // }

                    projX = x * near / z;
                    projY = y * near / z;

                    halfViewportWidth = (float) (near * Math.tan(fov / 2));
                    halfViewportHeight = halfViewportWidth / aspectRatio;

                    // to normalized device coordinates
                    ndcX = projX / halfViewportWidth;
                    ndcY = projY / halfViewportHeight;
                    break;
            }
            projVerts[i] = new Vector3(ndcX, ndcY, ndcZ);
        }

        return projVerts;
    }

    /** Converts normalized device coordinates to screen space coordinates
     * (the 2D positions to actually be rendered on screen) **/
    private Vector2 projectToScreenSpace(Vector3 ndcVertex) {
        // convert to canvas coordinates from normalized
        float screenX = ndcVertex.getX() * (float) width / 2;
        float screenY = ndcVertex.getY() * (float) height / 2;
        
        // make (0, 0) the center of the screen
        screenX += (float) width / 2;
        screenY += (float) height / 2;

        // vertical flip to be y-up
        // screenY = height - screenY;
        
        return new Vector2(screenX, screenY);
    }

    private boolean[] test_frustum_clipping(Vector3 ndcVertex) {
        // returns whether the given point is inside the currently defined frustum or not
        // the third value is the normalized z distance [0.0, 1.0] where 0.0 is at _near and 1.0 is at _far
        // works for both orthographic and perspective projection

        // the given point is inside the current frustum if it gets projected
        // inside the canvas and the normalized_z_distance is between 0.0 and 1.0
        // (including 0.0 and 1.0)

        // returns the boolean dictionary
        // {
        //     0: inside_frustum      True when the vertex is inside the current frustum
        //     1: inside_canvas       True when the vertex is projected inside the canvas boundries
        //     2: inside_x            True when the vertex is projected inside the horizontal boundries (-1.0 <= ndc_x <= 1.0)
        //     3: inside_y            True when the vertex is projected inside the vertical boundries (-1.0 <= ndc_y <= 1.0)
        //     4: inside_z            True when the vertex is projected inside the depth boundries (0.0 <= normalized_z_dist <= 1.0)
        //     5: too_left            True when the vertex is projected outside on the left side of the canvas (ndc_x < -1.0)
        //     6: too_right           True when the vertex is projected outside on the right side of the canvas (ndc_x > 1.0)
        //     7: too_high            True when the vertex is projected outside on the top side of the canvas (ndc_y > 1.0)
        //     8: too_low             True when the vertex is projected outside on the bottom side of the canvas (ndc_y < -1.0)
        //     9: too_close           True when the vertex is behind the near plane (normalized_z_dist < -1.0)
        //     10: too_far            True when the vertex is over the far plane (normalized_z_dist > 1.0)
        // }

        boolean too_left = ndcVertex.getX() < -1.0;
        boolean too_right = ndcVertex.getX() > 1.0;
        boolean too_high = ndcVertex.getY() > 1.0;
        boolean too_low = ndcVertex.getY() < -1.0;
        boolean too_close = ndcVertex.getZ() < 0.0;
        boolean too_far = ndcVertex.getZ() > 1.0;
        boolean inside_x = !too_left && !too_right;
        boolean inside_y = !too_high && !too_low;
        boolean inside_z = !too_close && !too_far;
        boolean inside_canvas = inside_x && inside_y;
        boolean inside_frustum = inside_x && inside_y && inside_z;

        return new boolean[] {
            inside_frustum, // inside_frustum
            inside_canvas, // inside_canvas
            inside_x, // inside_x
            inside_y, // inside_y
            inside_z, // inside_z
            too_left, // too_left
            too_right, // too_right
            too_high, // too_high
            too_low, // too_low
            too_close, // too_close
            too_far // too_far
        };
    }

    private boolean doesLineIntersectFrustum(Vector3 ndcV0, Vector3 ndcV1) {
        // 3D line frustum clipping test function
        // returns True if the line between the given NORMALIZED DEVICE COORDINATE VERTICES can be drawn
        // a line can be drawn if the two vertices are NOT clipped by the SAME plane
        // if they are clipped by different planes the line will traverse the frustum and thus it will be rendered
        // this includes the case scenario where the near plane cuts the line,
        // which has to be handled using the "intersect_line_with_near_or_far_plane()" function
        
        boolean[] v0_clip_result = test_frustum_clipping(ndcV0);
        boolean v0_too_left = v0_clip_result[5];
        boolean v0_too_right = v0_clip_result[6];
        boolean v0_too_high = v0_clip_result[7];
        boolean v0_too_low = v0_clip_result[8];
        boolean v0_too_close = v0_clip_result[9];
        boolean v0_too_far = v0_clip_result[10];
        
        boolean[] v1_clip_result = test_frustum_clipping(ndcV1);
        boolean v1_too_left = v1_clip_result[5];
        boolean v1_too_right = v1_clip_result[6];
        boolean v1_too_high = v1_clip_result[7];
        boolean v1_too_low = v1_clip_result[8];
        boolean v1_too_close = v1_clip_result[9];
        boolean v1_too_far = v1_clip_result[10];

        boolean both_too_left = v0_too_left && v1_too_left;
        boolean both_too_right = v0_too_right && v1_too_right;
        boolean both_too_high = v0_too_high && v1_too_high;
        boolean both_too_low = v0_too_low && v1_too_low;
        boolean both_too_close = v0_too_close && v1_too_close;
        boolean both_too_far = v0_too_far && v1_too_far;
        
        boolean both_clipped_by_the_same_x_plane = both_too_left || both_too_right;
        boolean both_clipped_by_the_same_y_plane = both_too_high || both_too_low;
        boolean both_clipped_by_the_same_z_plane = both_too_close || both_too_far;
        
        return !both_clipped_by_the_same_x_plane && !both_clipped_by_the_same_y_plane && !both_clipped_by_the_same_z_plane;
    }

    private Vector3[] intersectWithNearOrFarPlains(Vector3 vsV0, Vector3 vsV1, Camera camera) {
        Vector3 viewSpaceV0 = vsV0.copy();
        Vector3 viewSpaceV1 = vsV1.copy();

        Vector3[] ndcs = projectToNDC(new Vector3[] { viewSpaceV0, viewSpaceV1 });
        Vector3 ndcV0 = ndcs[0];
        Vector3 ndcV1 = ndcs[1];
        
        float plain = 0;
        boolean reproject;
        
        // figure out if reprojection is needed and on which plain
        reproject = true;
        // System.out.println(ndcV0.getZ());
        if (ndcV0.getZ() < 0) plain = near;
        else if (ndcV0.getZ() > 1) plain = far;
        else reproject = false;
        if (reproject) {
            float x0 = viewSpaceV0.getX();
            float y0 = viewSpaceV0.getY();
            float z0 = -viewSpaceV0.getZ();

            float x1 = viewSpaceV1.getX();
            float y1 = viewSpaceV1.getY();
            float z1 = -viewSpaceV1.getZ();

            // System.out.printf("%.2f, (%.2f, %.2f)\n", z0, near, far);

            viewSpaceV0.setX(x0 + ((x1 - x0) / (z1 - z0) * (plain - z0)));
            viewSpaceV0.setY(y0 + ((y1 - y0) / (z1 - z0) * (plain - z0)));
            viewSpaceV0.setZ(-plain);

            // viewSpaceV0.setX(viewSpaceV1.getX() / viewSpaceV1.getZ() * plain);
            // viewSpaceV0.setY(viewSpaceV1.getY() / viewSpaceV1.getZ() * plain);
        }
        // figure out if reprojection is needed and on which plain
        reproject = true;
        if (ndcV1.getZ() < 0) plain = near;
        else if (ndcV1.getZ() > 1) plain = far;
        else reproject = false;
        if (reproject) {
            float x0 = viewSpaceV1.getX();
            float y0 = viewSpaceV1.getY();
            float z0 = -viewSpaceV1.getZ();
            
            float x1 = viewSpaceV0.getX();
            float y1 = viewSpaceV0.getY();
            float z1 = -viewSpaceV0.getZ();

            viewSpaceV1.setX(x0 + ((x1 - x0) / (z1 - z0) * (plain - z0)));
            viewSpaceV1.setY(y0 + ((y1 - y0) / (z1 - z0) * (plain - z0)));
            viewSpaceV1.setZ(-plain);

            // viewSpaceV1.setX(viewSpaceV0.getX() / viewSpaceV0.getZ() * plain);
            // viewSpaceV1.setY(viewSpaceV0.getY() / viewSpaceV0.getZ() * plain);
        }

        return projectToNDC(new Vector3[] { viewSpaceV0, viewSpaceV1 });
    }

    private void draw_point(Vector3 p, Camera camera, Color color, int radius, boolean checkFrustumClipping) {
        // renders a point in 3D space
        
        // we get the world space point coordinates, so we need to transform them to camera space
        // then project them and take the screen space coordinates to render it
        Vector3[] camera_space_vector = camera.view(new Vector3[] { p });

        Vector3 projected_vector = projectToNDC(camera_space_vector)[0];

        // don't render the point if it gets clipped away by the current view frustum
        if (checkFrustumClipping && !test_frustum_clipping(projected_vector)[0]) return;
        
        // if the point is visible we render it
        Vector2 screen_space_coordinates = projectToScreenSpace(projected_vector);
        screen.point(
            (int) screen_space_coordinates.getX(),
            (int) screen_space_coordinates.getY(),
            radius, color
        );
    }

    private void draw_line(Vector3 p0, Vector3 p1, Camera camera, Color color, int stroke, boolean checkFrustumClipping) {
        // renders a line in 3D space
        
        // we get the world space point coordinates, so we need to transform them to camera space
        // then project them and take the screen space coordinates to render it
        Vector3[] pp = camera.view(new Vector3[] { p0, p1 });
        Vector3 camera_space_p0 = pp[0];
        Vector3 camera_space_p1 = pp[1];

        pp = projectToNDC(new Vector3[]{ camera_space_p0, camera_space_p1 });
        Vector3 projected_p0 = pp[0];
        Vector3 projected_p1 = pp[1];

        if (checkFrustumClipping) {
            // don't render the line if it gets clipped away by the current view frustum
            if (!doesLineIntersectFrustum(projected_p0, projected_p1)) {
                return;
            }
            
            // before rendering the line we check if the line is cut by the near or far planes
            // and get the line repaired by intersecting it with said planes
            pp = intersectWithNearOrFarPlains(
                camera_space_p0, camera_space_p1, camera
            );
        }
        
        // if the line is visible we render it
        Vector2 screen_space_p0 = projectToScreenSpace(pp[0]);
        Vector2 screen_space_p1 = projectToScreenSpace(pp[1]);
        
        screen.stroke(stroke);
        screen.line(
            (int) screen_space_p0.getX(), (int) screen_space_p0.getY(),
            (int) screen_space_p1.getX(), (int) screen_space_p1.getY(),
            color
        );
        screen.stroke(1);
    }
    
    public void gizmos(Camera camera) {
        // GIZMOS
        Vector2 center = projectToScreenSpace(new Vector3(0, 0, 0));
        // start position in world space (5 units in front of the camera)
        Vector3 start = Vector3.sum(camera.position, Vector3.scale(camera.forward, 5));
        // vector tips positions in world space
        Vector3 x = Vector3.sum(start, new Vector3(0.5f, 0, 0));
        Vector3 y = Vector3.sum(start, new Vector3(0, 0.5f, 0));
        Vector3 z = Vector3.sum(start, new Vector3(0, 0, 0.5f));
        // vector lines
        draw_line(start, x, camera, Color.RED, 2, false);
        draw_line(start, y, camera, Color.GREEN, 2, false);
        draw_line(start, z, camera, Color.BLUE, 2, false);
        // gizmos center
        screen.point((int) center.getX(), (int) center.getY(), 4, Color.BLACK);
        screen.point((int) center.getX(), (int) center.getY(), 2, Color.WHITE);
        // vector tips
        draw_point(x, camera, Color.RED, 3, false);
        draw_point(y, camera, Color.GREEN, 3, false);
        draw_point(z, camera, Color.BLUE, 3, false);
        // Vector3[] tips = projectToNDC(camera.view(new Vector3[] { x, y, z }));
        // Vector2 x_tip = projectToScreenSpace(tips[0]);
        // Vector2 y_tip = projectToScreenSpace(tips[1]);
        // Vector2 z_tip = projectToScreenSpace(tips[2]);
        // write("+X", x_tip.x, x_tip.y, backgroud_color=Color.LIGHT_GRAY);
        // write("+Y", y_tip.x, y_tip.y, backgroud_color=Color.LIGHT_GRAY);
        // write("+Z", z_tip.x, z_tip.y, backgroud_color=Color.LIGHT_GRAY);
    }

    private boolean isFaceToCull(Vector3 face_normal, Vector3[] viewSpaceFaceVertices, Transform transform, Camera camera) {
        // returns True if the given face is to be culled or not
        // based on the current face culling options
        // (so True means you can render it, False means you should not render it)

        // face_normal is the face normal vector
        // face_vertices is the list of vertices defining the face IN CAMERA (VIEW) SPACE
        // transform is the model transform applied to the face vertices
        // camera is the camera the mesh is being viewed from

        // HOW THE FACE CULLING ALGORITHM WORKS:
        // the key idea is that we want to figure out whether the viewer is looking at a face or not
        // to understand it, we can check if at least one vertex amoung the ones that make the face
        // is visible from the camera point of view
        // to do this we need the current face normal and the vertex position
        
        // the vertex position must be expressed in camera space, so that negating that vertex position
        // gives us the distance vector between that vertex and the camera, pointing to the camera

        // the face normal vector must be expressed in camera space too
        // however, we only need to ROTATE it accordingly to the model rotation and the camera rotation
        // translating that vector too would make it point in the wrong direction since a normal is just
        // a direction vector, not a position vector, so we ONLY HAVE TO ROTATE IT, NOT TO TRANSLATE IT TOO
        
        if (!cull_face) return false;
        
        // normals should only be rotated but not translated
        // so we rotate the face normal by the given model transform rotation
        Vector3 viewSpaceNormal = face_normal.copy();
        viewSpaceNormal.rotate3D(new float[] {
            transform.rotation.getX(),
            transform.rotation.getY(),
            transform.rotation.getZ()
        });
        // then we rotate the world rotated face normal by the given camera rotation
        viewSpaceNormal.rotate3D(new float[] {
            camera.rotation.getX(),
            camera.rotation.getY(),
            camera.rotation.getZ()
        });

        boolean facing_camera = false;
        // HERE WE'RE ESSETIALLY JUST DOING THE DOT PRODUCT BETWEEN THE VERTEX POSITION AND THE FACE NORMAL IN CAMERA VIEW SPACE
        // THE VERTICES ARE TAKEN FROM THE FACE THAT IS BEING TESTED. EVEN ONE VISIBLE VERTEX MEANS THE FACE WON'T BE CULLED AWAY
        for (Vector3 viewSpaceVertexPosition : viewSpaceFaceVertices) {
            // for each vertex in the face we take the position in camera space
            // (so the vector is starting from the camera and pointing towards the vertex)
            // we normalize it just so that the dot similarity is a number between -1.0 and 1.0
            
            // we compute the dot product between the vertex pointing vector and the face normal
            // the dot product will be POSITIVE if the two vectors are POINTING TOWARDS THE SAME
            // SEMI-SPACE (camera NOT facing the face), 0 if they are ORTHOGONAL, NEGATIVE if they are POINTING TOWARDS THE OPPOSITE
            // SEMI-SPACES (camera facing the face)
            float dot_similarity = Vector3.dot(viewSpaceVertexPosition, viewSpaceNormal);
            
            // if the dot product is negative (so the camera facing the current face)
            // then we want to check that flag and exit the loop, because at least one
            // of the current face vertices is visible, so we must render the current face
            if (dot_similarity < 0) { // checking the opposite to avoid doing another operation in the loop (the other operation is doing negate() on the vertex_vector)
                facing_camera = true;
                break;
            }
        }

        // depending on the culling target we go on to render or just skip the rendering cycle
        // for the current face
        if (cull_target == BACK_FACE && !facing_camera) return true; // skip face rendering
        if (cull_target == FRONT_FACE && facing_camera) return true; // skip face rendering
        return false;
    }
}
