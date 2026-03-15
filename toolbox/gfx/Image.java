package toolbox.gfx;

import java.awt.image.BufferedImage;

import toolbox.utils.FileHandler;

public class Image {

    private int width, height;
    private int[] pixels;

    public Image(int width, int height, int[] pixels) {
        if (width * height != pixels.length) {
            throw new IllegalArgumentException("Invalid image parameters");
        }

        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Image(String path) {
        BufferedImage bufferedImage = FileHandler.loadImage(path);
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
        pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = bufferedImage.getRGB(i % width, i / width);
        }
    }

    // SETTERS

    /** Sets the pixel at the given coordinates to the given color (does not take translation into account) **/
    public void setPixel(int x, int y, Color color) {
        // flip y to make the coordinate system a y-up one
        y = height - 1 - y;

        if (isOutside(x, y)) return;
        pixels[x + y * width] = color.toInt();
    }

    /** Returns the color of the pixel at the given coordinates (does not take translation into account) **/
    public Color getPixel(int x, int y) {
        // flip y to make the coordinate system a y-up one
        y = height - 1 - y;

        if (isOutside(x, y)) return null;
        return Color.fromInt(pixels[x + y * width]);
    }


    // GETTERS

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    // UTILITY & CHECKS

    /** Returns true if the given coordinates are inside the screen, false if they are outside (does not take translation into account) **/
    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Returns true if the given coordinates are outside the screen, false if they are inside (does not take translation into account) **/
    public boolean isOutside(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }
}
