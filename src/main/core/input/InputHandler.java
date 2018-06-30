package main.core.input;

import main.core.Engine;
import main.core.GameSettings;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Kelan
 */
public class InputHandler
{
    private GLFWKeyCallback keyCallback = new GLFWKeyCallback()
    {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods)
        {
//            System.out.println("Keyboard " + key + " (" + (char) key + ") " + (action == GLFW_PRESS ? "pressed" : action == GLFW_RELEASE ? "released" : action == GLFW_REPEAT ? "held" : "unknown"));
            keyState[key] = action;
            keyDown[key] = action != GLFW_RELEASE;
        }
    };

    private GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback()
    {
        @Override
        public void invoke(long window, int button, int action, int mods)
        {
//            System.out.println("Mouse button " + button + (action == GLFW_PRESS ? "pressed" : action == GLFW_RELEASE ? "released" : action == GLFW_REPEAT ? "held" : "unknown"));
            mouseState[button] = action;
            mouseDown[button] = action != GLFW_RELEASE;
        }
    };

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback()
    {
        @Override
        public void invoke(long window, double xpos, double ypos)
        {
            GameSettings settings = Engine.getGameSettings();
            mousePosition.set((float) xpos - settings.getWindowWidth() * 0.5F, (float) ypos - settings.getWindowHeight() * 0.5F);
        }
    };

    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback()
    {
        @Override
        public void invoke(long window, double xoffset, double yoffset)
        {
            mouseScroll.set((float) xoffset, (float) yoffset);
        }
    };

    private int[] keyState = new int[GLFW_KEY_LAST];
    private boolean[] keyDown = new boolean[GLFW_KEY_LAST];

    private int[] mouseState = new int[GLFW_MOUSE_BUTTON_LAST];
    private boolean[] mouseDown = new boolean[GLFW_MOUSE_BUTTON_LAST];

    private boolean mouseGrabbed = false;

    private Vector2f mouseScroll = new Vector2f();
    private Vector2f mouseVelocity = new Vector2f();
    private Vector2f mousePosition = new Vector2f();
    private Vector2f mouseDragStart;
    private Vector2f mouseDragEnd;

    private List<InputContext> contexts = new ArrayList<>();

    private long windowHandle;

    public InputHandler(long windowHandle)
    {
        this.windowHandle = windowHandle;

        glfwSetKeyCallback(this.windowHandle, this.keyCallback);
        glfwSetMouseButtonCallback(this.windowHandle, this.mouseButtonCallback);
        glfwSetCursorPosCallback(this.windowHandle, this.cursorPosCallback);
        glfwSetScrollCallback(this.windowHandle, this.scrollCallback);
    }

    public void update()
    {
        Vector2f lastMousePosition = getMousePosition();

        resetStates();
        glfwPollEvents();
        updateMouse(lastMousePosition);
        updateContexts();
    }

    private void resetStates()
    {
        this.mouseScroll.set(0.0F, 0.0F);

        for (int i = 0; i < GLFW_KEY_LAST; i++)
            this.keyState[i] = -1;

        for (int i = 0; i < GLFW_MOUSE_BUTTON_LAST; i++)
            this.mouseState[i] = -1;

        if (this.mouseReleased(GLFW_MOUSE_BUTTON_1))
        {
            this.mouseDragStart = null;
            this.mouseDragEnd = null;
        }
    }

    private void updateMouse(Vector2f lastMousePosition)
    {
        if (mouseDown(GLFW_MOUSE_BUTTON_1))
        {
            if (this.mouseDragStart == null)
                this.mouseDragStart = getMousePosition();

            this.mouseDragEnd = getMousePosition();
        }

        if (this.isMouseGrabbed())
        {
            this.mouseVelocity.set(this.getMousePosition());
            this.setMousePosition(new Vector2f(0.0F, 0.0F));

            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else
        {
            this.mouseVelocity.set(Vector2f.sub(this.getMousePosition(), lastMousePosition, null));
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        if (this.keyPressed(GLFW_KEY_ESCAPE))
            this.setMouseGrabbed(!this.isMouseGrabbed());
    }

    private void updateContexts()
    {
        this.contexts.sort(InputContext.comparator);

        boolean[] keys = new boolean[GLFW_KEY_LAST];

        for (InputContext context : this.contexts)
        {
            for (int i = 0; i < GLFW_KEY_LAST; i++)
            {
                if (keys[i])
                    continue;

                String command = Engine.getGameSettings().getKeyCommand(i);

                if (command != null && !command.isEmpty())
                {
                    InputContext.InputAction action = context.getAction(command);
                    InputContext.InputState state = context.getState(command);
                    InputContext.InputRange range = context.getRange(command);

                    if (action != null && keyPressed(i))
                        keys[i] = action.run();

                    if (state != null && keyDown(i))
                        keys[i] = state.run();
                }
            }
        }

        this.contexts.clear();
    }

    public void addContext(InputContext context)
    {
        this.contexts.add(context);
    }

    public boolean keyPressed(int key)
    {
        return keyState[key] >= 0 && keyState[key] == GLFW_PRESS;
    }

    public boolean keyDown(int key)
    {
        return keyDown[key];
    }

    public boolean keyReleased(int key)
    {
        return keyState[key] >= 0 && keyState[key] == GLFW_RELEASE;
    }

    public boolean mousePressed(int button)
    {
        return mouseState[button] >= 0 && mouseState[button] == GLFW_PRESS;
    }

    public boolean mouseDown(int button)
    {
        return mouseDown[button];
    }

    public boolean mouseReleased(int button)
    {
        return mouseState[button] >= 0 && mouseState[button] == GLFW_RELEASE;
    }

    public void setMousePosition(Vector2f position)
    {
        float xpos = position.x + Engine.getGameSettings().getWindowWidth() * 0.5F;
        float ypos = position.y + Engine.getGameSettings().getWindowHeight() * 0.5F;

        glfwSetCursorPos(windowHandle, xpos, ypos);
        this.mousePosition.set(position);
    }

    public Vector2f getMousePosition()
    {
        return new Vector2f(mousePosition);
    }

    public Vector2f getMouseScroll()
    {
        return new Vector2f(mouseScroll);
    }

    public Vector2f getMouseVelocity()
    {
        return new Vector2f(mouseVelocity);
    }

    public Vector2f getMouseDragStart()
    {
        if (mouseDragStart != null)
        {
            return new Vector2f(mouseDragStart);
        }

        return null;
    }

    public Vector2f getMouseDragEnd()
    {
        if (mouseDragEnd != null)
        {
            return new Vector2f(mouseDragEnd);
        }

        return null;
    }

    public boolean isMouseGrabbed()
    {
        return mouseGrabbed;
    }

    public void setMouseGrabbed(boolean mouseGrabbed)
    {
        if (this.mouseGrabbed != mouseGrabbed)
        {
            this.setMousePosition(new Vector2f(0.0F, 0.0F));
            this.update();
        }

        this.mouseGrabbed = mouseGrabbed;
    }
}
