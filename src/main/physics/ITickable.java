package main.physics;

/**
 * @author Kelan
 */
public interface ITickable
{
    void init();

    void update(double delta);

    default boolean doTick()
    {
        return true;
    }
}
