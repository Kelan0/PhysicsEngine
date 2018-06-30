package main.core;

import main.client.ClientThread;
import main.core.input.InputHandler;
import main.core.scene.SceneGraph;
import main.physics.PhysicsThread;

/**
 * @author Kelan
 */
public class Engine
{
    private static final Engine instance = new Engine();

    private final Object lock = new Object();

    private GameHandler game;
    private ClientThread clientThread;
    private PhysicsThread physicsThread;

    public Engine()
    {
        this.clientThread = new ClientThread();
        this.physicsThread = new PhysicsThread();
    }

    public void init(GameHandler game)
    {
        this.game = game;

        // Creates a call to the logger, causing the static initializer to get called. Any
        // regular println before this would otherwise not be redirected.
        LogHandler.getLogger();

        synchronized (getLock())
        {
            clientThread.start(10000);
            physicsThread.start(10000);
        }

        game.getGameSettings().apply();
    }

    public void stop()
    {
        LogHandler.getLogger().info("Stopping engine");

        int timeout = Thread.currentThread().getName().equals("main") ? 10000 : -1;
        synchronized (getLock())
        {
            clientThread.stop(timeout);
            physicsThread.stop(timeout);
        }
    }

    public Object getLock()
    {
        return lock;
    }

    public static Engine getInstance()
    {
        return instance;
    }

    public static GameHandler getGame()
    {
        return getInstance().game;
    }

    public static ClientThread getClientThread()
    {
        return getInstance().clientThread;
    }

    public static PhysicsThread getPhysicsThread()
    {
        return getInstance().physicsThread;
    }

    public static InputHandler getInputHandler()
    {
        return getClientThread().getInputHandler();
    }

    public static SceneGraph getSceneGraph()
    {
        return getGame().getSceneGraph();
    }

    public static GameSettings getGameSettings()
    {
        return getGame().getGameSettings();
    }
}
