package main.core.input;

import java.util.*;

/**
 * @author Kelan
 */
public class InputContext
{
    public static final Comparator<InputContext> comparator = Comparator.comparingInt(o -> o.priority);

    private Map<String, InputAction> actionMap = new HashMap<>(); // A map of inputs to actions. These actions are executed once for the input.
    private Map<String, InputState> stateMap = new HashMap<>(); // A map of inputs to states. These states continue until the input stops.
    private Map<String, InputRange> rangeMap = new HashMap<>(); // A map of inputs to ranges. The range may be normalized and corresponds to a continuous value, such as mouse or joystick movement.
    private int priority;

    public InputContext(int priority)
    {
        this.priority = priority;
    }

    public InputContext()
    {
        this(0);
    }

    public void registerAction(String command, InputAction inputAction)
    {
        this.actionMap.put(command, inputAction);
    }

    public void registerState(String command, InputState inputState)
    {
        this.stateMap.put(command, inputState);
    }

    public void registerRange(String command, InputRange inputRange)
    {
        this.rangeMap.put(command, inputRange);
    }

    public InputAction getAction(String command)
    {
        return this.actionMap.get(command);
    }

    public InputState getState(String command)
    {
        return this.stateMap.get(command);
    }

    public InputRange getRange(String command)
    {
        return this.rangeMap.get(command);
    }

    public int getPriority()
    {
        return priority;
    }

    public interface InputAction
    {
        boolean run();
    }

    public interface InputState
    {
        boolean run();
    }

    public interface InputRange
    {
        void run(float value);

        default float getMin()
        {
            return -1.0F;
        }

        default float gteMax()
        {
            return +1.0F;
        }
    }
}
