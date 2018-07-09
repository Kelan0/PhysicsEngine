package main.client.rendering.geometry;

import main.core.scene.Transformation;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Kelan
 */
public class Vertex
{
    public static final int BYTES = 48;

    int index;
    Vector3f position;
    Vector3f normal;
    Vector2f texture;
    Vector4f colour;

    public Vertex(Vector3f position, Vector3f normal, Vector2f texture, Vector4f colour)
    {
        this.position = position;
        this.normal = normal;
        this.texture = texture;
        this.colour = colour;
    }

    public Vertex(Vector3f position, Vector3f normal, Vector2f texture)
    {
        this(position, normal, texture, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
    }

    public Vertex(Vector3f position, Vector3f normal)
    {
        this(position, normal, new Vector2f(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
    }

    public Vertex(Vector3f position, Vector2f texture)
    {
        this(position, new Vector3f(), texture, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
    }

    public Vertex(Vector3f position)
    {
        this(position, new Vector3f(), new Vector2f(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
    }

    public Vertex(Vertex vertex)
    {
        this(new Vector3f(vertex.position), new Vector3f(vertex.normal), new Vector2f(vertex.texture), new Vector4f(vertex.colour));
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(Vector3f position)
    {
        this.position = position;
    }

    public Vector3f getNormal()
    {
        return normal;
    }

    public void setNormal(Vector3f normal)
    {
        this.normal = normal;
    }

    public Vector2f getTexture()
    {
        return texture;
    }

    public void setTexture(Vector2f texture)
    {
        this.texture = texture;
    }

    public Vector4f getColour()
    {
        return colour;
    }

    public float[] getData(Transformation transformation)
    {
        return getData(transformation, ShaderDataLocations.getDefaultDataLocations());
    }

    public float[] getData(Transformation transformation, ShaderDataLocations attributes)
    {
        return getData(transformation, attributes == null ? null : attributes.vertexFormat);
    }

    public float[] getData(Transformation transformation, String format)
    {
        Vector4f p = this.position == null ? new Vector4f() : new Vector4f(this.position.x, this.position.y, this.position.z, 1.0F);
        Vector4f n = this.normal == null ? new Vector4f() : new Vector4f(this.normal.x, this.normal.y, this.normal.z, 0.0F);
        Vector2f t = this.texture == null ? new Vector2f() : new Vector2f(this.texture.x, this.texture.y);
        Vector4f c = this.colour == null ? new Vector4f() : new Vector4f(this.colour.x, this.colour.y, this.colour.z, this.colour.w);

        if (transformation != null)
        {
            Matrix4f matrix = transformation.getMatrix();

            Matrix4f.transform(matrix, p, p);
            Matrix4f.transform(matrix, n, n);
        }

        float[] data;

        if (format != null && !format.equalsIgnoreCase("pppnnnttccc"))
        {
            int[] counters = new int[4];
            float[] ap = new float[]{p.x, p.y, p.z};
            float[] an = new float[]{n.x, n.y, n.z};
            float[] at = new float[]{t.x, t.y};
            float[] ac = new float[]{c.x, c.y, c.z, c.w};

            data = new float[format.length()];
            for (int i = 0; i < format.length(); i++)
            {
                char chr = format.charAt(i);

                if ((chr == 'P' || chr == 'p') && counters[0] < 3)
                    data[i] = ap[counters[0]++];
                if ((chr == 'n' || chr == 'N') && counters[1] < 3)
                    data[i] = an[counters[1]++];
                if ((chr == 't' || chr == 'T') && counters[2] < 2)
                    data[i] = at[counters[2]++];
                if ((chr == 'c' || chr == 'C') && counters[3] < 4)
                    data[i] = ac[counters[3]++];
            }
        } else
        {
            data = new float[]{p.x, p.y, p.z, n.x, n.y, n.z, t.x, t.y, c.x, c.y, c.z, c.w};
        }

        return data;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(position, vertex.position) && Objects.equals(normal, vertex.normal) && Objects.equals(texture, vertex.texture) && Objects.equals(colour, vertex.colour);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(position, normal, texture, colour);
    }

    @Override
    public String toString()
    {
        return "Vertex{" + "position=" + position + ", normal=" + normal + ", texture=" + texture + ", colour=" + colour + '}';
    }
}
