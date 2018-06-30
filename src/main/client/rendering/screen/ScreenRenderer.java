package main.client.rendering.screen;

import main.client.rendering.IRenderable;
import main.client.rendering.ShaderProgram;
import main.client.rendering.geometry.GLMesh;
import main.client.rendering.geometry.MeshData;
import main.client.rendering.geometry.ShaderDataLocations;
import main.client.rendering.geometry.Vertex;
import main.core.Engine;
import main.core.util.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * @author Kelan
 */
public class ScreenRenderer implements IRenderable
{
    private GLMesh guiQuad;

    private ShaderProgram postprocessingShader;

    private FrameBuffer screenBuffer;
    private int deferredDiffuseTexture;             // RED,      GREEN,    BLUE
    private int deferredNormalTexture;              // X,        Y,        Z
    private int deferredSpecularTexture;            // SPECULAR
    private int deferredDepthTexture;               // DEPTH

    private boolean msaaFixedLocations = true;
    private int msaaSamples = 4;
    private int ssaoSamples = 64;
    private float ssaoRadius = 0.5F;

    private int ssaoNoiseWidth = 4;
    private int ssaoNoiseHeight = 4;
    private int ssaoNoiseTexture;
    private int ssaoSamplesTexture;

    public ScreenRenderer()
    {

    }

    @Override
    public void init()
    {
        initBuffers();
        initShaders();
        setAmbientOcclusionSamples(64, new Random());

        if (guiQuad != null)
            guiQuad.dispose();

        Vertex[] vertices = new Vertex[]{
                new Vertex(new Vector3f(0.0F, 0.0F, 0.0F), new Vector2f(0.0F, 0.0F)), // 0
                new Vertex(new Vector3f(1.0F, 0.0F, 0.0F), new Vector2f(1.0F, 0.0F)), // 1
                new Vertex(new Vector3f(0.0F, 1.0F, 0.0F), new Vector2f(0.0F, 1.0F)), // 2
                new Vertex(new Vector3f(1.0F, 1.0F, 0.0F), new Vector2f(1.0F, 1.0F)), // 3
        };

        Integer[] indices = new Integer[]{0, 1, 2, 1, 3, 2};

        MeshData meshData = new MeshData(Arrays.asList(vertices), Arrays.asList(indices));

        guiQuad = new GLMesh(meshData, ShaderDataLocations.getGuiDataLocations());
        onScreenResized();
    }

    public void initBuffers()
    {
        int windowWidth = Engine.getClientThread().getWindowWidth();
        int windowHeight = Engine.getClientThread().getWindowHeight();

        if (screenBuffer != null)
            screenBuffer.dispose();

        glDeleteTextures(deferredDiffuseTexture);
        deferredDiffuseTexture = glGenTextures();

        glDeleteTextures(deferredNormalTexture);
        deferredNormalTexture = glGenTextures();

        glDeleteTextures(deferredSpecularTexture);
        deferredSpecularTexture = glGenTextures();

        glDeleteTextures(deferredDepthTexture);
        deferredDepthTexture = glGenTextures();

        int[] drawBuffers = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};

        screenBuffer = new FrameBuffer();
        screenBuffer.bind(windowWidth, windowHeight);
        screenBuffer.setDrawBuffers(BufferUtils.createIntBuffer(drawBuffers.length).put(drawBuffers).flip());

        updateTexturesMultisample(windowWidth, windowHeight, msaaSamples, msaaFixedLocations);

