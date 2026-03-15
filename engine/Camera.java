package engine;

import java.awt.event.KeyEvent;

import toolbox.Input;
import toolbox.math.Maths;
import toolbox.math.Vector3;

public class Camera {
    
    public Vector3 position;
    public Vector3 rotation;

    // ortho-normal right-handed coordinate system
    public static Vector3 WORLD_RIGHT = new Vector3(1, 0, 0);
    public static Vector3 WORLD_UP = new Vector3(0, 1, 0);
    public static Vector3 WORLD_FORWARD = new Vector3(0, 0, -1);
    
    public Vector3 forward, right, up;

    public Camera() {
        position = new Vector3();
        rotation = new Vector3();

        forward = new Vector3();
        right = new Vector3();
        up = new Vector3();
    }

    public Vector3[] view(Vector3[] worldSpaceVertices) {
        int vertexCount = worldSpaceVertices.length;
        Vector3[] viewSpaceVertices = new Vector3[vertexCount];
        
        Vector3 viewedVertex;
        int i = 0;
        for (i = 0; i < vertexCount; i++) {
            viewedVertex = worldSpaceVertices[i].copy();
            // translate (origin to camera)
            viewedVertex.add(Vector3.negate(position));
            // rotate (around camera)
            viewedVertex.rotate3D(
                new float[] {
                    rotation.getX(),
                    rotation.getY(),
                    rotation.getZ()
                }
            );

            viewSpaceVertices[i] = viewedVertex;
        }

        return viewSpaceVertices;
    }

    // camera motion and rotation

    /** Increments the camera position by the given move vector **/
    public void move(Vector3 move) {
        position.add(move);
    }

    /** Moves the camera along its coordinate system axes by the given amounts **/
    public void moveAlongVectors(float forward, float right, float up) {
        // get the movement along each axis
        Vector3 forwardMovement = Vector3.scale(this.forward, forward);
        Vector3 rightMovement = Vector3.scale(this.right, right);
        Vector3 upMovement = Vector3.scale(this.up, up);
        
        // combine the movements together
        Vector3 movement = new Vector3();
        movement.add(forwardMovement);
        movement.add(rightMovement);
        movement.add(upMovement);

        position.add(movement);
    }

    /** Rotates the camera by the given degree euler angles **/
    public void rotate(float pitch, float yaw, float roll) {
        rotation.set(
            Maths.clamp(rotation.getX() + pitch, -90, 90),
            (rotation.getY() + yaw) % 360,
            (rotation.getZ() + roll) % 360
        );

        updateVectors();
    }

    /** Updates the camera forward, right and up vectors based on its current rotation in degree euler angles **/
    private void updateVectors() {
        // calculate camera forward, right and up vectors
        double theta = Math.toRadians(rotation.getX()); // pitch
        double phi = Math.toRadians(rotation.getY()); // yaw
        float x = (float) (Math.sin(phi) * Math.cos(theta));
        float y = (float) Math.sin(theta);
        float z = (float) -(Math.cos(phi) * Math.cos(theta));
        // update the camera vectors
        forward.set(x, y, z);
        right = Vector3.cross(forward, WORLD_UP);
        up = Vector3.cross(right, forward);

        // System.err.println("forward: " + forward);
        // System.err.println("right: " + right);
        // System.err.println("up: " + up);
    }

    public static void cameraController(Input input, Camera camera, float move_speed, float rot_speed) {
        float ptc = 0;
        float yaw = 0;
        if (input.isKeyDown(KeyEvent.VK_UP)) ptc = rot_speed; // look up
        if (input.isKeyDown(KeyEvent.VK_DOWN)) ptc = -rot_speed; // look down
        if (input.isKeyDown(KeyEvent.VK_LEFT)) yaw = -rot_speed; // look left
        if (input.isKeyDown(KeyEvent.VK_RIGHT)) yaw = rot_speed; // look right
        camera.rotate(ptc, yaw, 0);
        
        float fwd = 0;
        float rgt = 0;
        float up = 0;
        if (input.isKeyDown(KeyEvent.VK_W)) fwd = move_speed; // forward
        if (input.isKeyDown(KeyEvent.VK_A)) rgt = -move_speed; // left
        if (input.isKeyDown(KeyEvent.VK_S)) fwd = -move_speed; // backward
        if (input.isKeyDown(KeyEvent.VK_D)) rgt = move_speed; // right
        if (input.isKeyDown(KeyEvent.VK_SPACE)) up = move_speed; // up
        if (input.isKeyDown(KeyEvent.VK_SHIFT)) up = -move_speed; // down
        // forward motion vector
        Vector3 camera_fwd = camera.forward.copy();
        camera_fwd.setY(0);
        camera_fwd.normalize();
        camera_fwd.multiply(fwd);
        // strafe motion vector
        Vector3 camera_rgt = camera.right.copy();
        camera_rgt.setY(0);
        camera_rgt.normalize();
        camera_rgt.multiply(rgt);

        // combine the motions
        Vector3 motion = Vector3.sum(camera_fwd, camera_rgt);
        motion.setY(up);
        
        camera.move(motion);
    }
}
