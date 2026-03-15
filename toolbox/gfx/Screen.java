package toolbox.gfx;

import toolbox.math.Vector2;
import toolbox.math.Vector3;

public class Screen {

    // CONSTANTS
    private final Vector2 X_AXIS = new Vector2(15, 0); // points left
    private final Vector2 Y_AXIS = new Vector2(0, 15); // points up

    private static final double HALF_PI = Math.PI / 2;
    private static final double PI_OVER_FOUR = Math.PI / 4;
    private static final double THREE_PI_OVER_FOUR = 3 * Math.PI / 4;
    
    public static final int BRUSH_CIRCLE = 0;
    public static final int BRUSH_SQUARE = 1;
    
    private int width, height;
    private int[] pixels;

    // these are the exact coordinate of the pixel on the corresponding side
    // (e.g.: left is the x coordinate of the leftmost pixels in the canvas)
    private int left, right, top, bottom;
    private int leftPadding, rightPadding, topPadding, bottomPadding;

    private boolean outlinesEnabled = true;
    private boolean fillEnabled = false;

    private Color backgroundColor = Color.WHITE;
    private Color outlineColor = Color.BLACK;
    private Color fillColor = Color.RED;

    private int brushShape = BRUSH_CIRCLE;
    private int strokeWeight = 1;
    
    // translation
    private int tx = 0, ty = 0;

