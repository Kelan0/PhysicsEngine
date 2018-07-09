package main.core.scene;

import main.client.rendering.IRenderable;
import main.client.rendering.ShaderProgram;
import main.physics.ITickable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kelan
 */
public class GameObject implements ITickable, IRenderable
{
    private GameObject parent;
    private Map<String, GameObject> children;
    private Map<String, Component> components;

    private Transformation transformation;

    public GameObject(Transformation transformation)
    {
        this.children = new HashMap<>();
        this.components = new HashMap<>();

        if (transformation == null)
            this.transformation = new Transformation();
        else
            this.transformation = transformation;
    }

    public GameObject()
    {
        this(new Transformation());
    }


    public synchronized boolean addChild(String id, GameObject child)
    {
        if (id != null && !id.isEmpty() && child != null)
        {
            this.children.put(id, child);
            child.parent = this;
            return true;
        }

        return false;
    }

    public synchronized boolean addComponent(String id, Component component)
    {
        if (id != null && !id.isEmpty() && component != null)
        {
            this.components.put(id, component);
            component.parent = this;
            return true;
        }

        return false;
    }

    public synchronized GameObject getChild(String id)
    {
        return this.children.get(id);
    }

    public synchronized Component getComponent(String id)
    {
        return this.components.get(id);
    }

    public synchronized Transformation getTransformation()
    {
        return transformation;
    }

    @Override
    public void init()
    {
        for (GameObject object : this.children.values())
            object.init();

        for (Component component : this.components.values())
            component.init();
    }

    @Override
    public void update(double delta)
    {
        for (GameObject object : this.children.values())
        {
            if (object.doTick())
                object.update(delta);
        }

        for (Component component : this.components.values())
        {
            if (component.doTick())
                component.update(delta);
        }
    }

    @Override
    public void render(double delta)
    {
        for (GameObject object : this.children.values())
        {
            if (object.doRender())
                object.render(delta);
        }

        for (Component component : this.components.values())
        {
            if (component.doRender())
                component.render(delta);
        }
    }

    @Override
    public void render(double delta, ShaderProgram shaderProgram)
    {
        for (GameObject object : this.children.values())
        {
            if (object.doRender())
                object.render(delta, shaderProgram);
        }

        for (Component component : this.components.values())
        {
            if (component.doRender())
                component.render(delta, shaderProgram);
        }
    }

    @Override
    public void dispose()
    {
        for (GameObject object : this.children.values())
            object.dispose();

        for (Component component : this.components.values())
            component.dispose();
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {
        shaderProgram.setUniformMatrix4f("modelMatrix", this.getTransformation().getMatrix());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameObject object = (GameObject) o;
        return Objects.equals(parent, object.parent) && Objects.equals(children, object.children) && Objects.equals(components, object.components) && Objects.equals(transformation, object.transformation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(parent, children, components, transformation);
    }

    @Override
    public String toString()
    {
        return "GameObject{" + "parent=" + parent + ", children=" + children + ", components=" + components + ", transformation=" + transformation + '}';
    }
}
