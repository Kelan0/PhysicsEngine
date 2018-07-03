package main.core.scene;

import main.client.ClientThread;
import main.client.rendering.Camera;
import main.client.rendering.IRenderable;
import main.client.rendering.ShaderProgram;
import main.core.Engine;
import main.core.input.components.FlyController;
import main.physics.ITickable;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL14.*;

/**
 * @author Kelan
 */
public class SceneGraph implements ITickable, IRenderable
{
    private GameObject root;
    private GameObject player;
    private GameObject world;

    public SceneGraph()
    {
        this.root = new GameObject();
        this.player = this.createPlayerObject();
        this.world = this.createWorld();
    }

    protected GameObject createPlayerObject()
    {
        Vector3f position = new Vector3f(-0.41631278F, -0.04009331F, -0.14944725F);
        Quaternion rotation = new Quaternion(-0.18059678F, -0.37671292F, -0.075138204F, 0.90544266F);
        GameObject player = new GameObject(new Transformation(position, rotation));

        player.addComponent("camera", new Camera(new Transformation(new Vector3f(0.0F, 2.0F, 2.0F))));
        player.addComponent("controller", new FlyController());

        return player;
    }

    protected GameObject createWorld()
    {
        return new GameObject();
    }

    public GameObject getRoot()
    {
        return root;
    }

    public GameObject getPlayer()
    {
        return player;
    }

    public GameObject getWorld()
    {
        return world;
    }

    @Override
    public void init()
    {
        System.out.println("Initializing scene graph");

        this.player.init();
        this.world.init();
        this.root.init();
    }

    @Override
    public void update(double delta)
    {
        this.player.update(delta);
        this.world.update(delta);
        this.root.update(delta);
    }

    @Override
    public void render(double delta)
    {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        this.player.render(delta);
        ClientThread clientThread = Engine.getClientThread();
        boolean drawWireframe = clientThread.doDrawWireframe();
        boolean drawGeometry = clientThread.doDrawGeometry();
        boolean antialiasing = clientThread.isAntialiasingEnabled();

//        if (antialiasing)
//        {
//            glEnable(GL_MULTISAMPLE);
//            glEnable(GL_LINE_SMOOTH);
//            glEnable(GL_POLYGON_SMOOTH);
//            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
//            glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
//        }

        if (drawGeometry)
        {
            clientThread.setDrawWireframe(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glDisable(GL_POLYGON_OFFSET_FILL);
            glDisable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ZERO);
            this.world.render(delta);
            this.root.render(delta);
            clientThread.setDrawWireframe(drawWireframe);
        }
        if (drawWireframe)
        {
            clientThread.setDrawGeometry(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glEnable(GL_POLYGON_OFFSET_FILL);
            glEnable(GL_BLEND);
            glPolygonOffset(2.0F, 2.0F);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            this.world.render(delta);
            this.root.render(delta);
            clientThread.setDrawGeometry(drawGeometry);
        }
    }

    @Override
    public void dispose()
    {
        this.player.dispose();
        this.world.dispose();
        this.root.dispose();
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {
        Component camera = this.getPlayer().getComponent("camera");

        if (camera != null)
            camera.applyUniforms(shaderProgram);

        ClientThread clientThread = Engine.getInstance().getClientThread();
        if (clientThread.doDrawWireframe())
        {
            shaderProgram.setUniformVector4f("colourMultiplier", clientThread.getWireframeColour());
            shaderProgram.setUniformBoolean("wireframe", true);
        } else
        {
            shaderProgram.setUniformVector4f("colourMultiplier", new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
            shaderProgram.setUniformBoolean("wireframe", false);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SceneGraph that = (SceneGraph) o;

        return Objects.equals(root, that.root) && Objects.equals(player, that.player) && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(root, player, world);
    }

    @Override
    public String toString()
    {
        return "SceneGraph{" + "root=" + root + ", player=" + player + ", world=" + world + '}';
    }
}
