package main.client.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Kelan
 */
public class Texture
{
    public boolean mipmap = false;
    public boolean blendu = true;
    public boolean blendv = true;
    public boolean colourCorrection = false;
    public boolean clampUV = false;
    public float brightness = 1.0F;
    public float contrast = 0.0F;
    public float resolution = 1.0F;
    public float bumpScale = 0.0F;
    public Vector3f offset = new Vector3f();
    public Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
    public Vector3f turbulence = new Vector3f();
    public String channel = "";
    public String filePath = "";

    private int width;
    private int height;
    private int texture = 0;

    public boolean isBlendu()
    {
        return blendu;
    }

    public void setBlendu(boolean blendu)
    {
        this.blendu = blendu;
    }

    public boolean isBlendv()
    {
        return blendv;
    }

    public void setBlendv(boolean blendv)
    {
        this.blendv = blendv;
    }

    public boolean isColourCorrection()
    {
        return colourCorrection;
    }

    public void setColourCorrection(boolean colourCorrection)
    {
        this.colourCorrection = colourCorrection;
    }

    public boolean isClampUV()
    {
        return clampUV;
    }

    public void setClampUV(boolean clampUV)
    {
        this.clampUV = clampUV;
    }

    public float getBrightness()
    {
        return brightness;
    }

    public void setBrightness(float brightness)
    {
        this.brightness = brightness;
    }

    public float getContrast()
    {
        return contrast;
    }

    public void setContrast(float contrast)
    {
        this.contrast = contrast;
    }

    public float getResolution()
    {
        return resolution;
    }

    public void setResolution(float resolution)
    {
        this.resolution = resolution;
    }

    public float getBumpScale()
    {
        return bumpScale;
    }

    public void setBumpScale(float bumpScale)
    {
        this.bumpScale = bumpScale;
    }

    public Vector3f getOffset()
    {
        return offset;
    }

    public void setOffset(Vector3f offset)
    {
        this.offset = offset;
    }

    public Vector3f getScale()
    {
        return scale;
    }

    public void setScale(Vector3f scale)
    {
        this.scale = scale;
    }

    public Vector3f getTurbulence()
    {
        return turbulence;
    }

    public void setTurbulence(Vector3f turbulence)
    {
        this.turbulence = turbulence;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public int getLoadedTexture()
    {
        if (texture == 0)
            load(GL_TEXTURE_2D);

        return texture;
    }


    public boolean load(int target)
    {
        width = 0;
        height = 0;
        glDeleteTextures(texture);

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer c = BufferUtils.createIntBuffer(1);
        ByteBuffer buffer = null;

        try
        {
            STBImage.stbi_set_flip_vertically_on_load(true);
            buffer = STBImage.stbi_load(filePath, w, h, c, 4);
        } catch (NullPointerException e)
        { }

        if (buffer == null)
        {
            System.err.println("Failed to load image file \"" + filePath + "\"\n" + STBImage.stbi_failure_reason());
            texture = -1;
            return false;
        }

        width = w.get();
        height = h.get();
        texture = glGenTextures();

        glBindTexture(target, texture);
        glTexImage2D(target, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        if (mipmap)
            glGenerateMipmap(target);

        glTexParameteri(target, GL_TEXTURE_WRAP_S, clampUV ? GL_CLAMP : GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, clampUV ? GL_CLAMP : GL_REPEAT);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, mipmap ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);

        glBindTexture(target, 0);

        buffer.clear();

        System.out.println("Loaded image file \"" + filePath + "\", dimensions [" + width + " x " + width + "]");
        return true;
    }
}
