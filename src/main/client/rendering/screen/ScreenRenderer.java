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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;

/**
 * @author Kelan
 */
public class ScreenRenderer implements IRenderable
{
    private GLMesh guiQuad;

    private ShaderProgram screenShader;
    private ShaderProgram ssaoShader;

    private FrameBuffer screenBuffer;
    private int deferredDiffuseTexture;             // RED,      GREEN,    BLUE
    private int deferredNormalTexture;              // X,        Y,        Z
    private int deferredPositionTexture;            // X,        Y,        Z
    private int deferredSpecularTexture;            // SPECULAR
    private int deferredDepthTexture;               // DEPTH
    private int ssaoTexture;

    private boolean ambientOcclusion = true;
    private boolean msaaFixedLocations = true;
    private int msaaSamples = 4;
    private int ssaoSamples = 64;
    private float ssaoRadius = 1.5F;
    private float ssaoOffset = 0.0F;
    private float ssaoTextureScale = 1.0F;
    private int workGroupSizeX;
    private int workGroupSizeY;

    private int ssaoNoiseSize = 32;
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
        setAmbientOcclusionSamples(40, new Random());

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

        glDeleteTextures(deferredPositionTexture);
        deferredPositionTexture = glGenTextures();

        glDeleteTextures(deferredSpecularTexture);
        deferredSpecularTexture = glGenTextures();

        glDeleteTextures(deferredDepthTexture);
        deferredDepthTexture = glGenTextures();

        glDeleteTextures(ssaoTexture);
        ssaoTexture = glGenTextures();


