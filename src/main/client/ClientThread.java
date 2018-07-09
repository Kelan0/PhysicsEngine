package main.client;

import main.client.rendering.screen.FrameBuffer;
import main.client.rendering.screen.ScreenRenderer;
import main.core.Engine;
import main.core.TickableThread;
import main.core.input.InputContext;
import main.core.input.InputHandler;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.util.vector.Vector4f;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Kelan
 */
public class ClientThread extends TickableThread
{
    private long window;

    private InputContext inputContext = new InputContext(0);
    private InputHandler inputHandler;
    private GLFWErrorCallback errorCallback;
    private GLFWWindowSizeCallback windowSizeCallback;

    private double updateDelta = 0.004;
    private boolean updateFrustum = true;
    private boolean antialiasingEnabled = true;
    private boolean drawGeometry = true;
    private boolean drawWireframe = false;
    private Vector4f wireframeColour = new Vector4f(0.9F, 0.9F, 0.9F, 1.0F);

    private ScreenRenderer screenRenderer;
    private int windowWidth = 800;
    private int windowHeight = 600;

    public ClientThread()
    {
        super("RENDER-THREAD");

        this.inputContext.registerAction("toggleDrawWireframe", () -> {
            setDrawWireframe(!doDrawWireframe());

            wireframeColour = new Vector4f(0.08F, 0.08F, 0.08F, 1.0F);
            if (!doDrawWireframe())
                setDrawGeometry(true);

            return true;
        });
        this.inputContext.registerAction("toggleDrawGeometry", () -> {
            if (!doDrawWireframe())
                return false;

            wireframeColour = new Vector4f(0.9F, 0.9F, 0.9F, 1.0F);
            setDrawGeometry(!doDrawGeometry());
            return true;
        });
        this.inputContext.registerAction("toggleAntialiasing", () -> {
            this.setAntialiasingEnabled(!this.isAntialiasingEnabled());
            return true;
        });
    }

    @Override
    protected boolean init()
    {
        System.out.println("Initializing render engine");

        screenRenderer = new ScreenRenderer();

        errorCallback = GLFWErrorCallback.createPrint(System.err);
        windowSizeCallback = new GLFWWindowSizeCallback()
        {
            @Override
            public void invoke(long window, int w, int h)
            {
                windowWidth = w;
                windowHeight = h;
                screenRenderer.onScreenResized();
            }
        };

        glfwSetErrorCallback(errorCallback);

        System.out.println("Initializing GLFW");
        if (!glfwInit())
            return false;

        System.out.println("Setting GLFW window hints");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // compat profile to allow immediate mode
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        System.out.println("Creating window");
        window = glfwCreateWindow(windowWidth, windowHeight, "hello :D", 0L, 0L);

        if (window <= 0)
            return false;

        inputHandler = new InputHandler(window);

        glfwSetWindowSizeCallback(window, windowSizeCallback);

        System.out.println("Finalizing window");
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwShowWindow(window);

        screenRenderer.init();
        Engine.getSceneGraph().init();

        setWindowSize(windowWidth, windowHeight);

        return true;
    }


    private List<Double> frames = new ArrayList<>();
    private int counter = 0;
    private long a;
    private DecimalFormat df = new DecimalFormat("0.000");

