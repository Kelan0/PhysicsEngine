package main.client.rendering.screen;

import main.core.Engine;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Kelan
 */
public class FrameBuffer
{
    private int frameBufferID;

    public FrameBuffer()
    {
        this.frameBufferID = glGenFramebuffers();
    }

    public void bind(int width, int height)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, this.frameBufferID);
        glViewport(0, 0, width, height);
    }

    public static void unbind()
    {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Engine.getClientThread().getWindowWidth(), Engine.getClientThread().getWindowHeight());
    }

    public void setDrawBuffers(int buffer)
    {
        glDrawBuffer(buffer);
    }

    public void setDrawBuffers(IntBuffer buffer)
    {
        glDrawBuffers(buffer);
    }

    public void dispose()
    {
        glDeleteFramebuffers(frameBufferID);
    }

    public void createColourTextureAttachment(int attachment, int texture, int target)
    {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachment, target, texture, 0);
    }

    public void createDepthTextureAttachment(int texture, int target)
    {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, target, texture, 0);
    }

    public void createColourBufferAttachment(int width, int height, int colourbuffer, int attachment, int format)
    {
        glBindRenderbuffer(GL_RENDERBUFFER, colourbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, format, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachment, GL_RENDERBUFFER, colourbuffer);
    }

    public void createDepthBufferAttachment(int width, int height, int depthBuffer)
    {
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
    }

    public void createColourBufferAttachmentMultisample(int width, int height, int colourbuffer, int attachment, int format, int samples)
    {
        glBindRenderbuffer(GL_RENDERBUFFER, colourbuffer);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, format, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachment, GL_RENDERBUFFER, colourbuffer);
    }

    public void createDepthBufferAttachmentMultisample(int width, int height, int depthBuffer, int samples)
    {
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
    }

    public int genRenderBuffer()
    {
        return glGenRenderbuffers();
    }

    public int getFrameBufferID()
    {
        return frameBufferID;
    }
}