    /** Remember that the coordinate system is a y-up system **/
    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];

        resetTranslation();
        resetPadding();
    }

    // BASIC FUNCTIONS

    // The void setPixel() method should be the only channel through which
    // the communication between the outside world and the raster happens.
    // This means that if you want to edit a pixel you could always call this method
    // and never use direct raster int array editing via `pixel[index] = hexARGBcolor;`
    // void setPixel() should always be the only method that contains this line: `pixel[index] = hexARGBcolor;`

    /** Sets the pixel at the given coordinates to the given color (does not take translation into account) **/
    public void setPixel(int x, int y, Color color) {
        if (color == null || color.getAlpha() == 0) return;
        
        if (isOutside(x, y)) return;
        // pixels[x + y * width] = color.toInt();

        int[] oldChannels = Color.getChannelsFromInt(pixels[x + y * width]);
        
        // unpack
        int oldR = oldChannels[0];
        int oldG = oldChannels[1];
        int oldB = oldChannels[2];

        float alpha = color.getAlpha() / 255.0f;
        float invAlpha = 1.0f - alpha;

        // color blending (Linear Interpolation)
        int r = (int) (color.getRed() * alpha + oldR * invAlpha);
        int g = (int) (color.getGreen() * alpha + oldG * invAlpha);
        int b = (int) (color.getBlue() * alpha + oldB * invAlpha);
        
        // pack color back again (alpha is kept at 255 to have an opaque screen)
        pixels[x + y * width] = Color.toInt(r, g, b, 255);
    }

    public void setPixel(int i, Color color) {
        setPixel(i % getWidth(), i / getWidth(), color);
    }

    /** Returns the color of the pixel at the given coordinates (does not take translation into account) **/
    public Color getPixel(int x, int y) {
        // flip y to make the coordinate system a y-up one
        y = height - 1 - y;

        if (isOutside(x, y)) return null;
        return Color.fromInt(pixels[x + y * width]);
    }

    // GRAPHICS FUNCTIONS

    /** Clears the screen to the given color **/
    public void clear(Color color) {
        for (int i = 0; i < pixels.length; i++) {
            setPixel(i, color);
        }
    }

    /** Clears the screen to the set background color **/
    public void clear() {
        clear(backgroundColor);
    }

    /**
     * Clears the screen by drawing a semi-transparent veil over it with the specified alpha
     * @param color the color of the clearing
     * @param alpha the transparency of the veil ranged [0, 255], the higher it is the faster the frame buffer gets cleared
     */
    public void fadeClear(Color color, int alpha) {
        Color fadeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        
        // save the shape drawer options
        boolean wasFillEnabled = fillEnabled;
        boolean wasOutlinesEnabled = outlinesEnabled;
        
        fill(fadeColor);
        disableOutlines();
        
        // draw the transparent veil
        rectangle(getLeft(), getBottom(), getRight() + 1, getTop() + 1);
        
        // reset the shape drawer options
        fillEnabled = wasFillEnabled;
        outlinesEnabled = wasOutlinesEnabled;
    }

    public void fadeClear(int alpha) {
        fadeClear(backgroundColor, alpha);
    }

    // SETTERS

    /** Sets the background color **/
    public void background(Color color) {
        backgroundColor = color;
    }

    /** Sets the shape outline color (automatically enables outlines) **/
    public void outlines(Color color) {
        if (color == null) return;
        outlineColor = color;
        outlinesEnabled = true;
    }

    /** Sets the shape fill color (automatically enables shape filling) **/
    public void fill(Color color) {
        if (color == null) return;
        fillColor = color;
        fillEnabled = true;
    }

    /** Sets the brush shape (affects points and lines) **/
    public void brush(int shape) {
        brushShape = shape;
    }
    
    /** Sets the stroke width for shape outlines **/
    public void stroke(int width) {
        strokeWeight = width;
    }

    /** Disables shape outlines **/
    public void disableOutlines() {
        outlinesEnabled = false;
    }

    /** Disables shape filling **/
    public void disableFill() {
        fillEnabled = false;
    }

    /** Sets the translation vector to (tx, ty) (does not affect setPixel()) **/
    public void translate(int tx, int ty) {
        this.tx = tx;
        this.ty = ty;

        left = -tx;
        right = left + width - 1;
        top = -ty;
        bottom = top + height - 1;
        
        // bottom = -ty;
        // top = bottom + height - 1;
    }

    /** Sets the translation vector back to (getCenterX(), getCenterY()) **/
    public void translateToCenter() {
        translate(getCenterX(), getCenterY());
    }

    /** Sets the translation vector back to (0, 0) **/
    public void resetTranslation() {
        translate(0, 0);
    }

    public void padding(int left, int right, int top, int bottom) {
        leftPadding = left;
        rightPadding = right;
        topPadding = top;
        bottomPadding = bottom;
    }

    public void padding(int horizontal, int vertical) {
        leftPadding = horizontal;
        rightPadding = horizontal;
        topPadding = vertical;
        bottomPadding = vertical;
    }

    /** Applies the same padding to all four sides **/
    public void padding(int padding) {
        leftPadding = padding;
        rightPadding = padding;
        topPadding = padding;
        bottomPadding = padding;
    }

    public void resetPadding() {
        leftPadding = 0;
        rightPadding = 0;
        topPadding = 0;
        bottomPadding = 0;
    }

    // GETTERS

    /**
     * Returns the screen (canvas) width
     * Note this is not the window width
     * If you pick a pixelScale = 1, window width == screen width
     * If you pick a pixelScale = 3, window width == 3 * screen width
    **/
    public int getWidth() {
        return width;
    }

    /**
     * Returns the screen (canvas) height
     * Note this is not the window height
     * If you pick a pixelScale = 1, window height == screen height
     * If you pick a pixelScale = 3, window height == 3 * screen height
    **/
    public int getHeight() {
        return height;
    }

    /** Returns the screen pixels array **/
    public int[] getPixels() {
        return pixels;
    }

    /** Returns the screen (canvas) center x position **/
    public int getCenterX() {
        return width / 2;
    }

    /** Returns the screen (canvas) center y position **/
    public int getCenterY() {
        return height / 2;
    }

    /** Returns the leftmost x coordinate inside the visible screen (canvas) **/
    public int getLeft() {
        return left + leftPadding;
    }

    /** Returns the rightmost x coordinate inside the visible screen (canvas) **/
    public int getRight() {
        return right - rightPadding;
    }

    /** Returns the top y coordinate inside the visible screen (canvas) **/
    public int getTop() {
        return top + topPadding;
    }

    /** Returns the bottom y coordinate inside the visible screen (canvas) **/
    public int getBottom() {
        return bottom - bottomPadding;
    }

    /** Returns the left padding value **/
    public int getLeftPadding() {
        return leftPadding;
    }

    /** Returns the right padding value **/
    public int getRightPadding() {
        return rightPadding;
    }

    /** Returns the top padding value **/
    public int getTopPadding() {
        return topPadding;
    }

    /** Returns the bottom padding value **/
    public int getBottomPadding() {
        return bottomPadding;
    }

    // DRAWING

    // primitives

    /** Draws a point at the given coordinates **/
    public void point(int x, int y, int radius, Color color) {
        if (radius <= 0 || isBoundingBoxOutside(x, y, radius, radius) || color == null) return;

        // rotate
        // TODO
        // translate
        x += tx;
        y += ty;
   
        for (int yp = y - radius; yp < y + radius + 1; yp++) {
            for (int xp = x - radius; xp < x + radius + 1; xp++) {
                boolean drawCondition = true;
                if (brushShape == BRUSH_CIRCLE) {
                    final int x2 = (xp - x) * (xp - x);
                    final int y2 = (yp - y) * (yp - y);
                    final int p2 = x2 + y2;
                    final int r2 = radius * radius;
                    drawCondition = p2 < r2;
                } else if (brushShape == BRUSH_SQUARE) {
                    final boolean xx = x - radius < xp && xp < x + radius;
                    final boolean yy = y - radius < yp && yp < y + radius;
                    drawCondition = xx && yy;
                }

                if (drawCondition) {
                    setPixel(xp, yp, color);
                }
            }
        }
    }

    /** Draws a line between the two given end points (x0, y0) and (x1, y1) **/
    public void line(int x0, int y0, int x1, int y1, Color color) {
        if (x0 < left && x1 < left) return; // both too left
        if (x0 > right && x1 > right) return; // both too right
        if (y0 < top && y1 < top) return; // both too top
        if (y0 > bottom && y1 > bottom) return; // both too bottom

        if (color == null) return;

        final float slope;
        final int dir;
        final float theta;

        if (x1 - x0 != 0) {
            slope = (float) (y1 - y0) / (float) (x1 - x0);
            theta = (float) Math.atan2(y1 - y0, x1 - x0);
            dir = theta <= -PI_OVER_FOUR || theta > THREE_PI_OVER_FOUR ? -1 : 1;

            // project line points to the screen border if the points are outside to optimize
            // p0
            if (x0 < left) {
                y0 += (left-x0) * slope;
                x0 = left;
            }
            if (y0 < top) {
                x0 += (top - y0) / slope;
                y0 = top;
            }
            if (x0 > right) {
                y0 -= (x0 - right) * slope;
                x0 = right;
            }
            if (y0 > bottom) {
                x0 -= (y0 - bottom) / slope;
                y0 = bottom;
            }
            // p1
            if (x1 < left) {
                y1 += (left-x1) * slope;
                x1 = left;
            }
            if (y1 < top) {
                x1 += (top - y1) / slope;
                y1 = top;
            }
            if (x1 > right) {
                y1 -= (x1 - right) * slope;
                x1 = right;
            }
            if (y1 > bottom) {
                x1 -= (y1 - bottom) / slope;
                y1 = bottom;
            }
        } else {
            x0 = Math.clamp(x0, left, right);
            y0 = Math.clamp(y0, top, bottom);
            for (int yp = 0; yp < Math.abs(y1 - y0); yp++) {
                point(x0, yp + (y0 < y1 ? y0 : y1 + 1), strokeWeight, color);
            }

            return;
        }

        // if slope is 45° or less
        // do the loop on the x because the increase in x is greater than the one on the y
        if (Math.abs(slope) < 1) {
            int yp;
            for (int xp = 0; xp < Math.abs(x1 - x0); xp++) {
                yp = (int) (slope * xp);
                point(x0 + (xp * dir), y0 + (yp * dir), strokeWeight, color);
            }
        } else if (Math.abs(slope) > 1) {
            int xp;
            for (int yp = 0; yp < Math.abs(y1 - y0); yp++) {
                xp = (int) (yp / slope);
                point(x0 + (xp * dir), y0 + (yp * dir), strokeWeight, color);
            }
        } else {
            for (int xp = 0; xp < Math.abs(x1 - x0); xp++) {
                int xs = 0 > theta && theta > -HALF_PI ? -1 : 1;
                int ys = HALF_PI <= theta && theta < Math.PI ? -1 : 1;
                point(x0 + (xp * dir * xs), y0 + (xp * dir * ys), strokeWeight, color);
            }
        }
    }

    /** Draws a rectangle with the given top left corner (x0, y0) and bottom right corner (x1, y1) coordinates **/
    public void rectangle(int x0, int y0, int x1, int y1) {
        final int hSide = Math.abs(x1 - x0);
        final int vSide = Math.abs(y1 - y0);

        // filling
        if (fillEnabled) {
            for (int yp = 0; yp < vSide; yp++) {
                for (int xp = 0; xp < hSide; xp++) {
                    point(xp + Math.min(x0, x1), yp + Math.min(y0, y1), 1, fillColor);
                }
            }
        }
        
        // outlines
        if (outlinesEnabled) {
            line(x0, y0, x1 + 1, y0, outlineColor);
            line(x0, y1, x1 + 1, y1, outlineColor);
            line(x0, y0, x0, y1 + 1, outlineColor);
            line(x1, y0, x1, y1 + 1, outlineColor);
        }
    }

    /** Does not support outline, only filling **/
    public void ellipse(int cx, int cy, int xRadius, int yRadius) {
        for (int yp = cy - yRadius; yp < cy + yRadius; yp++) {
            for (int xp = cx - xRadius; xp < cx + xRadius; xp++) {
                final float x2 = (xp - cx) * (xp - cx);
                final float y2 = (yp - cy) * (yp - cy);
                final float a2 = xRadius * xRadius;
                final float b2 = yRadius * yRadius;

                final float ellipseEquation = (x2 / a2) + (y2 / b2);

                if (ellipseEquation < 1) {
                    point(xp, yp, 1, fillColor);
                }
            }
        }
    }

    // collections
    public void points(Vector2[] points, int radius, Color color) {
        for (Vector2 point : points) {
            point((int) point.getX(), (int) point.getY(), radius, color);
        }
    }

    public void lines(Vector2[] points, Color color, boolean close) {
        int prevX = (int) points[0].getX();
        int prevY = (int) points[0].getY();
        int currX, currY;
        for (int i = 1; i < points.length; i++) {
            currX = (int) points[i].getX();
            currY = (int) points[i].getY();
            line(prevX, prevY, currX, currY, color);
            prevX = currX;
            prevY = currY;
        }
        // last line to close if needed
        if (close) {
            line(prevX, prevY, (int) points[0].getX(), (int) points[0].getY(), color);
        }
    }

    public void polygon() {}

    // derived

    public void polarLine(int x, int y, int length, float radiansAngle, Color color) {
        final int x1 = (int) (x + length * Math.cos(radiansAngle));
        final int y1 = (int) (y + length * Math.sin(radiansAngle));
        line(x, y, x1, y1, color);
    }

    /** Draws a square with the given top left corner position and side **/
    public void square(int x, int y, int side) {
        rectangle(x, y, x + side, y + side);
    }
    
    public void circle(int cx, int cy, int radius) {
        if (radius < 0 || isBoundingBoxOutside(cx, cy, radius, radius)) return;

        // a circle with radius one is just a point
        if (radius == 0) {
            point(cx, cy, 1, outlineColor);
            return;
        }

        final float minR = radius - (strokeWeight + 1) / 2.0f;
        final float maxR = radius + (strokeWeight + 1) / 2.0f;

        for (int yp = (int) (cy - maxR); yp < cy + maxR + 1; yp++) {
            for (int xp = (int) (cx - maxR); xp < cx + maxR + 1; xp++) {
                final int x2 = (xp - cx) * (xp - cx);
                final int y2 = (yp - cy) * (yp - cy);
                final int p2 = x2 + y2;
                final int minR2 = (int) (minR * minR);
                final int maxR2 = (int) (maxR * maxR);
                // prioritize outline over filling (so no weird inside shapes appear)
                if (minR2 <= p2 && p2 < maxR2 && outlinesEnabled) {
                    point(xp, yp, 1, outlineColor);
                } else if (p2 < minR2) {
                    point(xp, yp, 1, fillColor);
                }
            }
        }
    }

    public void triangle(Vector2 p0, Vector2 p1, Vector2 p2) {
        if (fillEnabled) {    
            int leftBound = (int) Math.max(getLeft(), Math.min(Math.min(p0.getX(), p1.getX()), p2.getX()));
            int rightBound = (int) Math.min(getRight() + 1, Math.max(Math.max(p0.getX(), p1.getX()), p2.getX()));
            int topBound = (int) Math.max(getTop(), Math.min(Math.min(p0.getY(), p1.getY()), p2.getY()));
            int bottomBound = (int) Math.min(getBottom() + 1, Math.max(Math.max(p0.getY(), p1.getY()), p2.getY()));

            final Vector3 p03 = new Vector3(p0.getX(), p0.getY(), 0);
            final Vector3 p13 = new Vector3(p1.getX(), p1.getY(), 0);
            final Vector3 p23 = new Vector3(p2.getX(), p2.getY(), 0);

            final Vector3 side01 = Vector3.difference(p13, p03);
            final Vector3 side12 = Vector3.difference(p23, p13);
            final Vector3 side20 = Vector3.difference(p03, p23);
            
            Vector3 p = new Vector3();
            for (int yp = topBound; yp < bottomBound; yp++) {   
                for (int xp = leftBound; xp < rightBound; xp++) {
                    p.set(xp, yp, 0);
                    int z0 = (int) Math.signum(Vector3.cross(side01, Vector3.difference(p, p03)).getZ());
                    int z1 = (int) Math.signum(Vector3.cross(side12, Vector3.difference(p, p13)).getZ());
                    int z2 = (int) Math.signum(Vector3.cross(side20, Vector3.difference(p, p23)).getZ());
                    if (z0 == z1 && z1 == z2) {
                        point(xp, yp, 1, fillColor);
                    }
                }
            }
        }

        // draw outlines over the filled pixels to be sure they are visible and not overdrawn
        if (outlinesEnabled) {
            lines(new Vector2[] {p0, p1, p2}, outlineColor, true);
        }
    }

    public void vector(Vector2 vector, int x, int y, Color color) {
        final float magnitude = vector.magnitude();
        
        if ((int) magnitude == 0) {
            point(x, y, 1, color);
            return;
        }

        final float angle = vector.angle();
        final int x1 = (int) (x + magnitude * Math.cos(angle));
        final int y1 = (int) (y + magnitude * Math.sin(angle));
        
        line(x, y, x1, y1, color);

        final int arrowLength = 7 * strokeWeight;
        final float angleOffset = (float) Math.toRadians(150);
        polarLine(x1, y1, arrowLength, angle + angleOffset, color);
        polarLine(x1, y1, arrowLength, angle - angleOffset, color);
    }

    public void image(Image image, int x, int y) {
        x += tx;
        y += ty;

        // not using left and right because there's setPixel,
        // which takes in screen coordinates,
        // not world space coordinates (the translated ones)
        final int x0 = Math.max(tx + getLeft(), x);
        final int x1 = Math.min(tx + getRight(), x + image.getWidth());

        final int dx = Math.max(0, x0 - x);

        final int y0 = Math.max(ty + getTop(), y);
        final int y1 = Math.min(ty + getBottom(), y + image.getHeight());
        
        final int dy = Math.max(0, y0 - y);

        int pixelX = 0;
        int pixelY = 0;
        for (int yp = y0; yp < y1; yp++) {
            for (int xp = x0; xp < x1; xp++) {
                setPixel(x0 + pixelX, y0 + pixelY, image.getPixel(dx + pixelX, dy + pixelY));
                pixelX++;
            }
            pixelX = 0;
            pixelY++;
        }
    }

    /** Renders the given screen on top of the current one
     * only sampling the pixels within the given left, right, top and bottom bounds.
     * 
     * @param screen the screen to render
     * @param x the x position where to render the screen
     * @param y the y position where to render the screen
     * @param left the left bound from which to start rendering the screen
     * @param right the right bound from which to end rendering the screen
     * @param top the top bound from which to start rendering the screen
     * @param bottom the bottom bound from which to end rendering the screen
     **/
    public void overlay(Screen screen, int x, int y, int left, int right, int top, int bottom) {
        // invalid sample coordinates
        if (left < 0 || right >= screen.getWidth()
            || top < 0 || bottom >= screen.getHeight()) {
            return;
        }

        int pixelX = 0;
        int pixelY = 0;
        for (int yp = top; yp < bottom; yp++) {
            for (int xp = left; xp < right; xp++) {
                setPixel(x + pixelX, y + pixelY, screen.getPixel(xp, yp));
                pixelX++;
            }
            pixelX = 0;
            pixelY++;
        }
    }

    // UTILITY, CHECKS & DEBUG

    /** Returns true if the given coordinates are inside the screen, false if they are outside (does not take translation into account) **/
    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Returns true if the given coordinates are outside the screen, false if they are inside (does not take translation into account) **/
    public boolean isOutside(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    private boolean isBoundingBoxOutside(int x, int y, int xRadius, int yRadius) {
        boolean outsideCanvas = false;
        
        if (x < left && y < top) outsideCanvas = x + xRadius < left && y + yRadius < top;
        else if (x > right && y < top) outsideCanvas = x - xRadius < right && y + yRadius < top;
        else if (x < left && y > bottom) outsideCanvas = x + xRadius < left && y - yRadius > bottom;
        else if (x > right && y > bottom) outsideCanvas = x - xRadius >= right && y - yRadius > bottom;

        return outsideCanvas;
    }

    /**
     * Debug function:
     * draws a rectangle that covers the whole canvas
     * to test if translation math and left, right, top and bottom bounds
     * are correctly treated
    **/
    public void drawBounds() {
        outlines(Color.BLACK);
        fill(Color.LIGHT_BLUE);
        rectangle(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Debug function:
     * draws the coordinate system axes at the screen origin
     * (takes into account translation and rotation)
    **/
    public void drawAxes() {
        final int prevStrokeWeight = strokeWeight;
        stroke(1);
        // x axis
        vector(X_AXIS, 0, 0, Color.RED);
        // y axis
        vector(Y_AXIS, 0, 0, Color.GREEN);
        // origin
        point(0, 0, 2, Color.BLACK);
        point(0, 0, 1, Color.WHITE);

        stroke(prevStrokeWeight);
    }
}
