package main.client.rendering;

import main.client.rendering.geometry.GLMesh;
import main.client.rendering.geometry.Material;
import main.client.rendering.geometry.MeshData;
import main.core.Engine;
import main.core.scene.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Kelan
 */
public class MeshRenderer extends Component
{
    private Map<Material, GLMesh> mesh = new HashMap<>();
    private ShaderProgram shader = null;
    private boolean enableDepth = true;
    private boolean enableBlend = true;

    public MeshRenderer(GLMesh mesh, ShaderProgram shader, Material material)
    {
        this.mesh.put(material == null ? Material.NO_MATERIAL : material, mesh);
        this.shader = shader;
    }

    public MeshRenderer(GLMesh mesh, ShaderProgram shader)
    {
        this(mesh, shader, null);
    }

    public MeshRenderer(MeshData mesh, ShaderProgram shader, Material material)
    {
        this.mesh.put(material == null ? Material.NO_MATERIAL : material, new GLMesh(mesh, shader.getDataLocations()));
        this.shader = shader;
    }

    public MeshRenderer(MeshData mesh, ShaderProgram shader)
    {
        this(mesh, shader, null);
    }

    public MeshRenderer(Map<Material, MeshData> meshMap, ShaderProgram shaderProgram)
    {
        meshMap.forEach((material, meshData) -> mesh.put(material, new GLMesh(meshData, shaderProgram.getDataLocations())));
        this.shader = shaderProgram;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void update(double delta)
    {

    }

    @Override
    public void render(double delta)
    {
        render(delta, this.getShader());
    }

    @Override
    public void render(double delta, ShaderProgram shaderProgram)
    {
        if (shaderProgram != null && mesh != null)
        {
            ShaderProgram.bind(shaderProgram);
            this.applyUniforms(shaderProgram);

            if (this.doEnableDepth())
                glEnable(GL_DEPTH_TEST);
            else
                glDisable(GL_DEPTH_TEST);

            if (this.doEnableBlend())
                glEnable(GL_BLEND);
            else
                glDisable(GL_BLEND);

            mesh.forEach((material, glMesh) -> {
                if (material != null)
                {
                    material.bind(shaderProgram);
                    glMesh.draw();
                    material.unbind();
                    shaderProgram.setUniformBoolean("normalMap", false);
                }
            });

            ShaderProgram.bind(null);
        }
    }

    @Override
    public void dispose()
    {
//        mesh.dispose();
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {
        Engine.getSceneGraph().applyUniforms(shaderProgram);

        if (this.getParent() != null)
            this.getParent().applyUniforms(shaderProgram);
    }

    public ShaderProgram getShader()
    {
        return shader;
    }

    public boolean doEnableDepth()
    {
        return enableDepth;
    }

    public boolean doEnableBlend()
    {
        return enableBlend;
    }

    public MeshRenderer setEnableDepth(boolean enableDepth)
    {
        this.enableDepth = enableDepth;
        return this;
    }

    public MeshRenderer setEnableBlend(boolean enableBlend)
    {
        this.enableBlend = enableBlend;
        return this;
    }
}
