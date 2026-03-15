package toolbox.math;

public class Vector4 {
    
    private float x, y, z, w;

    /** Creates a vector with components x, y and z **/
    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /** Creates a vector with both components set to 0 **/
    public Vector4() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    /** Creates a vector with both components set to xyz **/
    public Vector4(float xyzw) {
        this.x = xyzw;
        this.y = xyzw;
        this.z = xyzw;
        this.w = xyzw;
    }

    /** Adds the given vector to this vector **/
    public void add(Vector4 vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
        this.w += vector.w;
    }

    /**
     * Subtracts the given vector from this vector.
     * Remember that a vector differences always points to the first vector tip.
     * Thus this will always return a vector pointing towards this vector tip.
    **/
    public void subtract(Vector4 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
        this.w -= vector.w;
    }
    
    /** Muliplies this vector components by the given s parameter **/
    public void multiply(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;
    }

    /** Divides this vector components by the given s parameter **/
    public void divide(float s) {
        if (s == 0) return;

        this.x /= s;
        this.y /= s;
        this.z /= s;
        this.w /= s;
    }

    /** Multiplies this vector components by -1 **/
    public void negate() {
        this.x *= -1;
        this.y *= -1;
        this.z *= -1;
        this.w *= -1;
    }

    /** Divides this vector components by its magnitude, thus turning it into a versor **/
    public void normalize() {
        final float magnitude = magnitude();
        
        if (magnitude == 0) return;
        
        this.x /= magnitude;
        this.y /= magnitude;
        this.z /= magnitude;
        this.w /= magnitude;
    }

    /** Returns this vector magnitude **/
    public float magnitude() {
        return (float) Math.sqrt((x * x) + (y * y) + (z * z) + (w * w));
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

    /** Returns the w component **/
    public float getW() {
        return w;
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

    /** Sets the w component to the given value **/
    public void setW(float w) {
        this.w = w;
    }

    /** Sets the x and y components to the given values **/
    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /** Sets both the x and y components to the given xy value **/
    public void set(float xyzw) {
        this.x = xyzw;
        this.y = xyzw;
        this.z = xyzw;
        this.w = xyzw;
    }

    // THE STATIC FUNCTIONS CREATE NEW VECTORS, THE METHODS CHANGE THE INSTANCE

    /** Returns the sum of the given vector **/
    public static Vector4 sum(Vector4 v0, Vector4 v1) {
        return new Vector4(v0.x + v1.x, v0.y + v1.y, v0.z + v1.z, v0.w + v1.w);
    }

    /**
     * Returns the vector difference between the given vectors.
     * Rememeber that the vector difference always points towards the first vector tip.
    **/
    public static Vector4 difference(Vector4 v0, Vector4 v1) {
        return new Vector4(v0.x - v1.x, v0.y - v1.y, v0.z - v1.z, v0.w - v1.w);
    }

    /** Returns the given vector scaled by s **/
    public static Vector4 scale(Vector4 vector, float s) {
        return new Vector4(vector.x * s, vector.y * s, vector.z * s, vector.w * s);
    }

    /** Returns the given vector scaled by -1 **/
    public static Vector4 negate(Vector4 vector) {
        return new Vector4(vector.x * -1, vector.y * -1, vector.z * -1, vector.w * -1);
    }

    /** Returns the dot product between the given vectors **/
    public static float dot(Vector4 v0, Vector4 v1) {
        return (v0.x * v1.x) + (v0.y * v1.y) + (v0.z * v1.z) + (v0.w * v1.w);
    }

    /** Returns the given vector after normalizing it **/
    public static Vector4 normalize(Vector4 vector) {
        final float magnitude = vector.magnitude();
        
        if (magnitude == 0) return vector;
        
        return new Vector4(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude, vector.w / magnitude);
    }
}
