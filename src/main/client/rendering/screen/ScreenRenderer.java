package main.client.rendering.screen;

import main.client.rendering.IRenderable;
import main.client.rendering.PointLight;
import main.client.rendering.ShaderProgram;
import main.client.rendering.ShadowRenderer;
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
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL45.glGenerateTextureMipmap;

/**
 * @author Kelan
 */
public class ScreenRenderer implements IRenderable
{
    private GLMesh guiQuad;

    private ShaderProgram quadShader;

    private ShaderProgram deferredShader;
    private ShaderProgram screenShader;
    private ShaderProgram ssaoShader;
    private ShaderProgram shadowShader;

    private FrameBuffer deferredBuffer;
    private FrameBuffer screenBuffer;
    private FrameBuffer ssaoBuffer;
    private FrameBuffer shadowBuffer;
    private int deferredDiffuseTexture;             // RED,      GREEN,    BLUE
    private int deferredNormalTexture;              // X,        Y,        Z
    private int deferredSpecularTexture;            // RED,      GREEN,    BLUE
    private int deferredDepthTexture;               // DEPTH
    private int deferredSSAOTexture;                // OCCLUSION
    private int deferredShadowTexture;              // OCCLUSION
    private int screenTexture;                      // R         G         B

    private boolean showBuffers = false;
    private boolean ambientOcclusion = true;
    private boolean shadowMapping = true;
    private int msaaSamples = 1;
    private int ssaoSamples = 64;
    private float ssaoRadius = 1.5F;
    private float ssaoOffset = 0.0F;
    private float ssaoTextureScale = 0.75F;
    private int workGroupSizeX;
    private int workGroupSizeY;

    private int ssaoNoiseSize = 64;
    private int ssaoNoiseTexture;
    private int ssaoSamplesTexture;

    private int selectedFullscreenBuffer = -1;
    private boolean renderCubemap = false;

    private ShadowRenderer shadowRenderer = new ShadowRenderer(1024);

    public ScreenRenderer()
    {

    }

    @Override
    public void init()
    {
        initBuffers();
        initShaders();

        shadowRenderer.init();

        setAmbientOcclusionSamples(32, new Random());

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
        int width = Engine.getClientThread().getWindowWidth();
        int height = Engine.getClientThread().getWindowHeight();

        if (deferredBuffer != null)
            deferredBuffer.dispose();

        if (screenBuffer != null)
            screenBuffer.dispose();

        if (ssaoBuffer != null)
            ssaoBuffer.dispose();

        if (shadowBuffer != null)
            shadowBuffer.dispose();

        glDeleteTextures(deferredDiffuseTexture);
        deferredDiffuseTexture = glGenTextures();

        glDeleteTextures(deferredNormalTexture);
        deferredNormalTexture = glGenTextures();

        glDeleteTextures(deferredSpecularTexture);
        deferredSpecularTexture = glGenTextures();

        glDeleteTextures(deferredDepthTexture);
        deferredDepthTexture = glGenTextures();

        glDeleteTextures(deferredSSAOTexture);
        deferredSSAOTexture = glGenTextures();

        glDeleteTextures(deferredShadowTexture);
        deferredShadowTexture = glGenTextures();

        glDeleteTextures(screenTexture);
        screenTexture = glGenTextures();

        boolean fixedSampleLocations = true;

        int[] drawBuffers = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};

        updateTextures(width, height, msaaSamples, true);

        System.out.println("Creating deferred framebuffer");
        deferredBuffer = new FrameBuffer();
        deferredBuffer.bind(width, height);
        deferredBuffer.setDrawBuffers(BufferUtils.createIntBuffer(drawBuffers.length).put(drawBuffers).flip());
        deferredBuffer.createColourTextureAttachment(0, deferredDiffuseTexture, GL_TEXTURE_2D_MULTISAMPLE);
        deferredBuffer.createColourTextureAttachment(1, deferredNormalTexture, GL_TEXTURE_2D_MULTISAMPLE);
        deferredBuffer.createColourTextureAttachment(2, deferredSpecularTexture, GL_TEXTURE_2D_MULTISAMPLE);
        deferredBuffer.createDepthTextureAttachment(deferredDepthTexture, GL_TEXTURE_2D_MULTISAMPLE);
        deferredBuffer.checkStatus();