        screenBuffer.createColourTextureAttachment(0, deferredDiffuseTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createColourTextureAttachment(1, deferredNormalTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createColourTextureAttachment(2, deferredSpecularTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createDepthTextureAttachment(deferredDepthTexture, GL_TEXTURE_2D_MULTISAMPLE);

        FrameBuffer.unbind();
    }

    public void initShaders()
    {
        try
        {
            if (postprocessingShader != null)
                postprocessingShader.delete();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/screen/postprocessing/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/screen/postprocessing/fragment.glsl", GL_FRAGMENT_SHADER);

            postprocessingShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void render(double delta)
    {
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        FrameBuffer.unbind();

        ShaderProgram.bind(postprocessingShader);

        Engine.getSceneGraph().applyUniforms(postprocessingShader);

        postprocessingShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
        postprocessingShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
        postprocessingShader.setUniformVector2i("screenResolution", Engine.getClientThread().getWindowWidth(), Engine.getClientThread().getWindowHeight());
        postprocessingShader.setUniformVector1i("diffuseTexture", 0);
        postprocessingShader.setUniformVector1i("normalTexture", 1);
        postprocessingShader.setUniformVector1i("specularEmission", 2);
        postprocessingShader.setUniformVector1i("depthTexture", 3);
        postprocessingShader.setUniformVector1i("ssaoSamples", ssaoSamples);
        postprocessingShader.setUniformVector1f("ssaoRadius", ssaoRadius);
        postprocessingShader.setUniformVector2i("ssaoNoiseSize", ssaoNoiseWidth, ssaoNoiseHeight);
        postprocessingShader.setUniformVector1i("ssaoNoiseTexture", 4);
        postprocessingShader.setUniformVector1i("ssaoSamplesTexture", 5);
        postprocessingShader.setUniformVector1i("msaaSamples", msaaSamples);
        postprocessingShader.setUniformBoolean("showGBuffer", false);

        glEnable(GL_TEXTURE_2D_MULTISAMPLE);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_1D, ssaoSamplesTexture);

        this.guiQuad.draw();

        ShaderProgram.unbind();
    }

    @Override
    public void dispose()
    {
        guiQuad.dispose();
        screenBuffer.dispose();

        glDeleteTextures(deferredDiffuseTexture);
        glDeleteTextures(deferredNormalTexture);
        glDeleteTextures(deferredSpecularTexture);
        glDeleteTextures(deferredDepthTexture);
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {

    }

    public void updateTexturesMultisample(int width, int height, int msaaSamples, boolean fixedSampleLocations)
    {
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGBA32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGB32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_R32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_DEPTH_COMPONENT32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
    }

    public void onScreenResized()
    {
        int width = Engine.getClientThread().getWindowWidth();
        int height = Engine.getClientThread().getWindowHeight();

        glViewport(0, 0, width, height);

        if (width > 0 && height > 0)
        {
            updateTexturesMultisample(width, height, this.msaaSamples, this.msaaFixedLocations);
        }
    }

    public void setAmbientOcclusionSamples(int ssaoSamples, Random rand)
    {
        this.ssaoSamples = ssaoSamples;
        FloatBuffer noiseBuffer = BufferUtils.createFloatBuffer(ssaoNoiseWidth * ssaoNoiseHeight * 3);
        for (int i = 0; i < ssaoNoiseWidth * ssaoNoiseHeight; i++)
            noiseBuffer.put(new float[] {rand.nextFloat() * 2.0F - 1.0F, rand.nextFloat() * 2.0F - 1.0F, 0.0F});
        noiseBuffer.flip();

        glDeleteTextures(ssaoNoiseTexture);
        ssaoNoiseTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, ssaoNoiseWidth, ssaoNoiseHeight, 0, GL_RGB, GL_FLOAT, noiseBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D, 0);

        glDeleteTextures(ssaoSamplesTexture);
        ssaoSamplesTexture = glGenTextures();
        FloatBuffer sampleBuffer = BufferUtils.createFloatBuffer(ssaoSamples * 3);
        for (int i = 0; i < ssaoSamples; i++)
        {
            Vector3f sample = new Vector3f(rand.nextFloat() * 2.0F - 1.0F, rand.nextFloat() * 2.0F - 1.0F, rand.nextFloat());
            sample.normalise().scale(rand.nextFloat() * (float) i / ssaoSamples);
            sampleBuffer.put(MathUtils.getVectorArray3(sample));
        }
        sampleBuffer.flip();

        glBindTexture(GL_TEXTURE_2D, ssaoSamplesTexture);
        glTexImage1D(GL_TEXTURE_1D, 0, GL_RGB32F, ssaoSamples, 0, GL_RGB, GL_FLOAT, noiseBuffer);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_1D, 0);
    }

    public ScreenRenderer bindScreenBuffer()
    {
        this.screenBuffer.bind(Engine.getClientThread().getWindowWidth(), Engine.getClientThread().getWindowHeight());
        return this;
    }
}
