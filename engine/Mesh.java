package engine;

import java.util.ArrayList;

import toolbox.math.Maths;
import toolbox.math.Vector3;
import toolbox.utils.Console;

public class Mesh {
    
    private Vector3[] vertices;
    private int[][] faces;
    private Vector3[] normals;

    public Mesh(Vector3[] vertices, int[][] faces, Vector3[] normals) {
        this.vertices = vertices;
        this.faces = faces;
        this.normals = normals;
    }

    /** Automatic normal computing only works if the faces are all triangles
     * REMEMEBER TO ALWAYS GIVE THE TRIANGLE FACE VERTICES IN A COUNTER-CLOCKWISE ORDER FOR CORRECT NORMAL DIRECTION
     * (giving them in a clockwise order will result in flipped inwards normals, instead of the regular outwards normals) **/
    public Mesh(Vector3[] vertices, int[][] faces) {
        this.vertices = vertices;
        this.faces = faces;
        normals = calculateNormals(vertices, faces);
    }

    public Vector3[] getVertices() {
        return vertices;
    }

    public int[] getFace(int n) {
        return faces[n];
    }

    public Vector3 getNormal(int n) {
        return normals[n];
    }

    public int getFaceCount() {
        return faces.length;
    }

    public void add_face(int[] face, Vector3 normal) {
        int[][] newFaces = new int[faces.length + 1][];
        Vector3[] newNormals = new Vector3[normals.length + 1];

        int i;
        for (i = 0; i < newFaces.length; i++) {
            newFaces[i] = i < faces.length ? faces[i] : face;
        }
        for (i = 0; i < newNormals.length; i++) {
            newNormals[i] = i < normals.length ? normals[i] : normal;
        }

        faces = newFaces;
        normals = newNormals;
    }

    // SOME UTILITY METHODS
    private static Mesh bakeMesh(ArrayList<Vector3> verticesList, ArrayList<int[]> facesList, ArrayList<Vector3> normalsList) {
        Vector3[] vertices = new Vector3[verticesList.size()];
        int[][] faces = new int[facesList.size()][];
        Vector3[] normals = new Vector3[normalsList.size()];

        int i;
        for (i = 0; i < vertices.length; i++) {
            vertices[i] = verticesList.get(i);
        }
        for (i = 0; i < faces.length; i++) {
            faces[i] = facesList.get(i);
        }
        for (i = 0; i < normals.length; i++) {
            normals[i] = normalsList.get(i);
        }

        return new Mesh(vertices, faces, normals);
    }

    // Calculates the given triangle face normal based on its three vertices
    // THEY MUST BE GIVEN IN COUNTER-CLOCKWISE ORDER
    public static Vector3 calculateFaceNormal(Vector3 v0, Vector3 v1, Vector3 v2) {
        Vector3 v01 = Vector3.difference(v1, v0);
        Vector3 v02 = Vector3.difference(v2, v0);
        Vector3 normal = Vector3.cross(v01, v02);
        normal.normalize();
        return normal;
    }

    public static Vector3[] calculateNormals(Vector3[] vertices, int[][] faces) {
        Vector3[] normals = new Vector3[faces.length];
        
        int i;
        for (i = 0; i < normals.length; i++) {
            if (faces[i].length != 3) {
                Console.warning("Automatic normal computing only works with triangle faces!\nNormals won't be saved for this mesh");
                normals = null;
                return null;
            }
            Vector3 v0 = vertices[faces[i][0]].copy();
            Vector3 v1 = vertices[faces[i][1]].copy();
            Vector3 v2 = vertices[faces[i][2]];
            
            normals[i] = calculateFaceNormal(v0, v1, v2);
        }
        
        return normals;
    }
    
    // SOME FAST SHAPES MESHES
    public static Mesh triangle(float side) {
        // returns a plane equilateral triangle mesh
        double right_angle = Math.PI / 2;
        double hundred_twenty_angle = 2 * Math.PI / 3;
        Vector3[] vertices = new Vector3[3];
        float radius = (float) (side / Math.sqrt(3)); // I figured out the math on paper: brotrustme it works!!!
        int i;
        for (i = 0; i < vertices.length; i++) {
            float x = (float) (radius * Math.cos(-right_angle + i * hundred_twenty_angle));
            float z = (float) (radius * Math.sin(-right_angle + i * hundred_twenty_angle));
            vertices[i] = new Vector3(x, 0, z);
        }
        int[][] faces = new int[][] { { 0, 1, 2 } };
        Vector3[] normals = new Vector3[] { new Vector3(0, 1, 0) };
        return new Mesh(vertices, faces, normals);
    }