        System.out.println("Creating screen framebuffer");
        screenBuffer = new FrameBuffer();
        screenBuffer.bind(width, height);
        screenBuffer.createColourTextureAttachment(0, screenTexture, GL_TEXTURE_2D);
        screenBuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0);
        screenBuffer.checkStatus();

        System.out.println("Creating ssao framebuffer");
        ssaoBuffer = new FrameBuffer();
        ssaoBuffer.bind((int) (width * ssaoTextureScale), (int) (height * ssaoTextureScale));
        ssaoBuffer.createColourTextureAttachment(0, deferredSSAOTexture, GL_TEXTURE_2D);
        ssaoBuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0);
        ssaoBuffer.checkStatus();

        System.out.println("Creating shadow framebuffer");
        shadowBuffer = new FrameBuffer();
        shadowBuffer.bind(width, height);
        shadowBuffer.createColourTextureAttachment(0, deferredShadowTexture, GL_TEXTURE_2D);
        shadowBuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0);
        shadowBuffer.checkStatus();
        FrameBuffer.unbind();
    }

    public void initShaders()
    {
        try
        {
            if (quadShader != null)
                quadShader.dispose();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/quad/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/quad/fragment.glsl", GL_FRAGMENT_SHADER);

            quadShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (deferredShader != null)
                deferredShader.dispose();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/deferred/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/deferred/fragment.glsl", GL_FRAGMENT_SHADER);

            deferredShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (screenShader != null)
                screenShader.dispose();

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
                ssaoShader.dispose();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/ssao/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/ssao/fragment.glsl", GL_FRAGMENT_SHADER);

            ssaoShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
//            ShaderProgram.Shader compute = new ShaderProgram.Shader("res/shaders/ssao/compute.glsl", GL_COMPUTE_SHADER);
//            compute.define("SIZE_X", s -> String.valueOf(Math.max(workGroupSizeX, 1)));
//            compute.define("SIZE_Y", s -> String.valueOf(Math.max(workGroupSizeY, 1)));
//            ssaoShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), compute);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (shadowShader != null)
                shadowShader.dispose();

            ShaderProgram.Shader vertex = new ShaderProgram.Shader("res/shaders/shadowScreen/vertex.glsl", GL_VERTEX_SHADER);
            ShaderProgram.Shader fragment = new ShaderProgram.Shader("res/shaders/shadowScreen/fragment.glsl", GL_FRAGMENT_SHADER);

            shadowShader = new ShaderProgram(ShaderDataLocations.getGuiDataLocations(), vertex, fragment);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void render(double delta)
    {
        int windowWidth = Engine.getClientThread().getWindowWidth();
        int windowHeight = Engine.getClientThread().getWindowHeight();

        if (Engine.getInputHandler().keyPressed(GLFW_KEY_F3))
            ambientOcclusion = !ambientOcclusion;

        if (Engine.getInputHandler().keyPressed(GLFW_KEY_F4))
        {
            if (selectedFullscreenBuffer < 0)
                showBuffers = !showBuffers;
            else
                selectedFullscreenBuffer = -1;
        }

        if (Engine.getInputHandler().keyPressed(GLFW_KEY_F5))
            renderCubemap = !renderCubemap;

        FrameBuffer.unbind();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDisable(GL_POLYGON_OFFSET_FILL);
        glBlendFunc(GL_ONE, GL_ZERO);

        if (ambientOcclusion)
            renderSSAO(windowWidth, windowHeight);

        renderShadows(windowWidth, windowHeight);

        FrameBuffer.unbind();
        glViewport(0, 0, windowWidth, windowHeight);
        glClear(GL_COLOR_BUFFER_BIT);

        if (!showBuffers)
        {
            renderDeferred(windowWidth, windowHeight);
            renderScreen(windowWidth, windowHeight);
        } else
        {
            renderDebugBuffers(windowWidth, windowHeight);
        }
    }

    @Override
    public void render(double delta, ShaderProgram shaderProgram)
    {
        render(delta);
    }

    private void renderShadows(int windowWidth, int windowHeight)
    {
        shadowBuffer.bind(windowWidth, windowHeight);
        glViewport(0, 0, windowWidth, windowHeight);
        glClear(GL_COLOR_BUFFER_BIT);

        for (int i = 0; i < Engine.getSceneGraph().getLights().size(); i++)
        {
            PointLight light = Engine.getSceneGraph().getLights().get(i);

            if (light != null)
            {
                shadowRenderer.renderLight(light);
                FrameBuffer.unbind();
                glDisable(GL_DEPTH_TEST);

                glBindTexture(GL_TEXTURE_2D, 0);
                glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

                shadowBuffer.bind(windowWidth, windowHeight);
                glViewport(0, 0, windowWidth, windowHeight);

                ShaderProgram.bind(shadowShader);

                Engine.getSceneGraph().applyUniforms(shadowShader);
                shadowShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
                shadowShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
                shadowShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);
                shadowShader.setUniformVector1i("depthTexture", 0);
                shadowShader.setUniformVector1i("numLights", 1);
                shadowShader.setUniformVector3f("light.position", light.getPosition());
                shadowShader.setUniformVector3f("light.colour", light.getColour());
                shadowShader.setUniformVector3f("light.attenuation", light.getAttenuation());
                shadowShader.setUniformVector1f("light.intensity", light.getIntensity());
                shadowShader.setUniformVector1f("light.nearPlane", shadowRenderer.getNearPlane());
                shadowShader.setUniformVector1f("light.farPlane", shadowRenderer.getFarPlane());
                shadowShader.setUniformVector1i("light.shadowMap", 1);

                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_CUBE_MAP, shadowRenderer.getCubemapTexture());

                if (i > 0)
                {
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_ONE, GL_ONE);
                } else
                {
                    glDisable(GL_BLEND);
                }
                guiQuad.draw();

                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

                ShaderProgram.unbind();
                FrameBuffer.unbind();
            }
        }
    }

    private void renderSSAO(int windowWidth, int windowHeight)
    {
        FrameBuffer.unbind();
        glDisable(GL_DEPTH_TEST);

        glBindTexture(GL_TEXTURE_1D, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        ssaoBuffer.bind((int) (windowWidth * ssaoTextureScale), (int) (windowHeight * ssaoTextureScale));
        glViewport(0, 0, (int) (windowWidth * ssaoTextureScale), (int) (windowHeight * ssaoTextureScale));

        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_TEXTURE_1D);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_2D_MULTISAMPLE);

        ShaderProgram.bind(ssaoShader);

        Engine.getSceneGraph().applyUniforms(ssaoShader);
        ssaoShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
        ssaoShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
        ssaoShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);

        ssaoShader.setUniformVector1i("ssaoSamples", ssaoSamples);
        ssaoShader.setUniformVector1f("ssaoRadius", ssaoRadius);
        ssaoShader.setUniformVector1f("ssaoOffset", ssaoOffset);
        ssaoShader.setUniformVector1i("ssaoNoiseSize", ssaoNoiseSize);
        ssaoShader.setUniformVector1f("ssaoTextureScale", ssaoTextureScale);
        ssaoShader.setUniformVector1i("normalTexture", 0);
        ssaoShader.setUniformVector1i("depthTexture", 1);
        ssaoShader.setUniformVector1i("ssaoNoiseTexture", 2);
        ssaoShader.setUniformVector1i("ssaoSamplesTexture", 3);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_1D, ssaoSamplesTexture);

        guiQuad.draw();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_1D, 0);

        ShaderProgram.unbind();
        FrameBuffer.unbind();
    }

    private void renderDeferred(int windowWidth, int windowHeight)
    {
        FrameBuffer.unbind();
        glDisable(GL_DEPTH_TEST);

        screenBuffer.bind(windowWidth, windowHeight);
        glViewport(0, 0, windowWidth, windowHeight);
        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_TEXTURE_1D);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_2D_MULTISAMPLE);
        glEnable(GL_TEXTURE_CUBE_MAP);
        glDisable(GL_BLEND);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, deferredSSAOTexture);

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, deferredShadowTexture);

        ShaderProgram.bind(deferredShader);

        Engine.getSceneGraph().applyUniforms(deferredShader);

        deferredShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
        deferredShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
        deferredShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);
        deferredShader.setUniformVector1i("diffuseTexture", 0);
        deferredShader.setUniformVector1i("normalTexture", 1);
        deferredShader.setUniformVector1i("specularTexture", 2);
        deferredShader.setUniformVector1i("depthTexture", 3);
        deferredShader.setUniformVector1i("ssaoTexture", 4);
        deferredShader.setUniformVector1i("shadowTexture", 5);
        deferredShader.setUniformVector1i("msaaSamples", msaaSamples);
        deferredShader.setUniformBoolean("showGBuffer", false);
        deferredShader.setUniformBoolean("ambientOcclusion", ambientOcclusion);

        guiQuad.draw();

        ShaderProgram.unbind();
        FrameBuffer.unbind();

        selectedFullscreenBuffer = -1;
    }

    private void renderScreen(int windowWidth, int windowHeight)
    {
        FrameBuffer.unbind();

        glEnable(GL_TEXTURE_1D);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_2D_MULTISAMPLE);
        glEnable(GL_TEXTURE_CUBE_MAP);
        glDisable(GL_BLEND);

        glBindTexture(GL_TEXTURE_1D, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, screenTexture);
        long a = System.nanoTime();
        glGenerateTextureMipmap(screenTexture);
