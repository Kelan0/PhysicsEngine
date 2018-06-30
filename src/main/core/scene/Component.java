package main.core.scene;

import main.client.rendering.IRenderable;
import main.physics.ITickable;

/**
 * @author Kelan
 */
public abstract class Component implements ITickable, IRenderable
{
    protected GameObject parent;

    public GameObject getParent()
    {
        return parent;
    }
}
