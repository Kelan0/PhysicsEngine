package main.client.rendering;

import main.client.rendering.geometry.ShaderDataLocations;
import main.client.rendering.screen.FrameBuffer;
import main.core.Engine;
import main.core.scene.Transformation;
import main.core.util.MathUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * @author Kelan
 */
public class ShadowRenderer
{
    public Matrix3f[] mapDirections = new Matrix3f[]{
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0), new Vector3f(1, 0, 0), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(1, 0, 0), new Vector3f(0, 0, -1), new Vector3f(0, 1, 0), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, -1, 0), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
            MathUtils.quaternionToMatrix3f(Quaternion.mul(MathUtils.matrix3fToQuaternion(MathUtils.setAxisMatrix3f(new Vector3f(-1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), null), null), MathUtils.axisAngleToQuaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float) (Math.PI), null), null), null),
    };

    public Camera lightCamera;
    public int cubemap;
    public int resolution;
    public FrameBuffer framebuffer;
    public ShaderProgram shadowShader;

    public ShadowRenderer(int resolution)
    {
        if (resolution <= 0)
            throw new IllegalStateException("Invalid shadowmap resolution: " + resolution);

        this.resolution = resolution;
    }

    public void init()
    {
        lightCamera = new Camera(new Transformation(), 0.0025F, 256.0F, 90.0F, 1.0F);

        if (framebuffer != null)
            framebuffer.dispose();

        glDeleteTextures(cubemap);
        cubemap = glGenTextures();

        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap);

        for (int i = 0; i < 6; i++)
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_R32F, resolution, resolution, 0, GL_RED, GL_FLOAT, (FloatBuffer) null);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        framebuffer = new FrameBuffer();
        framebuffer.bind(resolution, resolution);
        framebuffer.setDrawBuffers(GL_DEPTH_ATTACHMENT);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, cubemap, 0);
        framebuffer.createDepthBufferAttachment(resolution, resolution, framebuffer.genRenderBuffer());
        framebuffer.checkStatus();
        FrameBuffer.unbind();

        try
        {
            if (shadowShader != null)
                shadowShader.dispose();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/shadow/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/shadow/fragment.glsl", GL_FRAGMENT_SHADER);

            shadowShader = new ShaderProgram(ShaderDataLocations.getShadowDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void renderLight(PointLight light)
    {
        if (light != null)
        {
            glBindTexture(GL_TEXTURE_1D, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
            glDisable(GL_BLEND);

            framebuffer.bind(resolution, resolution);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glDepthFunc(GL_LEQUAL);
            glClearDepth(1.0F);

            lightCamera.getTransformationOffset().setTranslation(light.getPosition());

            for (int i = 0; i < 6; i++)
            {
                lightCamera.getTransformationOffset().setRotation(MathUtils.matrix3fToQuaternion(mapDirections[i], null));
                lightCamera.render(1.0);
                Engine.getSceneGraph().setCamera(lightCamera);
                ShaderProgram.bind(shadowShader);
                lightCamera.applyUniforms(shadowShader);
                shadowShader.setUniformVector3f("pointLightPos", light.getPosition());
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubemap, 0);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glDisable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ZERO);
                Engine.getSceneGraph().renderSimple(1.0, shadowShader);
            }
            FrameBuffer.unbind();
            Engine.getSceneGraph().setCamera(null);
        }
    }

    public void dispose()
    {
        framebuffer.dispose();
        shadowShader.dispose();
    }

    public int getCubemapTexture()
    {
        return cubemap;
    }

    public int getResolution()
    {
        return resolution;
    }

    public float getNearPlane()
    {
        return lightCamera.getNear();
    }

    public float getFarPlane()
    {
        return lightCamera.getFar();
    }
}
