package main;

import handler.KeyHandler;
import handler.MouseHandler;
import renderer.Camera;
import handler.CollisionHandler;
import renderer.Renderer;
import renderer.World;

import javax.swing.*;
import java.awt.*;

public class RendererPanel extends JPanel implements Runnable{

    public final int singlePixelSize = 16;

    public final int totalScreenCols = 80;
    public final int totalScreenRows = 45;

    public final int screenWidth = totalScreenCols * singlePixelSize;
    public final int screenHeight = totalScreenRows * singlePixelSize;

    public final int framesPerSecond = 100;
    public final long frameInterval = 1000000000 / framesPerSecond;
    public long workTime;

    public long startTime;
    public int frames;

    public Thread rendererThread;
    public boolean rendererRunning = true;

    public Renderer renderer;
    public World world;
    public KeyHandler keyHandler;
    public Camera camera;
    public MouseHandler mouseHandler;
    public Scripts scripts;
    public CollisionHandler collisionHandler;

    RendererPanel() {

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        keyHandler = new KeyHandler();
        this.addKeyListener(keyHandler);

        mouseHandler = new MouseHandler(this);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);

        collisionHandler = new CollisionHandler();

        scripts = new Scripts(this, 30000, 30000, 30000);

        renderer = new Renderer(this);
        world = new World(50, 50, 1000);
        camera = new Camera(this);

        rendererThread = new Thread(this);
        rendererThread.start();
    }

    @Override
    public void run() {

        workTime = System.nanoTime() + frameInterval;
        startTime = System.nanoTime();

        while(rendererRunning) {

            if(System.nanoTime() - startTime >= 1000000000) {
                System.out.println("FPS: " + frames);
                frames = 0;
                startTime = System.nanoTime();
            }

            if(workTime - System.nanoTime() <= 0) {

                update();

                repaint();

                frames++;
                workTime = System.nanoTime() + frameInterval;
            }
        }
    }

    public void update() {

        if(keyHandler.ePressed) {
            mouseHandler.isLocked = false;
        }

        renderer.update();
        camera.update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;

        g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // Define your smooth sky gradient colors
        // Top: Deep atmospheric/space blue
        Color topColor = new Color(12, 24, 48);
        // Bottom: Soft glowing horizon blue
        Color horizonColor = new Color(115, 165, 215);

        // Create the vertical gradient paint
        GradientPaint skyGradient = new GradientPaint(
                0, 0, topColor,             // Start at the very top (y = 0)
                0, height, horizonColor     // End at the bottom of the window (y = height)
        );

        g2D.setPaint(skyGradient);
        g2D.fillRect(0, 0, width, height);

        renderer.draw(g2D);

        g2D.dispose();
    }
}
