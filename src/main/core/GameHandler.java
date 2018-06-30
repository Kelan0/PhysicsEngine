package main.core;

import main.core.scene.SceneGraph;

/**
 * @author Kelan
 */
public abstract class GameHandler
{
    protected SceneGraph sceneGraph;
    protected GameSettings gameSettings;

    public void render(double delta)
    {

    }

    public void update(double delta)
    {

    }

    protected void createSceneGraph()
    {
        sceneGraph = new SceneGraph();
    }

    protected void createGameSettings()
    {
        gameSettings = new GameSettings();
    }

    public SceneGraph getSceneGraph()
    {
        if (sceneGraph == null)
        {
            createSceneGraph();

            if (sceneGraph == null)
                sceneGraph = new SceneGraph();
        }

        return sceneGraph;
    }

    public GameSettings getGameSettings()
    {
        if (gameSettings == null)
        {
            createGameSettings();

            if (gameSettings == null)
                gameSettings = new GameSettings();
        }
        return gameSettings;
    }
}
