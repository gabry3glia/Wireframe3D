package toolbox.math;

public class Vector2 {
    
    private float x, y;

    /** Creates a vector with components x and y **/
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** Creates a vector with both components set to 0 **/
    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    /** Creates a vector with both components set to xy **/
    public Vector2(float xy) {
        this.x = xy;
        this.y = xy;
    }

    /** Adds the given vector to this vector **/
    public void add(Vector2 vector) {
        this.x += vector.x;
        this.y += vector.y;
    }

    /**
     * Subtracts the given vector from this vector.
     * Remember that a vector differences always points to the first vector tip.
     * Thus this will always return a vector pointing towards this vector tip.
    **/
    public void subtract(Vector2 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
    }
    
    /** Muliplies this vector components by the given s parameter **/
    public void multiply(float s) {
        this.x *= s;
        this.y *= s;
    }

    /** Divides this vector components by the given s parameter **/
    public void divide(float s) {
        if (s == 0) return;

        this.x /= s;
        this.y /= s;
    }

    /** Multiplies this vector components by -1 **/
    public void negate() {
        this.x *= -1;
        this.y *= -1;
    }

    /** Divides this vector components by its magnitude, thus turning it into a versor **/
    public void normalize() {
        final float magnitude = magnitude();
        
        if (magnitude == 0) return;
        
        this.x /= magnitude;
        this.y /= magnitude;
    }

    /** Clamps this vector magnitude between the given minimum and maximum values **/
    public void clampMagnitude(float minimum, float maximum) {
        final float magnitude = magnitude();

        if (magnitude < minimum) setMagnitude(minimum);
        else if (magnitude > maximum) setMagnitude(maximum);
    }

    /** Sets this vector magnitude to the given value **/
    public void setMagnitude(float magnitude) {
        float angle = angle();
        final float xx = (float) (magnitude * Math.cos(angle));
        final float yy = (float) (magnitude * Math.sin(angle));

        this.x = xx;
        this.y = yy;
    }

    /** Returns this vector magnitude **/
    public float magnitude() {
        return (float) Math.sqrt((x * x) + (y * y));
    }

    /** Returns the radians angle between the horizontal axis and this vector **/
    public float angle() {
        return (float) Math.atan2(y, x);
    }

    /** Returns the x component **/
    public float getX() {
        return x;
    }

    /** Returns the y component **/
    public float getY() {
        return y;
    }

    /** Sets the x component to the given value **/
    public void setX(float x) {
        this.x = x;
    }

    /** Sets the y component to the given value **/
    public void setY(float y) {
        this.y = y;
    }

    /** Sets the x and y components to the given values **/
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** Sets both the x and y components to the given xy value **/
    public void set(float xy) {
        this.x = xy;
        this.y = xy;
    }

    /** Sets the x and y components based on the given polar coordinates values (theta must be in radians) **/
    public void setPolar(float magnitude, float theta) {
        this.x = (float) (magnitude * Math.cos(theta));
        this.y = (float) (magnitude * Math.sin(theta));
    }

    // THE STATIC FUNCTIONS CREATE NEW VECTORS, THE METHODS CHANGE THE INSTANCE

    /** Returns the sum of the given vector **/
    public static Vector2 sum(Vector2 v0, Vector2 v1) {
        return new Vector2(v0.x + v1.x, v0.y + v1.y);
    }

    /**
     * Returns the vector difference between the given vectors.
     * Rememeber that the vector difference always points towards the first vector tip.
    **/
    public static Vector2 difference(Vector2 v0, Vector2 v1) {
        return new Vector2(v0.x - v1.x, v0.y - v1.y);
    }

    /** Returns the given vector scaled by s **/
    public static Vector2 scale(Vector2 vector, float s) {
        return new Vector2(vector.x * s, vector.y * s);
    }

    /** Returns the given vector scaled by -1 **/
    public static Vector2 negate(Vector2 vector) {
        return new Vector2(vector.x * -1, vector.y * -1);
    }

    /** Returns the dot product between the given vectors **/
    public static float dot(Vector2 v0, Vector2 v1) {
        return (v0.x * v1.x) + (v0.y * v1.y);
    }

    /** Returns the given vector after normalizing it **/
    public static Vector2 normalize(Vector2 vector) {
        final float magnitude = vector.magnitude();
        
        if (magnitude == 0) return vector;
        
        return new Vector2(vector.x / magnitude, vector.y / magnitude);
    }

    /** Returns the radians angle between the given vectors (positive is counter-clockwise) **/
    public static float angle(Vector2 v0, Vector2 v1) {
        return v1.angle() - v0.angle();
    }
}
