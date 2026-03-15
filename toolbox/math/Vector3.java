package toolbox.math;

public class Vector3 {
    
    private float x, y, z;

    /** Creates a vector with components x, y and z **/
    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Creates a vector with both components set to 0 **/
    public Vector3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /** Creates a vector with both components set to xyz **/
    public Vector3(float xyz) {
        this.x = xyz;
        this.y = xyz;
        this.z = xyz;
    }

    /** Adds the given vector to this vector **/
    public void add(Vector3 vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
    }

    /**
     * Subtracts the given vector from this vector.
     * Remember that a vector differences always points to the first vector tip.
     * Thus this will always return a vector pointing towards this vector tip.
    **/
    public void subtract(Vector3 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
    }
    
    /** Muliplies this vector components by the given s parameter **/
    public void multiply(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    /** Divides this vector components by the given s parameter **/
    public void divide(float s) {
        if (s == 0) return;

        this.x /= s;
        this.y /= s;
        this.z /= s;
    }

    /** Multiplies this vector components by -1 **/
    public void negate() {
        this.x *= -1;
        this.y *= -1;
        this.z *= -1;
    }

    /** Divides this vector components by its magnitude, thus turning it into a versor **/
    public void normalize() {
        final float magnitude = magnitude();
        
        if (magnitude == 0) return;
        
        this.x /= magnitude;
        this.y /= magnitude;
        this.z /= magnitude;
    }

    /**
     * Performs the cross product operation between this vector and the given vector
     * storing the result in this vector
    **/
    public void cross(Vector3 vector) {
        x = (y * vector.z) - (z * vector.y);
        y = (z * vector.x) - (x * vector.z);
        z = (x * vector.y) - (y * vector.x);
    }

    /** Returns this vector magnitude **/
    public float magnitude() {
        return (float) Math.sqrt((x * x) + (y * y) + (z * z));
    }

    /** Returns the x component **/
    public float getX() {
        return x;
    }

    /** Returns the y component **/
    public float getY() {
        return y;
    }

    /** Returns the z component **/
    public float getZ() {
        return z;
    }

    /** Sets the x component to the given value **/
    public void setX(float x) {
        this.x = x;
    }

    /** Sets the y component to the given value **/
    public void setY(float y) {
        this.y = y;
    }

    /** Sets the z component to the given value **/
    public void setZ(float z) {
        this.z = z;
    }

    /** Sets the x and y components to the given values **/
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Sets both the x and y components to the given xy value **/
    public void set(float xyz) {
        this.x = xyz;
        this.y = xyz;
        this.z = xyz;
    }

    /** Muliplies this vector components by the corresponding scaling vector components **/
    public void multiply(Vector3 scalingVector) {
        this.x *= scalingVector.x;
        this.y *= scalingVector.y;
        this.z *= scalingVector.z;
    }

    /** Rotates the given vector by the given degree euler angles (pitch, yaw, roll)
    then returns the rotated vector **/
    public void rotate3D(float[] eulerAngles) {
        double x = this.x;
        double y = this.y;
        double z = this.z;

        double rx = 0;
        double ry = 0;
        double rz = 0;

        int i = 2;
        // rotate around all the three axes
        for (i = 2; i >= 0; i--) {
        // for (i = 0; i < 3; i++) {
            double theta = Math.toRadians(eulerAngles[i]);
            double cs = Math.cos(theta);
            double sn = Math.sin(theta);
            switch (i) {
                // rotation == 0 is around the x axis
                case 0:
                    rx = 1*x + 0*y + 0*z;
                    ry = 0*x + cs*y + sn*z;
                    rz = 0*x + -sn*y + cs*z;
                    break;
                // rotation == 1 is around the y axis
                case 1:
                    rx = cs*x + 0*y + sn*z;
                    ry = 0*x + 1*y + 0*z;
                    rz = -sn*x + 0*y + cs*z;
                    break;
                // rotation == 2 is around the z axis
                case 2:
                    rx = cs*x + sn*y + 0*z;
                    ry = -sn*x + cs*y + 0*z;
                    rz = 0*x + 0*y + 1*z;
                    break;
                default:
                    break;
            }
            // prepare for the next rotation
            x = rx;
            y = ry;
            z = rz;
        }

        this.set((float) x, (float) y, (float) z);
    }

    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vector3(%.2f, %.2f, %.2f)", x, y, z);
    }

    // THE STATIC FUNCTIONS CREATE NEW VECTORS, THE METHODS CHANGE THE INSTANCE

    /** Returns the sum of the given vector **/
    public static Vector3 sum(Vector3 v0, Vector3 v1) {
        return new Vector3(v0.x + v1.x, v0.y + v1.y, v0.z + v1.z);
    }

    /**
     * Returns the vector difference between the given vectors.
     * Rememeber that the vector difference always points towards the first vector tip.
    **/
    public static Vector3 difference(Vector3 v0, Vector3 v1) {
        return new Vector3(v0.x - v1.x, v0.y - v1.y, v0.z - v1.z);
    }

    /** Returns the given vector scaled by s **/
    public static Vector3 scale(Vector3 vector, float s) {
        return new Vector3(vector.x * s, vector.y * s, vector.z * s);
    }

    /** Returns the given vector scaled by -1 **/
    public static Vector3 negate(Vector3 vector) {
        return new Vector3(vector.x * -1, vector.y * -1, vector.z * -1);
    }

    /** Returns the dot product between the given vectors **/
    public static float dot(Vector3 v0, Vector3 v1) {
        return (v0.x * v1.x) + (v0.y * v1.y) + (v0.z * v1.z);
    }

    /** Returns the cross product between the given vectors **/
    public static Vector3 cross(Vector3 v0, Vector3 v1) {
        return new Vector3(
            (v0.y * v1.z) - (v0.z * v1.y),
            (v0.z * v1.x) - (v0.x * v1.z),
            (v0.x * v1.y) - (v0.y * v1.x)
        );
    }

    /** Returns the given vector after normalizing it **/
    public static Vector3 normalize(Vector3 vector) {
        final float magnitude = vector.magnitude();
        
        if (magnitude == 0) return vector;
        
        return new Vector3(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude);
    }
}
