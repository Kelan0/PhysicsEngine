package main.client.rendering;

import main.client.rendering.geometry.GLMesh;
import main.client.rendering.geometry.MeshData;
import main.core.Engine;
import main.core.scene.Component;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Kelan
 */
public class MeshRenderer extends Component
{
    private GLMesh mesh;
    private ShaderProgram shader;
    private boolean enableDepth = true;
    private boolean enableBlend = true;

    public MeshRenderer(GLMesh mesh, ShaderProgram shader)
    {
        this.mesh = mesh;
        this.shader = shader;
    }

    public MeshRenderer(MeshData mesh, ShaderProgram shader)
    {
        this(new GLMesh(mesh, shader.getDataLocations()), shader);
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
        ShaderProgram shader = this.getShader();

        if (shader != null && mesh != null)
        {
            ShaderProgram.bind(shader);
            this.applyUniforms(shader);

            if (this.doEnableDepth())
                glEnable(GL_DEPTH_TEST);
            else
                glDisable(GL_DEPTH_TEST);

            if (this.doEnableBlend())
                glEnable(GL_BLEND);
            else
                glDisable(GL_BLEND);

            this.mesh.draw();

            ShaderProgram.bind(null);
        }
    }

    @Override
    public void dispose()
    {
        mesh.dispose();
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {
        Engine.getSceneGraph().applyUniforms(shaderProgram);

        if (this.getParent() != null)
            this.getParent().applyUniforms(shaderProgram);
    }

    public GLMesh getMesh()
    {
        return mesh;
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
