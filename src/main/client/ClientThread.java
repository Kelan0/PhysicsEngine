package main.client;

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

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Kelan
 */
public class ClientThread extends TickableThread
{
    private int windowWidth = 800;
    private int windowHeight = 600;
    private long window;
    private InputHandler inputHandler;
    private GLFWErrorCallback errorCallback;
    private GLFWWindowSizeCallback windowSizeCallback;

    private double updateDelta = 0.004;
    private boolean updateFrustum = true;
    private boolean antialiasingEnabled = true;
    private boolean drawGeometry = true;
    private boolean drawWireframe = false;
    private Vector4f wireframeColour = new Vector4f(0.08F, 0.08F, 0.08F, 1.0F);
    private InputContext inputContext = new InputContext(0);

    public ClientThread()
    {
        super("RENDER-THREAD");

        this.inputContext.registerAction("toggleDrawWireframe", () -> {
            setDrawWireframe(!doDrawWireframe());

            if (!doDrawWireframe())
                setDrawGeometry(true);

            return true;
        });
        this.inputContext.registerAction("toggleDrawGeometry", () -> {
            if (!doDrawWireframe())
                return false;

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

        errorCallback = GLFWErrorCallback.createPrint(System.err);
        windowSizeCallback = new GLFWWindowSizeCallback()
        {
            @Override
            public void invoke(long window, int w, int h)
            {
                glViewport(0, 0, windowWidth = w, windowHeight = h);
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
        window = glfwCreateWindow(800, 600, "hello :D", 0L, 0L);

        if (window <= 0)
            return false;

        inputHandler = new InputHandler(window);

        glfwSetWindowSizeCallback(window, windowSizeCallback);

        System.out.println("Finalizing window");
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(0);
        glfwShowWindow(window);

        Engine.getSceneGraph().init();
        return true;
    }

    @Override
    protected boolean update(double delta)
    {
        glfwSwapBuffers(window);

        inputHandler.addContext(inputContext);
        inputHandler.update();

        glClearColor(0.12F, 0.09F, 0.48F, 1.0F);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (glfwWindowShouldClose(window))
            Engine.getInstance().stop();

        Engine.getSceneGraph().render(delta);
        Engine.getGame().render(delta);

        return true;
    }

    @Override
    protected boolean destroy()
    {
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

        int screenWidth = videoMode == null ? windowWidth : videoMode.width();
        int screenHeight = videoMode == null ? windowHeight : videoMode.height();

        setWindowPos((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

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