//        glGenerateMipmap(GL_TEXTURE_2D);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        int level = (int) (Math.log(Math.max(windowWidth, windowHeight)) / Math.log(2));
        glFinish();
        long b = System.nanoTime();
        glGetTexImage(GL_TEXTURE_2D, level, GL_RGBA, GL_FLOAT, buffer);
        Vector3f rgb = new Vector3f(buffer.get(), buffer.get(), buffer.get());
        System.out.println(level + ": " + rgb + " (" + (b - a) / 1000000.0 + "ms)");

        ShaderProgram.bind(screenShader);

        Engine.getSceneGraph().applyUniforms(screenShader);

        screenShader.setUniformVector2f("quadPosition", 0.0F, 0.0F);
        screenShader.setUniformVector2f("quadSize", 1.0F, 1.0F);
        screenShader.setUniformVector2i("screenResolution", windowWidth, windowHeight);
        screenShader.setUniformVector1i("screenTexture", 0);
        screenShader.setUniformVector3f("averageColour", rgb);

        guiQuad.draw();

        ShaderProgram.unbind();
        FrameBuffer.unbind();
    }

    private void renderDebugBuffers(int windowWidth, int windowHeight)
    {
        glEnable(GL_TEXTURE_1D);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_2D_MULTISAMPLE);

        glBindTexture(GL_TEXTURE_1D, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDiffuseTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredNormalTexture);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, deferredSSAOTexture);

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, deferredShadowTexture);

        ShaderProgram.bind(quadShader);
        Engine.getSceneGraph().applyUniforms(quadShader);

        glEnable(GL_TEXTURE_2D_MULTISAMPLE);
        glEnable(GL_TEXTURE_2D);

        int[][] textures;

        if (selectedFullscreenBuffer < 0)
        {
            textures = new int[][]{
                    {0, 1, 2},
                    {3, 4, 5}
            };
        } else
        {
            textures = new int[][]{
                    {selectedFullscreenBuffer},
            };
        }

        float size = 1.0F / Math.max(textures.length, textures[0].length);

        for (int i = 0; i < textures.length; i++)
        {
            for (int j = 0; j < textures[i].length; j++)
            {
                int texture = textures[i][j];

                if (texture >= 0)
                {
                    float x = j * size + (textures.length > textures[0].length ? size * 0.5F : 0.0F);
                    float y = i * size + (textures.length < textures[0].length ? size * 0.5F : 0.0F);
                    float w = size;

                    if (!Engine.getInputHandler().isMouseGrabbed() && selectedFullscreenBuffer < 0)
                    {
                        Vector2f mousePosition = Engine.getInputHandler().getMousePosition();
                        mousePosition.x = (mousePosition.x / windowWidth) + 0.5F;
                        mousePosition.y = (mousePosition.y / windowHeight) + 0.5F;

                        if (mousePosition.x >= x && mousePosition.y >= y && mousePosition.x < (x + size) && mousePosition.y < (y + size))
                        {
                            if (Engine.getInputHandler().mouseReleased(GLFW_MOUSE_BUTTON_1))
                            {
                                selectedFullscreenBuffer = texture;
                            } else
                            {
                                w = size * 0.92F;
                                x += (size - w) * 0.5F;
                                y += (size - w) * 0.5F;
                            }
                        }
                    }

                    quadShader.setUniformVector2f("quadPosition", x, y);
                    quadShader.setUniformVector2f("quadSize", w, w);
                    quadShader.setUniformVector1i("quadTexture", texture);
                    quadShader.setUniformVector1i("quadTextureMS", texture);
                    quadShader.setUniformBoolean("depth", texture == 3);
                    quadShader.setUniformBoolean("normal", texture == 1);
                    quadShader.setUniformBoolean("grayscale", texture == 3 || texture == 4 || texture == 5);
                    quadShader.setUniformBoolean("multisample", texture != 4 && texture != 5);

                    guiQuad.draw();
                }
            }
        }

        ShaderProgram.unbind();
    }

    private void renderDebugShadowDepthMap()
    {
        glClear(GL_DEPTH_BUFFER_BIT);
        ShaderProgram.bind(quadShader);
        Engine.getSceneGraph().applyUniforms(quadShader);
        shadowRenderer.lightCamera.applyUniforms(quadShader);

        if (renderCubemap)
        {
            glEnable(GL_TEXTURE_CUBE_MAP);
            glActiveTexture(GL_TEXTURE31);
            glBindTexture(GL_TEXTURE_CUBE_MAP, shadowRenderer.cubemap);

            int[][] quads = new int[][]{
                    {-1, 2, -1, -1},
                    {1, 4, 0, 5},
                    {-1, 3, -1, -1},
            };

            float a = Engine.getClientThread().getWindowAspectRatio();
            float w = 1.0F / (3.0F * a);
            float h = 1.0F / 3.0F;
            for (int i = 0; i < quads.length; i++)
            {
                for (int j = 0; j < quads[i].length; j++)
                {
                    int cubeFace = quads[i][j];
                    if (cubeFace >= 0)
                    {
                        int textureIndex = 31;
                        quadShader.setUniformVector2f("quadPosition", j * w, i * h);
                        quadShader.setUniformVector2f("quadSize", w, h);
                        quadShader.setUniformVector1i("quadTexture", textureIndex);
                        quadShader.setUniformVector1i("quadTextureMS", textureIndex);
                        quadShader.setUniformVector1i("quadTextureCube", textureIndex);
                        quadShader.setUniformBoolean("multisample", false);
                        quadShader.setUniformBoolean("cubemap", true);
                        quadShader.setUniformBoolean("depth", false);
                        quadShader.setUniformBoolean("grayscale", true);
                        quadShader.setUniformMatrix3f("cubeRotation", shadowRenderer.mapDirections[cubeFace]);

                        guiQuad.draw();
                    }
                }
            }

            ShaderProgram.unbind();

            glActiveTexture(GL_TEXTURE31);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        }
    }

    @Override
    public void dispose()
    {
        guiQuad.dispose();
        deferredBuffer.dispose();

        glDeleteTextures(deferredDiffuseTexture);
        glDeleteTextures(deferredNormalTexture);
        glDeleteTextures(deferredSpecularTexture);
        glDeleteTextures(deferredDepthTexture);
        glDeleteTextures(deferredSSAOTexture);
        glDeleteTextures(deferredShadowTexture);
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
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredSpecularTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_RGBA32F, width, height, fixedSampleLocations);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, deferredDepthTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaSamples, GL_DEPTH_COMPONENT32F, width, height, fixedSampleLocations);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

        glBindTexture(GL_TEXTURE_2D, deferredSSAOTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, (int) (width * ssaoTextureScale), (int) (height * ssaoTextureScale), 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, deferredShadowTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, screenTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
    }

    public void onScreenResized()
    {
        int width = Engine.getClientThread().getWindowWidth();
        int height = Engine.getClientThread().getWindowHeight();

        glViewport(0, 0, width, height);

        if (width > 0 && height > 0)
        {
            initBuffers();
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
        this.deferredBuffer.bind(Engine.getClientThread().getWindowWidth(), Engine.getClientThread().getWindowHeight());
        return this;
    }

    public ShaderProgram getQuadShader()
    {
        return quadShader;
    }

    public ShaderProgram getDeferredShader()
    {
        return deferredShader;
    }

    public ShaderProgram getSsaoShader()
    {
        return ssaoShader;
    }
}
