package toolbox;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import toolbox.gfx.Color;
import toolbox.gfx.Screen;
import toolbox.utils.Console;

public abstract class Sketch {

    private int windowWidth, windowHeight;
    private int pixelScale;

    private boolean running;

    private int tickRate = 60;
    private int frameRate = 60;
    private int tps, fps;
	private int time = 0;

	protected JFrame jFrame;

	private Canvas canvas;
	private BufferedImage image;
	private BufferStrategy bs;
	private Graphics g;
	private int[] pixels;
	
	protected Input input;
	protected Screen screen;

	private boolean autoClear = true;

    // SKETCH FUNCTIONS
	/** Called once before the sketch window is opened */
    public void windowSetup() {

	}
	/** Called once after the sketch app starts */
    public abstract void setup();
	/** Called TICK_RATE times per second before render() is called */
    public abstract void update();
	/** Called FRAME_RATE times per second after update() is called (you can draw only here because screen.clear() will be called right before this if screen automatic clear is enabled) */
    public abstract void render();

    public void createCanvas(String title, int width, int height, int pixelScale, Color backgroundColor) {
		this.windowWidth = width * pixelScale;
        this.windowHeight = height * pixelScale;
        this.pixelScale = pixelScale;

        // start sketch app
        // BufferedImage icon = FileManager.createBufferedImage(ICON_PATH);

		canvas = new Canvas();
		canvas.setSize(windowWidth, windowHeight);
		// perform the first actual canvas clearing
		// (this affects the canvas background color)
		// and prevents flashing frames at the beginning
		canvas.setBackground(new java.awt.Color(
			backgroundColor.getRed(),
			backgroundColor.getGreen(),
			backgroundColor.getBlue()
		));
		
		jFrame = new JFrame(title);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLayout(new BorderLayout());
		jFrame.add(canvas, "Center");

		jFrame.setResizable(false);
		// jFrame.setIconImage(icon);

		windowSetup();
		
		// make the canvas size be the specified one
		// without taking the decoration bar (title, _, ⌷, x buttons) into account
		jFrame.pack();
		// center the JFrame in the computer display
		jFrame.setLocationRelativeTo(null);

		// rendering "pipeline" initialization
		screen = new Screen(width, height);
		// prepare the screen for having the given background color
		screen.background(backgroundColor);
		// perform the first screen clearing
		// (this won't affect the current canvas color,
		// but only set the frame buffer base color to the background color
		// meaning you will start drawing on a screen
		// that has the requested background color)
		screen.clear(backgroundColor.transparent(1)); // don't care about the alpha, otherwise you get a starting fade

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		// input handler initialization
		input = new Input(windowHeight, pixelScale, screen);
		canvas.addKeyListener(input);
		canvas.addMouseListener(input);
		canvas.addMouseMotionListener(input);
		canvas.addMouseWheelListener(input);

		// make the jFrame, therefore the canvas, visible only after setting all pixels
		// to the desired first clear/background color, this prevents flashing frames at the sketch start
		jFrame.setVisible(true);
		
		// starting sequence
		running = true;
		run();
    }

	/**
	 * Gently requests the sketch app to close
	**/
	public void close() {
		running = false;
	}

	// @Override
	public void run() {
		Console.println("Welcome to JToolBox!");

		try {
			setup();

            long now = System.currentTimeMillis();
            long lastUpdate = 0;
            long lastRender = 0;
            long lastRunInfoRefresh = now;
            
            double updateInterval = 1000.0 / tickRate;
            double renderInterval = 1000.0 / frameRate;
            double runInfoRefreshInterval = 1000.0;

            int ticks = 0;
            int frames = 0;

            while (running) {
                now = System.currentTimeMillis();

                if (now - lastUpdate >= updateInterval) {
                    tick();
                    ticks++;
                    lastUpdate = now;
                }

                if (now - lastRender >= renderInterval) {
                    draw();
                    frames++;
                    lastRender = now;
                }

                if (now - lastRunInfoRefresh >= runInfoRefreshInterval) {
                    tps = ticks;
                    fps = frames;
                    ticks = 0;
                    frames = 0;
                    lastRunInfoRefresh = now;
                }
            }
			// if running == false then close the window, thus closing the whole sketch app as the jFrame exit mode is EXIT_ON_CLOSE
			jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING));
        } catch (Exception e) {
			running = false;
            Console.error("Your sketch crashed and produced the following report:");
			e.printStackTrace();
            System.exit(1);
        }
    }

	private void tick() {
		time += 1;

		if (input.isKeyPressed(KeyEvent.VK_ESCAPE)) {
			running = false;
		}

		update();

		// this is called after the sketch update because otherwise the pressed and released
		// variables are updated before they can be detected by the sketch update method user implementation
		input.update();
	}

	private void draw() {
		bs = canvas.getBufferStrategy();
		if (bs == null) {
			canvas.createBufferStrategy(3);
			canvas.requestFocus();
			return;
		}

		if (autoClear) {
			screen.clear();
		}

		render();
		
		// perform the y-flip as the last thing before rendering the frame buffer
		int index, verticallyFlippedIndex;
		for (int y = 0; y < screen.getHeight(); y++) {
			for (int x = 0; x < screen.getWidth(); x++) {
				index = x + y * screen.getWidth();
				verticallyFlippedIndex = x + (screen.getHeight() - y - 1) * screen.getWidth();
				pixels[index] = screen.getPixels()[verticallyFlippedIndex];
			}
		}

		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWindowWidth(), getWindowHeight(), null);
		g.dispose();
		bs.show();
	}

    // SETTERS
	/** Sets the amount of update() calls per second **/
	public void setTickRate(int tickRate) {
		this.tickRate = tickRate;
	}

	/** Sets the amount of render() calls per second **/
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
	/**
     * Toggles automatic screen refresh (prevering frame accumulation), which is true by default
     * The screen will be cleared using the background color
    **/
    public void autoClear(boolean toggle) {
        autoClear = toggle;
    }

    // GETTERS
    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

	public int getPixelScale() {
        return pixelScale;
    }

	/** Returns the current Ticks (Updates) per Second count **/
	public int getTPS() {
		return tps;
	}

	/** Returns the current Frames per Second count **/
	public int getFPS() {
		return fps;
	}

	/** Returns a time integer indicating how many tick calls have been made **/
	public int getTime() {
		return time;
	}
}
