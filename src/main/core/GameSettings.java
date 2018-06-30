package main.core;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Kelan
 */
public class GameSettings
{
    private Map<Integer, String> keyCommands = new HashMap<>();
    private Map<Integer, String> mouseCommands = new HashMap<>();

    private int maxFps = 300;
    private int maxTps = 64;
    private int windowWidth = 1600;
    private int windowHeight = 900;
    private String windowTitle = "Test Window";

    public GameSettings()
    {
        this.registerKeyCommand(GLFW_KEY_F1, "toggleDrawWireframe");
        this.registerKeyCommand(GLFW_KEY_F2, "toggleDrawGeometry");
        this.registerKeyCommand(GLFW_KEY_F3, "toggleAntialiasing");
        this.registerKeyCommand(GLFW_KEY_W, "forward");
        this.registerKeyCommand(GLFW_KEY_A, "left");
        this.registerKeyCommand(GLFW_KEY_S, "backward");
        this.registerKeyCommand(GLFW_KEY_D, "right");
        this.registerKeyCommand(GLFW_KEY_SPACE, "up");
        this.registerKeyCommand(GLFW_KEY_LEFT_SHIFT, "down");
        this.registerKeyCommand(GLFW_KEY_RIGHT_SHIFT, "down");
    }

    public void registerKeyCommand(int key, String command)
    {
        this.keyCommands.put(key, command);
    }

    public String getKeyCommand(int key)
    {
        return this.keyCommands.get(Integer.valueOf(key));
    }

    public int getMaxFps()
    {
        return maxFps;
    }

    public int getMaxTps()
    {
        return maxTps;
    }

    public int getWindowWidth()
    {
        return windowWidth;
    }

    public int getWindowHeight()
    {
        return windowHeight;
    }

    public String getWindowTitle()
    {
        return windowTitle;
    }

    public void setMaxFps(int maxFps)
    {
        this.maxFps = maxFps;
    }

    public void setMaxTps(int maxTps)
    {
        this.maxTps = maxTps;
    }

    public void setWindowSize(int windowWidth, int windowHeight)
    {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void setWindowTitle(String windowTitle)
    {
        this.windowTitle = windowTitle;
    }

    public void apply()
    {
        Engine.getClientThread().setWindowSize(windowWidth, windowHeight);
        Engine.getClientThread().setWindowTitle(windowTitle);
        Engine.getClientThread().setUpdateDelta(1.0 / getMaxFps());
        Engine.getPhysicsThread().setUpdateDelta(1.0 / getMaxTps());

        Engine.getClientThread().setWindowCentered();
    }
}
