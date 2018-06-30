package main.core;

import main.core.Engine;
import main.core.LogHandler;

/**
 * @author Kelan
 */
public abstract class TickableThread implements Runnable
{
    private final Object lock = new Object();

    private boolean initialized;
    private boolean running;
    private boolean stopped;
    private Thread thread;

    public TickableThread(String name)
    {
        this.thread = new Thread(this, name);
    }

    @Override
    public void run()
    {
        synchronized (getLock())
        {
            if (!initialized)
            {
                LogHandler.getLogger().info("Initializing");
                if (initialized = init())
                {
                    LogHandler.getLogger().info("Successfully initialized");
                    running = true;
                    getLock().notifyAll();
                } else
                {
                    LogHandler.getLogger().severe("Failed to initialize");
                    stopped = true;
                    getLock().notifyAll();
                }
            }
        }

        LogHandler.getLogger().info("Running");

        long lastTime = System.nanoTime();
        long lastTick = System.nanoTime();
        double partialTicks = 0.0;

        while (isRunning())
        {
            if (isStopped())
                break;

            long now = System.nanoTime();
            partialTicks += (now - lastTime) / Math.max(1.0, 1000000000.0 * getUpdateDelta());
            lastTime = now;

            if (partialTicks >= 1.0)
            {
                synchronized (getLock())
                {
                    if (!update((now - lastTick) / 1000000000.0))
                        Engine.getInstance().stop();

                    lastTick = now;
                    partialTicks--;
                }
            }

            try
            {
                Thread.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        synchronized (getLock())
        {
            destroy();
            running = false;
            getLock().notifyAll();
        }

        LogHandler.getLogger().info("Finished executing thread");
    }

    public final boolean start(long timeout)
    {
        LogHandler.getLogger().info("Starting " + thread.getName());
        this.thread.start();

        long start = System.currentTimeMillis();
        synchronized (getLock())
        {
            while (!isRunning() && !isStopped())
            {
                try
                {
                    getLock().wait(timeout);
                    if (timeout > 0 && System.currentTimeMillis() - start > timeout)
                    {
                        stop(timeout);
                        return false;
                    }
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    public final boolean stop(long timeout)
    {
        synchronized (getLock())
        {
            stopped = true;
        }

        if (timeout >= 0)
        {
            long start = System.currentTimeMillis();
            synchronized (getLock())
            {
                while (isRunning())
                {
                    try
                    {
                        getLock().wait(timeout);

                        if (timeout > 0 && System.currentTimeMillis() - start > timeout)
                        {
                            running = false;
                            Thread.sleep(100);
                            thread.interrupt();
                            return false;
                        }
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        thread.interrupt();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected abstract boolean init();

    protected abstract boolean update(double delta);

    protected abstract boolean destroy();

    public abstract double getUpdateDelta();

    public final Object getLock()
    {
        return lock;
    }

    public final boolean isInitialized()
    {
        synchronized (getLock())
        {
            return initialized;
        }
    }

    public final boolean isRunning()
    {
        synchronized (getLock())
        {
            return running;
        }
    }

    private final boolean isStopped()
    {
        synchronized (getLock())
        {
            return stopped;
        }
    }
}