    @Override
    protected boolean update(double delta)
    {
        frames.add(1.0 / delta);
        counter++;
        if (System.nanoTime() - a > 1000000000)
        {
            long t0 = System.nanoTime();
            int indexCount = (int) (frames.size() * 0.01);
            int[] lowIndices = new int[indexCount];
            int[] highIndices = new int[indexCount];

            for (int i = 0; i < indexCount; i++)
                lowIndices[i] = highIndices[i] = -1;

            for (int i = 0; i < indexCount; i++)
            {
                int curLowIndex = lowIndices[i];
                int curHighIndex = highIndices[i];

                for (int j = 0; j < frames.size(); j++)
                {
                    if (curLowIndex == -1 || frames.get(j) < frames.get(curLowIndex))
                    {
                        boolean flag = true;

                        if (curLowIndex != -1)
                        {
                            for (int index : lowIndices)
                            {
                                if (index == j)
                                {
                                    flag = false;
                                    break;
                                }
                            }
                        }

                        if (flag)
                            curLowIndex = j;
                    }
                    if (curHighIndex == -1 || frames.get(j) > frames.get(curHighIndex))
                    {
                        boolean flag = true;

                        if (curHighIndex != -1)
                        {
                            for (int index : highIndices)
                            {
                                if (index == j)
                                {
                                    flag = false;
                                    break;
                                }
                            }
                        }

                        if (flag)
                            curHighIndex = j;
                    }
                }

                lowIndices[i] = curLowIndex;
                highIndices[i] = curHighIndex;
            }

            double avg = 0.0;
            double p1l = 0.0;
            double p1h = 0.0;

            for (int i = frames.size() - counter; i < frames.size(); i++)
            {
                double frame = frames.get(i);
                avg += frame;
            }

            for (int i = 0; i < indexCount; i++)
            {
                p1l += frames.get(lowIndices[i]);
                p1h += frames.get(highIndices[i]);
            }

            avg /= counter;
            p1l /= indexCount;
            p1h /= indexCount;

            while (frames.size() > 10000)
                frames.remove(0);


            a = System.nanoTime();
            setWindowTitle("Window: avg fps = " + df.format(avg) + ", 1% low = " + df.format(p1l) + ", 1% high = " + df.format(p1h));

            System.out.println("Took " + (a - t0) / 1000000.0 + "ms to calculate 1% low/high for " + frames.size() + " frames");
            counter = 0;
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwSwapBuffers(window);

        inputHandler.addContext(inputContext);
        inputHandler.update();

        if (glfwWindowShouldClose(window))
            Engine.getInstance().stop();

        screenRenderer.bindScreenBuffer();

        Engine.getSceneGraph().render(delta);
        Engine.getGame().render(delta);

        FrameBuffer.unbind();

        screenRenderer.render(delta);
        return true;
    }

    @Override
    protected boolean dispose()
    {
        screenRenderer.dispose();
        Engine.getSceneGraph().dispose();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();

        errorCallback.free();

        return true;
    }

    @Override
    public double getUpdateDelta()
    {
        synchronized (getLock())
        {
            return updateDelta;
        }
    }

    public int getWindowWidth()
    {
        return windowWidth;
    }

    public int getWindowHeight()
    {
        return windowHeight;
    }

    public float getWindowAspectRatio()
    {
        return (float) getWindowWidth() / (float) getWindowHeight();
    }

    public boolean doUpdateFrustum()
    {
        return updateFrustum;
    }

    public boolean isAntialiasingEnabled()
    {
        return antialiasingEnabled;
    }

    public boolean doDrawGeometry()
    {
        return drawGeometry;
    }

    public boolean doDrawWireframe()
    {
        return drawWireframe;
    }

    public Vector4f getWireframeColour()
    {
        return wireframeColour;
    }

    public InputHandler getInputHandler()
    {
        return inputHandler;
    }

    public ScreenRenderer getScreenRenderer()
    {
        return screenRenderer;
    }

    public ClientThread setUpdateDelta(double updateDelta)
    {
        synchronized (getLock())
        {
            this.updateDelta = updateDelta;
        }

        return this;
    }

    public ClientThread setWindowSize(int w, int h)
    {
        glfwSetWindowSize(window, w, h);
        return this;
    }

    public ClientThread setWindowTitle(CharSequence title)
    {
        glfwSetWindowTitle(window, title);
        return this;
    }

    public ClientThread setWindowPos(int x, int y)
    {
        glfwSetWindowPos(window, x, y);
        return this;
    }

    public ClientThread setWindowCentered()
    {
        long monitor = glfwGetWindowMonitor(window);

        GLFWVidMode videoMode = glfwGetVideoMode(monitor > 0L ? monitor : glfwGetPrimaryMonitor());

        int screenWidth = videoMode == null ? getWindowWidth() : videoMode.width();
        int screenHeight = videoMode == null ? getWindowHeight() : videoMode.height();

        setWindowPos((screenWidth - getWindowWidth()) / 2, (screenHeight - getWindowHeight()) / 2);

        return this;
    }

    public void setUpdateFrustum(boolean updateFrustum)
    {
        this.updateFrustum = updateFrustum;
    }

    public void setAntialiasingEnabled(boolean antialiasingEnabled)
    {
        this.antialiasingEnabled = antialiasingEnabled;
    }

    public void setDrawGeometry(boolean drawGeometry)
    {
        this.drawGeometry = drawGeometry;
    }

    public void setDrawWireframe(boolean drawWireframe)
    {
        this.drawWireframe = drawWireframe;
    }

    public void setWireframeColour(Vector4f wireframeColour)
    {
        this.wireframeColour = wireframeColour;
    }

    public class Keyboard
    {
        private GLFWKeyCallback keyCallback = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                keysDown[key] = action != GLFW_RELEASE;
                keysPress[key] = action == GLFW_PRESS;
                keysRelease[key] = action == GLFW_RELEASE;
            }
        };

        private boolean[] keysDown = new boolean[GLFW_KEY_LAST + 1];
        private boolean[] keysPress = new boolean[GLFW_KEY_LAST + 1];
        private boolean[] keysRelease = new boolean[GLFW_KEY_LAST + 1];

        public boolean keyDown(int key)
        {
            if (key >= 0 && key <= GLFW_KEY_LAST)
            {
                return this.keysDown[key];
            }

            return false;
        }

        public boolean keyPressed(int key)
        {
            if (key >= 0 && key <= GLFW_KEY_LAST)
            {
                return this.keysPress[key];
            }

            return false;
        }

        public boolean keysReleased(int key)
        {
            if (key >= 0 && key <= GLFW_KEY_LAST)
            {
                return this.keysRelease[key];
            }

            return false;
        }

        private void update(double delta)
        {
            for (int i = 0; i <= GLFW_KEY_LAST; i++)
            {
                this.keysPress[i] = false;
                this.keysRelease[i] = false;
            }
        }

        private void createCallbacks()
        {
            glfwSetKeyCallback(window, this.keyCallback);
        }

        private void destroyCallbacks()
        {
            this.keyCallback.free();
        }
    }
}
