package main.core.scene;

import main.core.util.MathUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import java.util.Objects;

/**
 * @author Kelan
 */
public class Transformation
{
    private Vector3f translation;
    private Quaternion rotation;
    private Vector3f scale;

    public Transformation(Vector3f translation, Quaternion rotation, Vector3f scale)
    {
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Transformation(Vector3f translation, Quaternion rotation)
    {
        this(translation, rotation, new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public Transformation(Vector3f translation, Vector3f scale)
    {
        this(translation, new Quaternion(), scale);
    }

    public Transformation(Vector3f translation)
    {
        this(translation, new Quaternion(), new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public Transformation()
    {
        this(new Vector3f(), new Quaternion(), new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public Transformation(Transformation transformation)
    {
        this(new Vector3f(transformation.translation), new Quaternion(transformation.rotation), new Vector3f(transformation.scale));
    }

    public Vector3f getTranslation()
    {
        return translation;
    }

    public Quaternion getRotation()
    {
        return rotation;
    }

    public Vector3f getScale()
    {
        return scale;
    }

    public Transformation setTranslation(Vector3f translation)
    {
        this.translation = translation;
        return this;
    }

    public Transformation setRotation(Quaternion rotation)
    {
        this.rotation = rotation;
        return this;
    }

    public Transformation setScale(Vector3f scale)
    {
        this.scale = scale;
        return this;
    }

    public Transformation translate(Vector3f translation)
    {
        Vector3f.add(translation, this.translation, this.translation);
        return this;
    }


    public Transformation rotate(Quaternion rotation)
    {
        Quaternion.mul(rotation, this.rotation, this.rotation);
        return this;
    }

    public Transformation scale(Vector3f scale)
    {
        this.scale.x *= scale.x;
        this.scale.y *= scale.y;
        this.scale.z *= scale.z;
        return this;
    }

    public Matrix4f getMatrix()
    {
        Matrix4f matrix = new Matrix4f();

        Matrix4f.translate(this.translation, matrix, matrix);
        MathUtils.quaternionToMatrix4f(this.rotation, matrix);
        Matrix4f.scale(this.scale, matrix, matrix);

        return matrix;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transformation that = (Transformation) o;
        return Objects.equals(translation, that.translation) && Objects.equals(rotation, that.rotation) && Objects.equals(scale, that.scale);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(translation, rotation, scale);
    }
}
