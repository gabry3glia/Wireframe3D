import engine.Camera;
import engine.Mesh;
import engine.Renderer;
import engine.Transform;
import toolbox.Sketch;
import toolbox.gfx.Color;
import toolbox.math.Vector3;

public class Engine extends Sketch {

    private Renderer renderer;

    private Camera camera;

    private Mesh mesh;
    private Transform transform;
      
    @Override  
    public void setup() {
        renderer = new Renderer(this.screen);
        renderer.perspective(this.screen.getWidth(), this.screen.getHeight(), 0.01f, 1000.0f, 70.f);
        // TODO: ortho + face culling doesn't work because the camera position is still the one in the perspetive projection
        // TODO: you should change the way the camera moves in ortho mode
        // renderer.orthographic(getWindowWidth(), getWindowHeight(), 10f, 1000.0f);

        renderer.enableFaceCulling(Renderer.BACK_FACE);

        // mesh = Mesh.box(100, 100, 100, false);
        mesh = Mesh.cylinder(100, 100, 10, true); 
        // mesh = Mesh.square_pyramid(50, 100, true);
        // mesh = Mesh.cone(50, 100, 100, true); // TODO: this is not working well... still working on it
        transform = new Transform();
        transform.position.setZ(-140);
  
        camera = new Camera();
    }   
 
    private final Vector3 rot = new Vector3(0, 0.2f, 0);
    @Override
    public void update() {
        transform.rotate(rot);
        Camera.cameraController(input, camera, 2f, 2f);
    } 

    @Override
    public void render() {
        renderer.drawMesh(mesh, transform, camera);
        renderer.gizmos(camera);
    }
 
    public static void main(String[] args) {
        new Engine().createCanvas("3D Java Engine", 600, 600, 1, Color.WHITE);
    }
} 