    public static Mesh plane(float width, float depth, int h_subdivisions, int d_subdivisions, boolean triangulate) {
        // returns a plane mesh
        h_subdivisions = Math.max(1, h_subdivisions);
        d_subdivisions = Math.max(1, d_subdivisions);

        ArrayList<Vector3> verticesList = new ArrayList<Vector3>();
        ArrayList<int[]> facesList = new ArrayList<int[]>();
        ArrayList<Vector3> normalsList = new ArrayList<Vector3>();

        float half_width = width / 2;
        float half_depth = depth / 2;
        float sub_width = width / h_subdivisions;
        float sub_depth = depth / d_subdivisions;
        // vertices are put in a row major order, where rows run along the x axis
        // and columns run along the z axis
        int i, j;
        for (j = 0; j < d_subdivisions + 1; j++) {
            for (i = 0; i < h_subdivisions + 1; i++) {
                float x = i * sub_width - half_width;
                float z = j * sub_depth - half_depth;
                verticesList.add(new Vector3(x, 0, z));
        
                if (i < h_subdivisions && j < d_subdivisions) {
                    int i0 = i + j * (h_subdivisions + 1);
                    int i1 = i0 + 1;
                    int i3 = i0 + h_subdivisions + 1;
                    int i2 = i3 + 1;

                    if (triangulate) {
                        facesList.add(new int[] { i0, i1, i2 });
                        facesList.add(new int[] { i0, i2, i3 });

                        normalsList.add(new Vector3(0, 1, 0));
                        normalsList.add(new Vector3(0, 1, 0));
                    } else {
                        facesList.add(new int[] { i0, i1, i2, i3 });
                        normalsList.add(new Vector3(0, 1, 0));
                    }
                }
            }
        }

        return bakeMesh(verticesList, facesList, normalsList);
    }

    public static Mesh box(float width, float height, float depth, boolean triangulate) {
        int i;

        float half_width = width / 2;
        float half_height = height / 2;
        float half_depth = depth / 2;
        Vector3[] vertices = new Vector3[]{
            new Vector3(-half_width, -half_height, half_depth),     // 0
            new Vector3(half_width, -half_height, half_depth),      // 1
            new Vector3(half_width, half_height, half_depth),       // 2
            new Vector3(-half_width, half_height, half_depth),      // 3
            new Vector3(-half_width, -half_height, -half_depth),    // 4
            new Vector3(half_width, -half_height, -half_depth),     // 5
            new Vector3(half_width, half_height, -half_depth),      // 6
            new Vector3(-half_width, half_height, -half_depth)      // 7
        };
        int[][] faces;
        if (triangulate) {
            faces = new int[][] {
                { 0, 1, 2 }, { 0, 2, 3 }, // front
                { 1, 5, 6 }, { 1, 6, 2 }, // right
                { 3, 2, 6 }, { 3, 6, 7 }, // top
                { 4, 5, 1 }, { 4, 1, 0 }, // bottom
                { 4, 0, 3 }, { 4, 3, 7 }, // left
                { 5, 4, 7 }, { 5, 7, 6 }  // back
            };
        } else {
            faces = new int[][] {
                new int[] { 0, 1, 2, 3 }, // front
                new int[] { 1, 5, 6, 2 }, // right
                new int[] { 3, 2, 6, 7 }, // top
                new int[] { 4, 5, 1, 0 }, // bottom
                new int[] { 4, 0, 3, 7 }, // left
                new int[] { 5, 4, 7, 6 }  // back
            };
        }

        ArrayList<Vector3> normalsList = new ArrayList<Vector3>();
        normalsList.add(new Vector3(0, 0, 1));   // front
        normalsList.add(new Vector3(1, 0, 0));   // right
        normalsList.add(new Vector3(0, 1, 0));   // top
        normalsList.add(new Vector3(0, -1, 0));      // bottom
        normalsList.add(new Vector3(-1, 0, 0));      // left
        normalsList.add(new Vector3(0, 0, -1));      // back
        if (triangulate) {
            normalsList.ensureCapacity(normalsList.size() * 2);
            // duplicate the normals because there will be double the faces
            for (i = 0; i < normalsList.size() / 2; i++) {
                normalsList.set(i*2 + 1, normalsList.get(i*2));
            }
        }

        Vector3[] normals = new Vector3[normalsList.size()];
        for (i = 0; i < normals.length; i++) {
            normals[i] = normalsList.get(i);
        }
        
        return new Mesh(vertices, faces, normals);
    }

    public static Mesh cube(float side, boolean triangulate) {
        // returns a cube mesh
        return box(side, side, side, triangulate);
    }

