package main.client.rendering.geometry;

import main.client.rendering.ShaderProgram;
import main.client.rendering.Texture;
import org.lwjgl.util.vector.Vector3f;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 * @author Kelan
 */

public class Material
{
    public static Material NO_MATERIAL = new Material("no_material");

    /**
     * The name of this material
     */
    protected String name = "invalid_material";

    /**
     * The ambient colour of this material is the colour of the material when no additional
     * lighting effects are applied to it.
     *
     * If this is the only colour applied to the material, it would result in a silhouette
     * of the material being visible, with no 3D depth to it (assuming the background is a
     * different colour to the ambient colour, otherwise you would see nothing)
     */
    protected Vector3f ambientColour = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * The diffuse colour of this material is the colour of the material when a direct
     * source of light, such as a directional, point or spot light is illuminating it.
     * This colour will be affected by the angle of incidence of the light, the intensity
     * of the light, and other such parameters.
     */
    protected Vector3f diffuseColour = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * The specular colour is the colour of specular highlights in the material. Specular
     * reflections in this material will appear to be this colour.
     */
    protected Vector3f specularColour = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * Any light passing through the object is multiplied by the transmission filter. This results
     * in only specific colours passing through the material. For example, if the transmission filter
     * is 0 1 0, it allows all the green to pass through and filters out all the red and blue. This would
     * create an effect similar to a green tinted window.
     */
    protected Vector3f transmissionFilter = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * The ambient colour texture of this material. This texture represents the same thing as the
     * {@link #ambientColour}, but in a texture file instead. This allows for different ambient colour
     * values to be specified at different points on the geometry. In general, the ambient and diffuse
     * colours will be the same, but this may be used to create some kinds of shading effects.
     */
    protected Texture ambientColourTexture = null;

    /**
     * The diffuse colour texture of this material. This texture represents the same thing as the
     * {@link #diffuseColour}, but in a texture file instead. This allows for different diffuse colour
     * values to be specified at different points on the geometry. In general, the ambient and diffuse
     * colours will be the same.
     */
    protected Texture diffuseColourTexture = null;

    /**
     * The specular colour texture of this material. This texture represents the same thing as the
     * {@link #specularColour}, but in a texture file instead. This allows for different specular colour
     * values to be specified at different points on the geometry, allowing for effects such as anodised
     * and brushed effects for metal materials, or wear-and-tear effects.
     */
    protected Texture specularColourTexture = null;

    /**
     * The specular power texture of this material. This texture represents the same thing as the
     * {@link #specularPower}, but in a texture file instead. This allows for different specular intensity
     * values to be specified at different points on the geometry, allowing for much finer control over the
     * specular highlights in different locations on the geometry.
     */
    protected Texture specularPowerTexture = null;

    protected Texture displacementTexture = null;

    protected Texture normalTexture = null;

    protected Texture alphaTexture = null;

    /**
     * Specifies the specular exponent for the current material. This defines the focus of the specular
     * highlight. A high exponent results in a tight, concentrated highlight. Values normally range
     * from 0 to 1000.
     */
    protected float specularPower = 0.0F;

    /**
     * The transmission value for the current material. This value defines the amount of light that can
     * be transmitted through this material. Glass for example would have a very high transmission value,
     * as it lets through the vast majority of light.
     */
    protected float transmission = 0.0F;

    /**
     * The optical density of this material. This is also known as index of refraction.
     *
     * Values for this can range from 0.001 to 10. A value of 1.0 means that light does not bend as it
     * passes through the material. Increasing the value increases the amount of bending. Glass has an
     * index of refraction of about 1.5. Values of less than 1.0 produce bizarre results and are not
     * recommended.
     */
    protected float opticalDensity = 1.0F;

    public Material(String name)
    {
        this.name = name;
    }

    public int bind(ShaderProgram shader)
    {
//        System.out.println(getName());
        shader.setUniformBoolean("normalMap", false);
        shader.setUniformBoolean("alphaMap", false);
        shader.setUniformBoolean("displacementMap", false);
        shader.setUniformVector3f("ambientColour", ambientColour);
        shader.setUniformVector3f("diffuseColour", diffuseColour);
        shader.setUniformVector3f("specularColour", specularColour);
        shader.setUniformVector1f("specularPower", specularPower);
        shader.setUniformVector1f("transmission", transmission);
        shader.setUniformVector3f("transmissionFilter", transmissionFilter);
        shader.setUniformVector1f("opticalDensity", opticalDensity);

        boolean clampUV = false;
        int textureUnit = 0;
        if (ambientColourTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformVector1i("ambientColourTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, ambientColourTexture.getLoadedTexture());
            clampUV |= ambientColourTexture.clampUV;
            textureUnit++;
        }
        if (diffuseColourTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformVector1i("diffuseColourTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, diffuseColourTexture.getLoadedTexture());
            clampUV |= diffuseColourTexture.clampUV;
            textureUnit++;
        }
        if (specularColourTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformVector1i("specularColourTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, specularColourTexture.getLoadedTexture());
            clampUV |= specularColourTexture.clampUV;
            textureUnit++;
        }
        if (specularPowerTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformVector1i("specularPowerTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, specularPowerTexture.getLoadedTexture());
            clampUV |= specularPowerTexture.clampUV;
            textureUnit++;
        }
        if (displacementTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformBoolean("displacementMap", true);
            shader.setUniformVector1i("displacementTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, displacementTexture.getLoadedTexture());
            clampUV |= displacementTexture.clampUV;
            textureUnit++;
        }
        if (normalTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformBoolean("normalMap", true);
            shader.setUniformVector1i("normalTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, normalTexture.getLoadedTexture());
            clampUV |= normalTexture.clampUV;
            textureUnit++;
        }
        if (alphaTexture != null)
        {
            glEnable(GL_TEXTURE_2D);
            shader.setUniformBoolean("alphaMap", true);
            shader.setUniformVector1i("alphaTexture", textureUnit);
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, alphaTexture.getLoadedTexture());
            clampUV |= alphaTexture.clampUV;
            textureUnit++;
        }

        shader.setUniformBoolean("clampUV", clampUV);

        return textureUnit;
    }

    public void unbind()
    {
        int textureUnit = 0;
        glEnable(GL_TEXTURE_2D);
        if (ambientColourTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (diffuseColourTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (specularColourTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (specularPowerTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (displacementTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (normalTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (alphaTexture != null)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit++);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glDisable(GL_TEXTURE_2D);
    }

    public String getName()
    {
        return name;
    }

    public Vector3f getAmbientColour()
    {
        return ambientColour;
    }

    public Vector3f getDiffuseColour()
    {
        return diffuseColour;
    }

    public Vector3f getSpecularColour()
    {
        return specularColour;
    }

    public Vector3f getTransmissionFilter()
    {
        return transmissionFilter;
    }

    public Texture getAmbientColourTexture()
    {
        return ambientColourTexture;
    }

    public Texture getDiffuseColourTexture()
    {
        return diffuseColourTexture;
    }

    public Texture getSpecularColourTexture()
    {
        return specularColourTexture;
    }

    public Texture getSpecularPowerTexture()
    {
        return specularPowerTexture;
    }

    public Texture getDisplacementTexture()
    {
        return displacementTexture;
    }

    public float getSpecularPower()
    {
        return specularPower;
    }

    public float getTransmission()
    {
        return transmission;
    }

    public float getOpticalDensity()
    {
        return opticalDensity;
    }

    public Material setName(String name)
    {
        this.name = name;
        return this;
    }

    public Material setAmbientColour(Vector3f ambientColour)
    {
        this.ambientColour = ambientColour;
        return this;
    }

    public Material setDiffuseColour(Vector3f diffuseColour)
    {
        this.diffuseColour = diffuseColour;
        return this;
    }

    public Material setSpecularColour(Vector3f specularColour)
    {
        this.specularColour = specularColour;
        return this;
    }

    public Material setTransmissionFilter(Vector3f transmissionFilter)
    {
        this.transmissionFilter = transmissionFilter;
        return this;
    }

    public Material setAmbientColourTexture(Texture ambientColourTexture)
    {
        this.ambientColourTexture = ambientColourTexture;
        return this;
    }

    public Material setDiffuseColourTexture(Texture diffuseColourTexture)
    {
        this.diffuseColourTexture = diffuseColourTexture;
        return this;
    }

    public Material setSpecularColourTexture(Texture specularColourTexture)
    {
        this.specularColourTexture = specularColourTexture;
        return this;
    }

    public Material setSpecularPowerTexture(Texture specularPowerTexture)
    {
        this.specularPowerTexture = specularPowerTexture;
        return this;
    }

    public void setDisplacementTexture(Texture displacementTexture)
    {
        this.displacementTexture = displacementTexture;
    }

    public Material setSpecularPower(float specularPower)
    {
        this.specularPower = specularPower;
        return this;
    }

    public Material setTransmission(float transmission)
    {
        this.transmission = transmission;
        return this;
    }

    public Material setOpticalDensity(float opticalDensity)
    {
        this.opticalDensity = opticalDensity;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Float.compare(material.specularPower, specularPower) == 0 &&
                Float.compare(material.transmission, transmission) == 0 &&
                Float.compare(material.opticalDensity, opticalDensity) == 0 &&
                Objects.equals(name, material.name) &&
                Objects.equals(ambientColour, material.ambientColour) &&
                Objects.equals(diffuseColour, material.diffuseColour) &&
                Objects.equals(specularColour, material.specularColour) &&
                Objects.equals(transmissionFilter, material.transmissionFilter) &&
                Objects.equals(ambientColourTexture, material.ambientColourTexture) &&
                Objects.equals(diffuseColourTexture, material.diffuseColourTexture) &&
                Objects.equals(specularColourTexture, material.specularColourTexture) &&
                Objects.equals(specularPowerTexture, material.specularPowerTexture) &&
                Objects.equals(displacementTexture, material.displacementTexture);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, ambientColour, diffuseColour, specularColour, transmissionFilter, ambientColourTexture, diffuseColourTexture, specularColourTexture, specularPowerTexture, displacementTexture, specularPower, transmission, opticalDensity);
    }

    @Override
    public String toString()
    {
        return "Material{" +
                "name='" + name + '\'' +
                ", ambientColour=" + ambientColour +
                ", diffuseColour=" + diffuseColour +
                ", specularColour=" + specularColour +
                ", transmissionFilter=" + transmissionFilter +
                ", ambientColourTexture=" + ambientColourTexture +
                ", diffuseColourTexture=" + diffuseColourTexture +
                ", specularColourTexture=" + specularColourTexture +
                ", specularPowerTexture=" + specularPowerTexture +
                ", displacementTexture=" + displacementTexture +
                ", specularPower=" + specularPower +
                ", transmission=" + transmission +
                ", opticalDensity=" + opticalDensity +
                '}';
    }
}