        int[] drawBuffers = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};

        screenBuffer = new FrameBuffer();
        screenBuffer.bind(windowWidth, windowHeight);
        screenBuffer.setDrawBuffers(BufferUtils.createIntBuffer(drawBuffers.length).put(drawBuffers).flip());

        updateTextures(windowWidth, windowHeight, msaaSamples, msaaFixedLocations);

        screenBuffer.createColourTextureAttachment(0, deferredDiffuseTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createColourTextureAttachment(1, deferredNormalTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createColourTextureAttachment(2, deferredPositionTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createColourTextureAttachment(3, deferredSpecularTexture, GL_TEXTURE_2D_MULTISAMPLE);
        screenBuffer.createDepthTextureAttachment(deferredDepthTexture, GL_TEXTURE_2D_MULTISAMPLE);

        FrameBuffer.unbind();
    }

    public void initShaders()
    {
        try
        {
            if (screenShader != null)
                screenShader.delete();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/screen/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/screen/fragment.glsl", GL_FRAGMENT_SHADER);

            screenShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (ssaoShader != null)
                ssaoShader.delete();

            ShaderProgram.Shader compute = new ShaderProgram.Shader("res/shaders/ssao/compute.glsl", GL_COMPUTE_SHADER);
            compute.define("SIZE_X", s -> String.valueOf(Math.max(workGroupSizeX, 1)));
            compute.define("SIZE_Y", s -> String.valueOf(Math.max(workGroupSizeY, 1)));
            ssaoShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), compute);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void render(double delta)
    {
        if (Engine.getInputHandler().keyPressed(GLFW_KEY_F3))
            ambientOcclusion = !ambientOcclusion;

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDisable(GL_POLYGON_OFFSET_FILL);
        glBlendFunc(GL_ONE, GL_ZERO);

        int windowWidth = Engine.getClientThread().getWindowWidth();
        int windowHeight = Engine.getClientThread().getWindowHeight();

        FrameBuffer.unbind();

        if (ambientOcclusion)
            renderSSAO();

        ShaderProgram.bind(screenShader);

        Engine.getSceneGraph().applyUniforms(screenShader);

        screenShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
        screenShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
        screenShader.setUniformVector1f("ssaoTextureScale", ssaoTextureScale);
        screenShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);
        screenShader.setUniformVector1i("diffuseTexture", 0);
        screenShader.setUniformVector1i("normalTexture", 1);
        screenShader.setUniformVector1i("positionTexture", 2);
        screenShader.setUniformVector1i("specularTexture", 3);
        screenShader.setUniformVector1i("depthTexture", 4);
        screenShader.setUniformVector1i("ssaoTexture", 5);
        screenShader.setUniformVector1i("msaaSamples", msaaSamples);
        screenShader.setUniformBoolean("showGBuffer", false);
        screenShader.setUniformBoolean("ambientOcclusion", ambientOcclusion);

        glEnable(GL_TEXTURE_2D_MULTISAMPLE);
        glEnable(GL_TEXTURE_2D);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredPositionTexture);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, ssaoTexture);

        glActiveTexture(GL_TEXTURE6);
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);

        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_1D, ssaoSamplesTexture);

        this.guiQuad.draw();

        ShaderProgram.unbind();
    }

    private void renderSSAO()
    {
        int windowWidth = Engine.getClientThread().getWindowWidth();
        int windowHeight = Engine.getClientThread().getWindowHeight();

        ShaderProgram.bind(ssaoShader);
        Engine.getSceneGraph().applyUniforms(ssaoShader);
        ssaoShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);

        ssaoShader.setUniformVector1i("ssaoSamples", ssaoSamples);
        ssaoShader.setUniformVector1f("ssaoRadius", ssaoRadius);
        ssaoShader.setUniformVector1f("ssaoOffset", ssaoOffset);
        ssaoShader.setUniformVector1i("ssaoNoiseSize", ssaoNoiseSize);
        ssaoShader.setUniformVector1f("ssaoTextureScale", ssaoTextureScale);
        ssaoShader.setUniformVector1i("ssaoNoiseTexture", 0);
        ssaoShader.setUniformVector1i("ssaoSamplesTexture", 1);
        ssaoShader.setUniformVector1i("normalTexture", 2);
        ssaoShader.setUniformVector1i("positionTexture", 3);
        ssaoShader.setUniformVector1i("depthTexture", 4);

        glEnable(GL_TEXTURE_2D_MULTISAMPLE);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_1D, ssaoSamplesTexture);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredPositionTexture);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

        glBindImageTexture(0, ssaoTexture, 0, false, 0, GL_WRITE_ONLY, GL_R32F);
        glDispatchCompute((int) Math.ceil((windowWidth * ssaoTextureScale) / workGroupSizeX), (int) Math.ceil((windowHeight * ssaoTextureScale) / workGroupSizeY), 1);
        glMemoryBarrier(GL_ALL_BARRIER_BITS);
        glFinish();

        ShaderProgram.unbind();
    }

    @Override
    public void dispose()
    {
        guiQuad.dispose();
        screenBuffer.dispose();

        glDeleteTextures(deferredDiffuseTexture);
        glDeleteTextures(deferredNormalTexture);
        glDeleteTextures(deferredPositionTexture);
        glDeleteTextures(deferredSpecularTexture);
        glDeleteTextures(deferredDepthTexture);
        glDeleteTextures(ssaoTexture);
    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {

    }

    public void updateTextures(int width, int height, int msaaSamples, boolean fixedSampleLocations)
    {
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGBA32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGB32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredPositionTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGB32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_R32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_DEPTH_COMPONENT32F, width, height, fixedSampleLocations);

        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

        glBindTexture(GL_TEXTURE_2D, ssaoTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, (int) (width), (int) (height), 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void onScreenResized()
    {
        int width = Engine.getClientThread().getWindowWidth();
        int height = Engine.getClientThread().getWindowHeight();

        glViewport(0, 0, width, height);

        if (width > 0 && height > 0)
        {
            updateTextures(width, height, this.msaaSamples, this.msaaFixedLocations);
        }

        int gcd = MathUtils.gcd(width, height);
        int num = width / gcd;
        int den = height / gcd;

        IntBuffer buf = BufferUtils.createIntBuffer(1);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, buf);
        int maxWorkgroupSize = Math.max(buf.get(), 1024); // 1024 is guaranteed by OpenGL, so if there is an error here, we use that instead.
        double x = Math.sqrt((double) maxWorkgroupSize / (num * den));
        workGroupSizeX = (int) Math.floor(x * num);
        workGroupSizeY = (int) Math.floor(x * den);

//        if ((workGroupSizeX + 1) * (workGroupSizeY - 1) <= maxWorkgroupSize)
//        {
//            workGroupSizeX--;
//            workGroupSizeY++;
//        } else if ((workGroupSizeX - 1) * (workGroupSizeY + 1) <= maxWorkgroupSize)
//        {
//            workGroupSizeX++;
//            workGroupSizeY--;
//        }

        initShaders();
    }

    public void setAmbientOcclusionSamples(int ssaoSamples, Random rand)
    {
        this.ssaoSamples = ssaoSamples;
        FloatBuffer noiseBuffer = BufferUtils.createFloatBuffer(ssaoNoiseSize * ssaoNoiseSize * 3);
        for (int i = 0; i < ssaoNoiseSize * ssaoNoiseSize; i++)
            noiseBuffer.put(new float[]{rand.nextFloat() * 2.0F - 1.0F, rand.nextFloat() * 2.0F - 1.0F, 0.0F});
        noiseBuffer.flip();

        glDeleteTextures(ssaoNoiseTexture);
        ssaoNoiseTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, ssaoNoiseSize, ssaoNoiseSize, 0, GL_RGB, GL_FLOAT, noiseBuffer);
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
            float scale = (float) i / ssaoSamples;
            scale = MathUtils.interpolate(0.1, 1.0, scale * scale);
            sample.normalise().scale(rand.nextFloat() * scale);
            sampleBuffer.put(MathUtils.getVectorArray3(sample));
        }
        sampleBuffer.flip();

        glBindTexture(GL_TEXTURE_2D, ssaoSamplesTexture);
        glTexImage1D(GL_TEXTURE_1D, 0, GL_RGB32F, ssaoSamples, 0, GL_RGB, GL_FLOAT, sampleBuffer);
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
