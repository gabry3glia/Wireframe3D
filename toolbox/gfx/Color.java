package toolbox.gfx;

import toolbox.math.Maths;

public class Color {

    // COLOR CONSTANTS
    // public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color LIGHT_GRAY = new Color(200, 200, 200);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color DARK_GRAY = new Color(100, 100, 100);
    public static final Color BLACK = new Color(0, 0, 0);

    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE = new Color(0, 0, 255);

    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color MAGENTA = new Color(255, 0, 255);
    public static final Color YELLOW = new Color(255, 255, 0);

    public static final Color DARK_RED = new Color(128, 0, 0);
    public static final Color DARK_GREEN = new Color(0, 128, 0);
    public static final Color DARK_BLUE = new Color(0, 0, 128);

    public static final Color DARK_CYAN = new Color(0, 128, 128);
    public static final Color DARK_MAGENTA = new Color(128, 0, 128);
    public static final Color DARK_YELLOW = new Color(128, 128, 0);

    public static final Color LIGHT_RED = new Color(255, 200, 200);
    public static final Color LIGHT_GREEN = new Color(200, 255, 200);
    public static final Color LIGHT_BLUE = new Color(200, 200, 255);

    public static final Color LIGHT_CYAN = new Color(200, 255, 255);
    public static final Color LIGHT_MAGENTA = new Color(255, 200, 255);
    public static final Color LIGHT_YELLOW = new Color(255, 255, 200);
    
    private int r, g, b, a; // for now alpha transparency and color sovrapposition are not supported

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        a = 255;
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int toInt() {
        return b | (g << 8) | (r << 16) | (a << 24);
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getAlpha() {
        return a;
    }

    public void set(int r, int g, int b) {
        this.r = Math.clamp(r, 0, 255);
        this.g = Math.clamp(g, 0, 255);
        this.b = Math.clamp(b, 0, 255);
    }

    public void setRed(int r) {
        this.r = r;
        r = (int) Maths.clamp(r, 0, 255);
    }

    public void setGreen(int g) {
        this.g = g;
        g = (int) Maths.clamp(g, 0, 255);
    }

    public void setBlue(int b) {
        this.b = b;
        b = (int) Maths.clamp(b, 0, 255);
    }

    public void setAlpha(int a) {
        this.a = a;
        a = (int) Maths.clamp(a, 0, 255);
    }

    public void changeRedBy(int amount) {
        r += amount;
        r = (int) Maths.clamp(r, 0, 255);
    }

    public void changeGreenBy(int amount) {
        g += amount;
        g = (int) Maths.clamp(g, 0, 255);
    }

    public void changeBlueBy(int amount) {
        b += amount;
        b = (int) Maths.clamp(b, 0, 255);
    }

    public void changeAlphaBy(int amount) {
        a += amount;
        a = (int) Maths.clamp(a, 0, 255);
    }

    /** Returns a factor percent darker version of this color (e.g.: factor = 0.5f means 50 % darker) **/
    public Color darker(float factor) {
        return new Color(
            (int) (r * factor),
            (int) (g * factor),
            (int) (b * factor)
        );
    }

    /** Returns a factor percent brighter version of this color (e.g.: factor = 0.5f means 50 % brighter) **/
    public Color brighter(float factor) {
        return new Color(
            (int) Maths.clamp(r * (1 + factor), 0, 255),
            (int) Maths.clamp(g * (1 + factor), 0, 255),
            (int) Maths.clamp(b * (1 + factor), 0, 255)
        );
    }

    /** Returns a factor percent trasparent version of this color (e.g.: alpha = 0.5f) **/
    public Color transparent(float alpha) {
        return new Color(
            r, g, b,
            (int) Maths.clamp(255 * alpha, 0, 255)
        );
    }

    /** Takes an r, g, b, a set of integers and returns the corresponding hexadecimal ARGB color (format 0xAARRGGBB) **/
    public static int toInt(int r, int g, int b, int a) {
        return b | (g << 8) | (r << 16) | (a << 24);
    }

    /** Takes an hexadecimal ARGB color (format 0xAARRGGBB) and returns a new Color object instance **/
    public static Color fromInt(int i) {
        int r = (i >>> 16) & 0xff;
        int g = (i >>> 8) & 0xff;
        int b = i & 0xff;
        int a = (i >>> 24) & 0xff;
        return new Color(r, g, b, a);
    }

    /** Takes an hexadecimal ARGB color (format 0xAARRGGBB) and returns the channels as an int array { r, g, b, a } **/
    public static int[] getChannelsFromInt(int i) {
        int r = (i >>> 16) & 0xff;
        int g = (i >>> 8) & 0xff;
        int b = i & 0xff;
        int a = (i >>> 24) & 0xff;
        return new int[] { r, g, b, a };
    }

    @Override
    public String toString() {
        return String.format("rbga(%d, %d, %d, %d)", r, g, b, a);
    }
}