    public static Mesh cylinder(float radius, float height, int resolution, boolean triangulate) {
        // returns a cylinder mesh
        // THE LOGIC OF THE FOR LOOP
        // I hope this makes sense to you
        //   i-1
        //   /| \
        //  / |  \
        // i+1----i+3
        // |  |    |
        // | i-2   |
        // | /  \  |
        // |/    \ |
        // i------i+2

        // if there are only 3 vertices in the base, then the base is already a triangle
        if (resolution == 3) triangulate = false;

        ArrayList<Vector3> verticesList = new ArrayList<Vector3>();
        ArrayList<int[]> facesList = new ArrayList<int[]>();
        ArrayList<Vector3> normalsList = new ArrayList<Vector3>();
        
        float half_height = height / 2;
        int triangulate_addition = triangulate ? 2 : 0; // used for correcting the indices
        int num_cylinder_vertices = 2 * resolution + triangulate_addition;

        if (triangulate) {
            verticesList.add(new Vector3(0, -half_height, 0)); // base
            verticesList.add(new Vector3(0, half_height, 0)); // lid
        }

        // Note: in the loop there are some swapped indices when creating the faces
        // that's because python could be increasing the angles in a clockwise order...
        // I'm not really sure but it must be that, because otherwise
        // the faces and the normals would be correct without swapping
        // the order of the three vertices
        int i;
        for (i = 0; i < resolution; i++) {
            double theta = i * 2 * Math.PI / resolution;
            float x = (float) (radius * Math.cos(theta));
            float z = (float) (radius * Math.sin(theta));

            // we'll have this vertices list:
            // i-th index: the vertex of the base
            // (i+1)th index: the corresponding base vertex but in the lid
            // so the loop is run only once considering i to the base vertex
            verticesList.add(new Vector3(x, -half_height, z));
            verticesList.add(new Vector3(x, half_height, z));

            // figure out the indices (see the "drawing" below)
            int i0 = i * 2 + triangulate_addition; // because we have to skip the corresponding lid vertex at i+1
            int i1 = i0 + 1;
            int i2 = (i0 + 2) % num_cylinder_vertices; // this could need wrap-around logic
            int i3 = (i0 + 3) % num_cylinder_vertices; // this could need wrap-around logic
            if (i2 == 0) i2 += triangulate_addition;
            if (i3 == 1) i3 += triangulate_addition;

            // add the proper indices
            if (triangulate) {
                // build two triangles
                facesList.add(new int[] { i0, i1, i2 }); // first triangle
                facesList.add(new int[] { i2, i1, i3 }); // second triangle

                // add the base and lid triangle faces too
                facesList.add(new int[] { 0, i0, i2 }); // base triangle
                facesList.add(new int[] { 1, i3, i1 }); // lid triangle
            } else {
                // build a quad
                facesList.add(new int[] { i0, i2, i3, i1 });
            
                // manually calculate and add the normal vector of the previously generated face,
                // since we need the actual vectors we must wait for the next column to be generated
                if (i > 0) {
                    Vector3 v0 = verticesList.get(i0 - 1); // previous lid vertex
                    Vector3 v1 = verticesList.get(i0); // current base vertex
                    Vector3 v2 = verticesList.get(i1); // current lid vertex
                    Vector3 norm = calculateFaceNormal(v2, v1, v0); // they must be given in a counter-clockwise order
                    normalsList.add(norm);
                }
            }
        }

        // if not triangulating manually add the last face normal
        // because it was not calculated in the loop as there we calculate
        // the normal vector for the previously generated face
        if (!triangulate) {
            Vector3 v0 = verticesList.get(verticesList.size() - 1); // second-last lid vertex
            Vector3 v1 = verticesList.get(0); // last base vertex
            Vector3 v2 = verticesList.get(1); // last lid vertex
            Vector3 norm = calculateFaceNormal(v2, v1, v0); // they must be given in a counter-clockwise order
            normalsList.add(norm);
            if (triangulate) {
                normalsList.add(norm); // add a second normal vector for the second triangle
            }
        }

        // normals are manually calculated only for when not triangulating the cylinder mesh
        Mesh cylinder_mesh;

        if (!triangulate) {
            cylinder_mesh = bakeMesh(verticesList, facesList, normalsList);
        } else {
            Vector3[] vertices = new Vector3[verticesList.size()];
            int[][] faces = new int[facesList.size()][];

            for (i = 0; i < vertices.length; i++) {
                vertices[i] = verticesList.get(i);
            }
            for (i = 0; i < faces.length; i++) {
                faces[i] = facesList.get(i);
            }
            
            cylinder_mesh = new Mesh(vertices, faces);
        }

        // manually adding the base and lid faces
        if (!triangulate) {
            int[] base_face = Maths.range(0, 2 * resolution, 2);
            Vector3 base_normal = new Vector3(0, -1, 0);
            cylinder_mesh.add_face(base_face, base_normal); // base
            int[] lid_face = Maths.range(1, 2 * resolution, 2);
            Vector3 lid_normal = new Vector3(0, 1, 0);
            cylinder_mesh.add_face(lid_face, lid_normal); // lid
        }
        
        return cylinder_mesh;
    }

