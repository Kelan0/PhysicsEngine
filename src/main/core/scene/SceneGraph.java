package main.core.scene;

import main.client.ClientThread;
import main.client.rendering.Camera;
import main.client.rendering.IRenderable;
import main.client.rendering.PointLight;
import main.client.rendering.ShaderProgram;
import main.core.Engine;
import main.core.input.components.FlyController;
import main.core.util.MathUtils;
import main.physics.ITickable;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;

/**
 * @author Kelan
 */
public class SceneGraph implements ITickable, IRenderable
{
    public static final int MAX_LIGHTS = 16;
    private GameObject root;
    private GameObject player;
    private GameObject world;
    private boolean shadowPass = false;

    private final List<PointLight> lights = new ArrayList<>();
    private Camera camera;
    private Matrix3f[] mapDirections;

    public SceneGraph()
    {
        this.root = new GameObject();
        this.player = this.createPlayerObject();
        this.world = this.createWorld();

        if (mapDirections == null)
            mapDirections = new Matrix3f[6];

        mapDirections[0] = MathUtils.setAxisMatrix3f(new Vector3f(-0.0F, 0.0F, 1.0F), new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(+1.0F, 0.0F, 0.0F), null);
        mapDirections[1] = MathUtils.setAxisMatrix3f(new Vector3f(-0.0F, -0.0F, -1.0F), new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 0.0F, 0.0F), null);
        mapDirections[2] = MathUtils.setAxisMatrix3f(new Vector3f(1.0F, -0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, +1.0F, 0.0F), null);
        mapDirections[3] = MathUtils.setAxisMatrix3f(new Vector3f(1.0F, 0.0F, -0.0F), new Vector3f(0.0F, 0.0F, +1.0F), new Vector3f(0.0F, -1.0F, 0.0F), null);
        mapDirections[4] = MathUtils.setAxisMatrix3f(new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, +1.0F), null);
        mapDirections[5] = MathUtils.setAxisMatrix3f(new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, -1.0F), null);
    }

    protected GameObject createPlayerObject()
    {
        GameObject player = new GameObject();

        player.addComponent("camera", new Camera(new Transformation(new Vector3f(0.0F, 2.0F, 0.0F))));
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

    public List<PointLight> getLights()
    {
        return lights;
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
        render(delta, null);
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera != null ? camera : (Camera) getPlayer().getComponent("camera");
    }

    public void renderSimple(double delta, ShaderProgram shaderProgram)
    {
        ClientThread clientThread = Engine.getClientThread();
        boolean drawWireframe = clientThread.doDrawWireframe();
        boolean drawGeometry = clientThread.doDrawGeometry();
        clientThread.setDrawWireframe(false);
        clientThread.setDrawGeometry(true);

        if (shaderProgram != null)
        {
            world.render(delta, shaderProgram);
            root.render(delta, shaderProgram);
        } else
        {
            world.render(delta);
            root.render(delta);
        }

        clientThread.setDrawWireframe(drawWireframe);
        clientThread.setDrawGeometry(drawGeometry);
    }

    @Override
    public void render(double delta, ShaderProgram shaderProgram)
    {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        ClientThread clientThread = Engine.getClientThread();
        Engine.getSceneGraph().getPlayer().render(delta);

        boolean drawWireframe = clientThread.doDrawWireframe();
        boolean drawGeometry = clientThread.doDrawGeometry();
        boolean shadows = true;
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

            renderSimple(delta, shaderProgram);
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

            renderSimple(delta, shaderProgram);
            clientThread.setDrawGeometry(drawGeometry);
        }

        camera = (Camera) getPlayer().getComponent("camera");
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
        if (camera == null && getPlayer() != null)
            camera = (Camera) getPlayer().getComponent("camera");

        if (camera != null)
            camera.applyUniforms(shaderProgram);
        else
            System.err.println("No camera");

        ClientThread clientThread = Engine.getClientThread();
        if (clientThread.doDrawWireframe())
        {
            shaderProgram.setUniformVector4f("colourMultiplier", clientThread.getWireframeColour());
            shaderProgram.setUniformBoolean("drawWireframe", true);
        } else
        {
            shaderProgram.setUniformVector4f("colourMultiplier", new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
            shaderProgram.setUniformBoolean("drawWireframe", false);
        }

        shaderProgram.setUniformBoolean("drawGeometry", clientThread.doDrawGeometry());

        PointLight nullLight = new PointLight(new Vector3f(), new Vector3f(), new Vector3f(), 0.0F);

        for (int i = 0; i < MAX_LIGHTS; i++)
        {
            PointLight light = nullLight;

            if (i < lights.size())
                light = lights.get(i);

            shaderProgram.setUniformVector3f("lights[" + i + "].position", light.getPosition());
            shaderProgram.setUniformVector3f("lights[" + i + "].colour", light.getColour());
            shaderProgram.setUniformVector3f("lights[" + i + "].attenuation", light.getAttenuation());
            shaderProgram.setUniformVector1f("lights[" + i + "].intensity", light.getIntensity());
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
