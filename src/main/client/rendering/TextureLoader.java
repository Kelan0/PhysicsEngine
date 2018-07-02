package main.client.rendering;

import org.lwjgl.util.vector.Vector3f;

/**
 * @author Kelan
 */
public class TextureLoader
{
    public boolean blendu = true;
    public boolean blendv = true;
    public boolean colourCorrection = false;
    public boolean clampUV = false;
    public float brightness = 1.0F;
    public float contrast = 0.0F;
    public float resolution = 1.0F;
    public Vector3f offset = new Vector3f();
    public Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
    public Vector3f turbulence = new Vector3f();
    public String channel = "";
    public String filePath = "";

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
}
