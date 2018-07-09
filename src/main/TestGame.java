package main;

import main.client.rendering.Camera;
import main.client.rendering.MeshRenderer;
import main.client.rendering.PointLight;
import main.client.rendering.ShaderProgram;
import main.client.rendering.geometry.Material;
import main.client.rendering.geometry.MeshData;
import main.client.rendering.geometry.MeshHelper;
import main.client.rendering.geometry.OBJModel;
import main.client.rendering.geometry.ShaderDataLocations;
import main.core.Engine;
import main.core.GameHandler;
import main.core.GameSettings;
import main.core.input.InputHandler;
import main.core.input.components.FlyController;
import main.core.scene.GameObject;
import main.core.scene.SceneGraph;
import main.core.scene.Transformation;
import main.core.util.MathUtils;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * @author Kelan
 */
public class TestGame extends GameHandler
{
    @Override
    protected void createSceneGraph()
    {
        sceneGraph = new SceneGraph();

        GameObject root = sceneGraph.getRoot();

        FlyController controller = (FlyController) sceneGraph.getPlayer().getComponent("controller");
        controller.setMoveSpeed(5.0F);
        controller.setMouseSpeed(0.06F);

        try
        {
            float floorSize = 25.0F;

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/default/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader geometry = new ShaderProgram.Shader("res/shaders/default/geometry.glsl", GL_GEOMETRY_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/default/fragment.glsl", GL_FRAGMENT_SHADER);
            ShaderProgram program = new ShaderProgram(ShaderDataLocations.getDefaultDataLocations(), vertex, geometry, fragment);


            MeshData bunny = OBJModel.parseObj("res/models/bunny/bunny.obj").compileMesh();
            MeshData dragon = OBJModel.parseObj("res/models/dragon/dragon.obj").compileMesh();
            Map<Material, MeshData> sponza = OBJModel.parseObj("res/models/sponza/sponza.obj").compileMeshWithMaterials();
//            createPhysicsObject("sponza", root, new Transformation(new Vector3f(0.0F, 1.1F, 0.0F), new Vector3f(0.333F, 0.333F, 0.333F)), sponza, program, true);
            createPhysicsObject("dragon", root, new Transformation(new Vector3f(-6.0F, 0.0F, -.0F), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), 0.638F, null), new Vector3f(0.125F, 0.125F, 0.125F)), dragon, program, true);
            createPhysicsObject("sponza", root, new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.01F, 0.01F, 0.01F)), sponza, program, true);
            GameObject cube = createPhysicsObject("cube", root, new Transformation(new Vector3f(0.0F, 0.5F, 0.0F)), MeshHelper.createCuboid(-0.5F, +0.5F), program, true);
            createPhysicsObject("bunny", cube, new Transformation(new Vector3f(0.0F, 1.1F, 0.0F), new Vector3f(0.333F, 0.333F, 0.333F)), bunny, program, true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void createGameSettings()
    {
        gameSettings = new GameSettings();
    }

    private PointLight currentLight = null;

    @Override
    public void render(double delta)
    {

        super.render(delta);

        SceneGraph sceneGraph = Engine.getSceneGraph();
        InputHandler inputHandler = Engine.getInputHandler();

        if (inputHandler.keyDown(GLFW_KEY_LEFT_CONTROL) && inputHandler.mousePressed(GLFW_MOUSE_BUTTON_1))
            sceneGraph.getLights().add(currentLight = new PointLight(new Vector3f(((Camera) sceneGraph.getPlayer().getComponent("camera")).getPositionWithOffset()), new Vector3f(1.0F, 1.0F, 1.0F), new Vector3f(1.0F, 1.0F, 0.02F), 1.5F));

        if (inputHandler.mouseReleased(GLFW_MOUSE_BUTTON_1))
            currentLight = null;

        if (currentLight != null)
        {
            if (inputHandler.getMouseScroll().y > 0.0F)
                currentLight.getAttenuation().z *= 1.05F;
            if (inputHandler.getMouseScroll().y < 0.0F)
                currentLight.getAttenuation().z /= 1.05F;
        }
    }

    @Override
    public void update(double delta)
    {
        super.update(delta);
    }

    private GameObject createPhysicsObject(String name, GameObject parent, Transformation transformation, MeshData mesh, ShaderProgram shaderProgram, boolean staticObject)
    {
        GameObject gameObject = new GameObject(transformation);
        gameObject.addComponent("meshRenderer", new MeshRenderer(mesh, shaderProgram));
        parent.addChild(name, gameObject);
        return gameObject;
    }

    private GameObject createPhysicsObject(String name, GameObject parent, Transformation transformation, Map<Material, MeshData> mesh, ShaderProgram shaderProgram, boolean staticObject)
    {
        GameObject gameObject = new GameObject(transformation);
        gameObject.addComponent("meshRenderer", new MeshRenderer(mesh, shaderProgram));
        parent.addChild(name, gameObject);
        return gameObject;
    }

    public static void main(String[] args)
    {
        Engine.getInstance().init(new TestGame());
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(+1.0F, 0.0F, 0.0F), null));
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 0.0F, 0.0F), null));
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, +1.0F, 0.0F), null));
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, 0.0F, +1.0F), new Vector3f(0.0F, -1.0F, 0.0F), null));
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, +1.0F), null));
//        System.out.println(Vector3f.cross(new Vector3f(0.0F, -1.0F, 0.0F), new Vector3f(0.0F, 0.0F, -1.0F), null));
    }
}
