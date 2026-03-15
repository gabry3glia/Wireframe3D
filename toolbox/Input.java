package toolbox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import toolbox.gfx.Screen;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public static final int LEFT_BUTTON = 1;
    public static final int MIDDLE_BUTTON = 2;
    public static final int RIGHT_BUTTON = 3;

    private final int NUM_KEYS = 256;
    private final int NUM_BUTTONS = 5;

    private final int windowHeight;
    private final int pixelScale;
    private final Screen screen;

    // there here are used to avoid checking all the keys and buttons everytime
    private ArrayList<Integer> keysToCheck;
    private ArrayList<Integer> buttonsToCheck;

    private boolean[] downKeys;
    private boolean[] downButtons;

    private boolean[] pressedKeys;
    private boolean[] pressedButtons;

    private boolean[] releasedKeys;
    private boolean[] releasedButtons;

    private int mouseX;
    private int mouseY;
    private int mouseDeltaX;
    private int mouseDeltaY;
    private int mousePreviousX;
    private int mousePreviousY;

    private int mouseScroll;

    public Input(int windowHeight, int pixelScale, Screen screen) {
        this.windowHeight = windowHeight;
        this.pixelScale = pixelScale;
        this.screen = screen;

        keysToCheck = new ArrayList<Integer>();
        buttonsToCheck = new ArrayList<Integer>();

        downKeys = new boolean[NUM_KEYS];
        downButtons = new boolean[NUM_BUTTONS];

        pressedKeys = new boolean[NUM_KEYS];
        pressedButtons = new boolean[NUM_BUTTONS];
        
        releasedKeys = new boolean[NUM_KEYS];
        releasedButtons = new boolean[NUM_BUTTONS];

        mousePreviousX = 0;
        mousePreviousY = 0;
    }

    // KEYBOARD
    /**
     * Returns true as soon as the key is pressed, then returns false
    **/
    public boolean isKeyPressed(int key) {
        if (key < 0 || key >= pressedKeys.length) return false;
        return pressedKeys[key];
    }

    /**
     * Returns true if the key is being held down, otherwise returns false
    **/
    public boolean isKeyDown(int key) {
        if (key < 0 || key >= downKeys.length) return false;
        return downKeys[key];
    }

    /**
     * Returns true as soon as the key is released, then returns false
    **/
    public boolean isKeyReleased(int key) {
        if (key < 0 || key >= releasedKeys.length) return false;
        return releasedKeys[key];
    }

    // MOUSE
    /**
     * Returns true as soon as the button is clicked, then returns false
    **/
    public boolean isButtonPressed(int button) {
        if (button < 0 || button >= pressedButtons.length) return false;
        return pressedButtons[button];
    }

    /**
     * Returns true if the button is being held down, otherwise returns false
    **/
    public boolean isButtonDown(int button) {
        if (button < 0 || button >= downButtons.length) return false;
        return downButtons[button];
    }

    /**
     * Returns true as soon as the button is released, then returns false
    **/
    public boolean isButtonReleased(int button) {
        if (button < 0 || button >= releasedButtons.length) return false;
        return releasedButtons[button];
    }

    /**
     * Returns the mouse x position in window space (does not take into account the pixelScale)
    **/
    public int getMouseX() {
        return mouseX;
    }

    /**
     * Returns the mouse y position in window space (does not take into account the pixelScale)
    **/
    public int getMouseY() {
        // the screen is y-down by default, and I'm using y-up instead
        return windowHeight - 1 - mouseY;
    }

    /**
     * Returns the mouse x position in screen (canvas) space (also takes into account the pixelScale and screen translation)
    **/
    public int getMouseCanvasX() {
        return getMouseX() / pixelScale + screen.getLeft() - screen.getLeftPadding();
    }

    /**
     * Returns the mouse y position in screen (canvas) space (also takes into account the pixelScale and screen translation)
    **/
    public int getMouseCanvasY() {
        return getMouseY() / pixelScale + screen.getTop() - screen.getTopPadding();
    }

    /**
     * Returns the difference between the current and the previous tick mouse x position in canvas space
    **/
    public int getMouseDeltaX() {
        return mouseDeltaX;
    }

    /**
     * Returns the difference between the current and the previous tick mouse y position in canvas space
    **/
    public int getMouseDeltaY() {
        // it's negated because the screen is y-down by default, and I'm using y-up instead
        return -mouseDeltaY;
    }

    /**
     * Returns the scroll amount of the mouse wheen
     * positive when scrolling towards the user
     * negative when scrolling away
     * 0 when not scrolling
    **/
    public int getMouseScroll() {
        return mouseScroll;
    }

    // INPUT HANDLERS

    /** Updates the input variables. It's an internal function you should not call **/
    public void update() {
        // update keyboard keys
        for (int key : keysToCheck) {
            if (downKeys[key]) {
                if (pressedKeys[key]) pressedKeys[key] = false;
            } else {
                if (releasedKeys[key]) releasedKeys[key] = false;
            }
        }
        keysToCheck.clear();

        // update mouse buttons
        for (int button : buttonsToCheck) {
            if (downButtons[button]) {
                if (pressedButtons[button]) pressedButtons[button] = false;
            } else {
                if (releasedButtons[button]) releasedButtons[button] = false;
            }
        }
        buttonsToCheck.clear();

        // update the mouse scroll
        mouseScroll = 0;
    }

    // utils
    private void toggleKey(int key, boolean state) {
        keysToCheck.add(key);
        if (state) {
            downKeys[key] = true;
            pressedKeys[key] = true;
            releasedKeys[key] = false;
        } else {
            downKeys[key] = false;
            pressedKeys[key] = false;
            releasedKeys[key] = true;
        }
    }

    private void toggleButton(int button, boolean state) {
        buttonsToCheck.add(button);
        if (state) {
            downButtons[button] = true;
            pressedButtons[button] = true;
            releasedButtons[button] = false;
        } else {
            downButtons[button] = false;
            pressedButtons[button] = false;
            releasedButtons[button] = true;
        }
    }

    // KEYBOARD
    @Override
    public void keyPressed(KeyEvent e) {
        toggleKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        toggleKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // MOUSE
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}


    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        toggleButton(e.getButton(), true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        toggleButton(e.getButton(), false);
    }

    // MOUSE MOTION
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mouseDeltaX = e.getX() - mousePreviousX;
        mouseDeltaY = e.getY() - mousePreviousY;
        mousePreviousX = e.getX();
        mousePreviousY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mouseDeltaX = e.getX() - mousePreviousX;
        mouseDeltaY = e.getY() - mousePreviousY;
        mousePreviousX = e.getX();
        mousePreviousY = e.getY();
    }

    // MOUSE WHEEL SCROLL
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseScroll = e.getWheelRotation();
    }
}