    // fixme: broken
    public static Mesh cone(float radius, float height, int resolution, boolean triangulate) {
        // returns a cone mesh
        // triangulate only affects the base for this mesh

        // if there are only 3 vertices in the base, then the base is already a triangle
        if (resolution == 3) triangulate = false;
        
        ArrayList<Vector3> verticesList = new ArrayList<Vector3>();
        verticesList.add(new Vector3(0, height, 0)); // cone apex vertex
        
        if (triangulate) verticesList.add(0, new Vector3(0, 0, 0));
        
        ArrayList<int[]> facesList = new ArrayList<int[]>();
        
        // in case we are triangulating the cone mesh we'll have two vertices at the beginning
        // of the vertices list: the base center (at index 0) and the apex (at index 1)
        // triangulate_addition is used to correctly indicize the faces,
        // adding one more index if there is also the base center
        int triangulate_addition = triangulate ? 1 : 0;
        int num_cone_vertices = 1 + resolution + triangulate_addition;
        
        int i;
        for (i = 0; i < resolution; i++) {
            double theta = i * 2 * Math.PI / resolution;
            float x = (float) (radius * Math.cos(theta));
            float z = (float) (radius * Math.sin(theta));
        
            verticesList.add(new Vector3(x, 0, z));
        
            // skipping the first two (cone center and apex)
            // or only skipping the first one (thanks to triangulate_addition)
            // in case we are not triangulating the mesh

            // computing the indices
            // int i1 = (i + 1 + triangulate_addition) % num_cone_vertices;
            // int i2 = (i + 2 + triangulate_addition) % num_cone_vertices;
            // // correcting modulo for a correct wrap-around index math
            // // (we need to skip the first 2 or 1 vertices depending on
            // // whether we are triangulating the cone mesh or not)
            // if (i1 == 0) i1 += 1 + triangulate_addition;
            // if (i2 == 0) i2 += 1 + triangulate_addition;

            int i1 = (1 + i + triangulate_addition) % resolution;
            int i2 = (2 + i + triangulate_addition) % resolution;

            // adding the faces
            facesList.add(new int[] { 0 + triangulate_addition, i2, i1 }); // lateral face
            if (triangulate) facesList.add(new int[] { 0, i1, i2 }); // base face triangle (only if triangulating the mesh)
        }

        // building the mesh with automatic normal calculation
        Vector3[] vertices = new Vector3[verticesList.size()];
        int[][] faces = new int[facesList.size()][];

        for (i = 0; i < vertices.length; i++) {
            vertices[i] = verticesList.get(i);
        }
        for (i = 0; i < faces.length; i++) {
            faces[i] = facesList.get(i);
        }

        Mesh cone_mesh = new Mesh(vertices, faces);

        // manually adding the base face in case we are not triangulating the mesh
        if (!triangulate) {
            int[] bottom_face = Maths.range(1, resolution+1, 1); // a face connecting all the base vertices in a ring shape
            for (int in : bottom_face) System.out.println("ads "+  in);
            Vector3 bottom_normal = new Vector3(0, -1, 0); // the base normal points down in the negative y direction
            cone_mesh.add_face(bottom_face, bottom_normal); // base face normal
        }

        return cone_mesh;
    }

    public static Mesh square_pyramid(float side, float height, boolean triangulate) {
        // returns a square pyramid mesh

        float half_side = side / 2;
        Vector3[] vertices = {
            new Vector3(0, height, 0),
            new Vector3(-half_side, 0, half_side),
            new Vector3(half_side, 0, half_side),
            new Vector3(half_side, 0, -half_side),
            new Vector3(-half_side, 0, -half_side),
        };
        ArrayList<int[]> facesList = new ArrayList<int[]>();
        facesList.add(new int[]{ 0, 1, 2 }); // front
        facesList.add(new int[]{ 0, 2, 3 }); // right
        facesList.add(new int[]{ 0, 4, 1 }); // left
        facesList.add(new int[]{ 0, 3, 4 }); // back

        if (triangulate) {
            // manually triangulated base
            facesList.add(new int[] { 1, 3, 2 });
            facesList.add(new int[] { 1, 4, 3 });
        }

        int[][] faces = new int[facesList.size()][];
        int i;
        for (i = 0; i < faces.length; i++) {
            faces[i] = facesList.get(i);
        }

        Mesh pyramid_mesh = new Mesh(vertices, faces);
        
        if (!triangulate) {
            // manually add bottom face because that is a square and the other faces are triangles
            // so I let the computer compute those normals for me
            pyramid_mesh.add_face(new int[] { 1, 2, 3, 4 }, new Vector3(0, -1, 0));
        }

        return pyramid_mesh;
    }
}
