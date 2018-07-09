package main.client.rendering;

import main.client.rendering.screen.FrameBuffer;
import main.core.util.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

/**
 * @author Kelan
 */
public class PointLight
{
    private Vector3f position;
    private Vector3f colour;
    private Vector3f attenuation;
    private float intensity;

    public PointLight(Vector3f position, Vector3f colour, Vector3f attenuation, float intensity)
    {
        this.position = position;
        this.colour = colour;
        this.attenuation = attenuation;
        this.intensity = intensity;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(Vector3f position)
    {
        this.position = position;
    }

    public Vector3f getColour()
    {
        return colour;
    }

    public void setColour(Vector3f colour)
    {
        this.colour = colour;
    }

    public Vector3f getAttenuation()
    {
        return attenuation;
    }

    public void setAttenuation(Vector3f attenuation)
    {
        this.attenuation = attenuation;
    }

    public float getIntensity()
    {
        return intensity;
    }

    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointLight that = (PointLight) o;
        return Float.compare(that.intensity, intensity) == 0 &&
                Objects.equals(position, that.position) &&
                Objects.equals(colour, that.colour) &&
                Objects.equals(attenuation, that.attenuation);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(position, colour, attenuation, intensity);
    }

    @Override
    public String toString()
    {
        return "PointLight{" +
                "position=" + position +
                ", colour=" + colour +
                ", attenuation=" + attenuation +
                ", intensity=" + intensity +
                '}';
    }
}
