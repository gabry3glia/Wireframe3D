package engine;

import toolbox.math.Vector3;

public class Transform {
    
    public Vector3 position, rotation, scale;

    public Transform() {
        position = new Vector3();
        rotation = new Vector3();
        scale = new Vector3(1);
    }

    /** Sums the transform position vector to the given position vector **/
    public void move(Vector3 position) {        
        this.position.add(position);
    }

    /** Sums the transform rotation vector to the given rotation vector (in degree euler angles) **/
    public void rotate(Vector3 rotation) {        
        this.rotation.add(rotation);
    }
    
    /** Sums the transform scale vector to the given scale vector **/
    public void scaling(Vector3 scale) {        
        this.scale.add(scale);
    }

    public Vector3[] applyTo(Vector3[] localSpaceVertices) {
        int vertexCount = localSpaceVertices.length;
        Vector3[] worldSpaceVertices = new Vector3[vertexCount];
        
        Vector3 transformedVertex;
        int i = 0;
        for (i = 0; i < vertexCount; i++) {
            transformedVertex = localSpaceVertices[i].copy();
            // scale (around origin)
            transformedVertex.multiply(scale);
            // rotate (around origin)
            transformedVertex.rotate3D(
                new float[] {
                    rotation.getX(),
                    rotation.getY(),
                    rotation.getZ()
                }
            );
            // translate (mesh center relative to world origin)
            transformedVertex.add(position);

            worldSpaceVertices[i] = transformedVertex;
        }

        return worldSpaceVertices;
    }
}
