package main.physics;

import main.core.Engine;
import main.core.TickableThread;

/**
 * @author Kelan
 */
public class PhysicsThread extends TickableThread
{
    private double updateDelta = 0.01;

    public PhysicsThread()
    {
        super("PHYSICS-THREAD");
    }

    @Override
    protected boolean init()
    {
        return true;
    }

    @Override
    protected boolean update(double delta)
    {
        Engine.getSceneGraph().update(delta);
        return true;
    }

    @Override
    protected boolean destroy()
    {
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

    public void setUpdateDelta(double updateDelta)
    {
        synchronized (getLock())
        {
            this.updateDelta = updateDelta;
        }
    }
}
