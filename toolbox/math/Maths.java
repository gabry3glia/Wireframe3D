package toolbox.math;

public class Maths {

    /** Returns the minimum between the given float values **/
    public static float min(float... values) {
        int minimumIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < values[minimumIndex]) {
                minimumIndex = i;
            }
        }
        return values[minimumIndex];
    }

    /** Returns the maximum between the given float values **/
    public static float max(float... values) {
        int maximumIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > values[maximumIndex]) {
                maximumIndex = i;
            }
        }
        return values[maximumIndex];
    }

    /** Clamps the given float value between the two given minimum and maximum values **/
    public static float clamp(float value, float minimum, float maximum) {
        return min(max(minimum, value), maximum);
    }
    
    /** Linear interpolation from a to b with interpolation parameter t (ranged [0.0, 1.0]) **/
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Maps the given float value, ranged from a to b, to an integer ranged from start to end including all end points **/
    public static int map(float value, float a, float b, int start, int end) {
        float t = (value - a) / (b - a); // percentage
        return (int) (start + (end - start) * t);
    }

    /** Returns an integer array populated with ints from start (included) to end (excluded)
     * The difference between two contiguous elements will always be step **/
    public static int[] range(int start, int end, int step) {
        int i = 0, n;
        int[] result = new int[(end + 1 - start) / step];
        for (n = start; n < end; n += step) {
            // if (n >= end) break;
            result[i] = n;
            i++;
        }
        return result;
    }
}
