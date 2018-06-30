package main.client.rendering.geometry;

import main.core.scene.Transformation;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kelan
 */
public class MeshData
{
    private List<Vertex> vertices;
    private List<Integer> indices;

    private Transformation transformationOffset;
    private int indexOffset = 0;

    public MeshData(List<Vertex> vertices, List<Integer> indices)
    {
        this.vertices = Collections.synchronizedList(vertices == null ? new ArrayList<>() : vertices);
        this.indices = Collections.synchronizedList(indices == null ? new ArrayList<>() : indices);
    }

    public MeshData(List<Vertex> vertices)
    {
        this(vertices, null);
    }

    public MeshData()
    {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public List<Vertex> getVertices()
    {
        return vertices;
    }

    public List<Integer> getIndices()
    {
        return indices;
    }

    public static synchronized MeshData concat(MeshData a, MeshData b, MeshData dest)
    {
        if (a == null || b == null)
            return null;

        return combine(a.getNumVertices(), b.getNumIndices(), a, b, dest);
    }

    public static synchronized MeshData combine(int vertexOffset, int indexOffset, MeshData a, MeshData b, MeshData dest)
    {
        if (a == null || b == null)
            return null;

        if (dest == null)
            dest = new MeshData();

        dest.vertices = new ArrayList<>(a.vertices);
        dest.indices = new ArrayList<>(a.indices);

        dest.vertices.addAll(vertexOffset, b.vertices);
        dest.indices.addAll(indexOffset, b.indices.stream().map(i -> i + vertexOffset).collect(Collectors.toList()));

        return dest;
    }

    public synchronized Transformation getTransformationOffset()
    {
        return transformationOffset;
    }

    public synchronized int getIndexOffset()
    {
        return indexOffset;
    }

    public synchronized MeshData setTransformationOffset(Transformation transformationOffset)
    {
        this.transformationOffset = transformationOffset;

        return this;
    }

    public synchronized MeshData setIndexOffset(int indexOffset)
    {
        this.indexOffset = indexOffset;

        return this;
    }

    public synchronized MeshData setOffsets(Transformation transformationOffset, int indexOffset)
    {
        this.setTransformationOffset(transformationOffset);
        this.setIndexOffset(indexOffset);

        return this;
    }

    public FloatBuffer getVertexBufferData(FloatBuffer vertexBuffer)
    {
        synchronized (this.getVertices())
        {
            if (vertexBuffer == null)
                vertexBuffer = BufferUtils.createFloatBuffer(this.getVertexBufferSize());

            for (Vertex vertex : this.getVertices())
                vertexBuffer.put(vertex.getData(this.getTransformationOffset()));

            vertexBuffer.flip();

            return vertexBuffer;
        }
    }

    public IntBuffer getIndexBufferData(IntBuffer indexBuffer)
    {
        synchronized (this.getIndices())
        {
            if (indexBuffer == null)
                indexBuffer = BufferUtils.createIntBuffer(this.getIndexBufferSize());

            for (Integer index : this.getIndices())
                indexBuffer.put(index + this.getIndexOffset());

            indexBuffer.flip();

            return indexBuffer;
        }
    }

    public int getNumVertices()
    {
        return this.getVertices() != null ? this.getVertices().size() : 0;
    }

    public synchronized int getNumIndices()
    {
        return this.getIndices() != null ? this.getIndices().size() : 0;
    }

    public synchronized int getVertexBufferSize()
    {
        return this.getNumVertices() * (Vertex.BYTES / Float.BYTES);
    }

    public synchronized int getIndexBufferSize()
    {
        return this.getNumIndices();
    }

    public boolean hasVertices()
    {
        return this.getNumVertices() > 0;
    }

    public boolean hasIndices()
    {
        return this.getNumIndices() > 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeshData meshData = (MeshData) o;
        return Objects.equals(vertices, meshData.vertices) && Objects.equals(indices, meshData.indices);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(vertices, indices);
    }

    @Override
    public String toString()
    {
        return "MeshData{" + "vertices=" + vertices + ", indices=" + indices + '}';
    }
}
