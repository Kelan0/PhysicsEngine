package main.client.rendering.screen;

import main.core.Engine;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

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

    public FrameBuffer(int frameBufferID)
    {
        this.frameBufferID = frameBufferID;
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

    public void setDrawBuffers(IntBuffer buffer)
    {
        glDrawBuffers(buffer);
    }

    public void setDrawBuffers(int buffer)
    {
        glDrawBuffer(buffer);
    }

    public void setReadBuffers(int buffer)
    {
        glReadBuffer(buffer);
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

    public void checkStatus()
    {
        StringBuilder message = new StringBuilder("Checking framebuffer status: ");

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch (status)
        {
            case GL_FRAMEBUFFER_UNDEFINED:
                message.append("GL_FRAMEBUFFER_UNDEFINED");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                message.append("GL_FRAMEBUFFER_UNSUPPORTED");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                message.append("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
                break;
            case GL_FRAMEBUFFER_COMPLETE:
                message.append("GL_FRAMEBUFFER_COMPLETE");
                break;
            default:
                message.append("Unexpected reply from glCheckFramebufferStatus, " + status);
                break;
        }

        if (message.length() > 0)
            System.out.println(message);
    }
}
