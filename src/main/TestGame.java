package main;

import main.client.rendering.MeshRenderer;
import main.client.rendering.ShaderProgram;
import main.client.rendering.geometry.MeshData;
import main.client.rendering.geometry.MeshHelper;
import main.client.rendering.geometry.OBJModel;
import main.client.rendering.geometry.ShaderDataLocations;
import main.core.Engine;
import main.core.GameHandler;
import main.core.GameSettings;
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
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL20.*;

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
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/default/fragment.glsl", GL_FRAGMENT_SHADER);
            ShaderProgram program = new ShaderProgram(ShaderDataLocations.getDefaultDataLocations(), vertex, fragment);


            MeshData bunny = OBJModel.parseObj("res/models/bunny/bunny.obj").compileMesh();
            MeshData dragon = OBJModel.parseObj("res/models/dragon/dragon.obj").compileMesh();
            MeshData sponza = OBJModel.parseObj("res/models/sponza/sponza.obj").compileMesh();
//            createPhysicsObject("sponza", root, new Transformation(new Vector3f(0.0F, 1.1F, 0.0F), new Vector3f(0.333F, 0.333F, 0.333F)), sponza, program, true);
            createPhysicsObject("bunny", root, new Transformation(new Vector3f(0.0F, 1.1F, 0.0F), new Vector3f(0.333F, 0.333F, 0.333F)), bunny, program, true);
            createPhysicsObject("dragon", root, new Transformation(new Vector3f(3.0F, 0.0F, 2.0F), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), 0.638F, null), new Vector3f(0.25F, 0.25F, 0.25F)), dragon, program, true);
            createPhysicsObject("sponza", root, new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F)), sponza, program, true);
//            createPhysicsObject("floor", root, new Transformation(), MeshHelper.createCuboid(-floorSize, -1.0F, -floorSize, +floorSize, 0.0F, +floorSize), program, true);
            createPhysicsObject("cube", root, new Transformation(new Vector3f(0.0F, 0.5F, 0.0F)), MeshHelper.createCuboid(-0.5F, +0.5F), program, true);
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

    @Override
    public void render(double delta)
    {
        super.render(delta);
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

    public static void main(String[] args)
    {
        Engine.getInstance().init(new TestGame());
    }
}
