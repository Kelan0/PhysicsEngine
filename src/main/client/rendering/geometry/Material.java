package main.client.rendering.geometry;

import main.client.rendering.ShaderProgram;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

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
     * The ambient colour of this material
     */
    protected Vector3f ambientColour = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * The diffuse colour of this material
     */
    protected Vector3f diffuseColour = new Vector3f(0.0F, 0.0F, 0.0F);

    /**
     * The specular colour of this material. xyz represent the RGB components, and w rer coefficient
     */
    protected Vector4f specularColour = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);

    /**
     * The diffuse texture of this material. This is multiplied by the diffuse colour
     */
    protected int diffuseTexture = 0;

    /**
     * The specular texture of this material. This is multiplied by the specular colour
     */
    protected int specularTexture = 0;

    /**
     * The diffuse texture of this material. This is multiplied by the diffuse colour
     */
    protected int ambientTexture = 0;

    public Material(String name)
    {
        this.name = name;
    }

    public void bind(ShaderProgram shader)
    {
//        shader.setUniformVector3f("ambientColour", this.ambientColour);
//        shader.setUniformVector3f("diffuseColour", this.diffuseColour);
//        shader.setUniformVector3f("specularColour", new Vector3f(this.specularColour));
//
//        if (diffuseTexture != null)
//        {
//            diffuseTexture.bind(shader);
//        }
////        specularTexture.bind(shader);
////        ambientTexture.bind(shader);
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

    public Vector4f getSpecularColour()
    {
        return specularColour;
    }

    public int getDiffuseTexture()
    {
        return diffuseTexture;
    }

    public int getSpecularTexture()
    {
        return specularTexture;
    }

    public int getAmbientTexture()
    {
        return ambientTexture;
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

    public Material setSpecularColour(Vector4f specularColour)
    {
        this.specularColour = specularColour;
        return this;
    }

    public Material setDiffuseTexture(int diffuseTexture)
    {
        this.diffuseTexture = diffuseTexture;
        return this;
    }

    public Material setSpecularTexture(int specularTexture)
    {
        this.specularTexture = specularTexture;
        return this;
    }

    public Material setAmbientTexture(int ambientTexture)
    {
        this.ambientTexture = ambientTexture;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Material material = (Material) o;

        return name != null ? name.equals(material.name) : material.name == null;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "Material{" + "name='" + name + '\'' + ", ambientColour=" + ambientColour + ", diffuseColour=" + diffuseColour + ", specularColour=" + specularColour + ", diffuseTexture=" + diffuseTexture + ", specularTexture=" + specularTexture + ", ambientTexture=" + ambientTexture + '}';
    }
}
