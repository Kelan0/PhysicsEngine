package main.client.rendering.geometry;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Kelan
 */
public class GLMesh
{
    private int vertexArray;
    private int vertexBuffer;
    private int indexBuffer;

    private long vertexCount = 0;
    private long indexCount = 0;

    private ShaderDataLocations attributes;
    private long vertexCapacity;
    private long indexCapacity;
    private FloatBuffer vertexData;
    private IntBuffer indexData;

    public GLMesh(MeshData meshData, ShaderDataLocations attributes)
    {
        if (attributes == null)
            this.attributes = ShaderDataLocations.getDefaultDataLocations();
        else
            this.attributes = attributes;

        createBuffers();

        if (meshData != null)
        {
            allocateBuffers(meshData.getNumVertices(), meshData.getNumIndices());
            uploadMeshData(meshData);
        }
    }

    public GLMesh(MeshData meshData)
    {
        this(meshData, null);
    }

    public GLMesh(ShaderDataLocations attributes)
    {
        this(null, attributes);
    }

    public GLMesh()
    {
        this(null, null);
    }

    private void createBuffers()
    {
        vertexArray = glGenVertexArrays();
        vertexBuffer = glGenBuffers();
        indexBuffer = glGenBuffers();
    }

    public GLMesh uploadMeshData(MeshData meshData)
    {
//        System.out.println("Uploading (vertices / indices) " + meshData.getNumVertices() + " / " + meshData.getNumVertices() + " to mesh position " + vertexCount + " / " + indexCount + " out of " + vertexCapacity + " / " + indexCapacity);

        if (meshData != null)
        {
            glBindVertexArray(vertexArray);

            if (meshData.hasVertices())
            {
                FloatBuffer vertexBufferData = meshData.getVertexBufferData(vertexData, attributes);
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
                glBufferSubData(GL_ARRAY_BUFFER, vertexCount * Vertex.BYTES, vertexBufferData);
                vertexCount += meshData.getNumVertices();
            }

            if (meshData.hasIndices())
            {
                IntBuffer indexBufferData = meshData.getIndexBufferData(indexData);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
                glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, indexCount * Integer.BYTES, indexBufferData);
                indexCount += meshData.getNumIndices();
            }

            attributes.enableVertexAttributes();
            attributes.bindVertexAttributes();
            attributes.disableVertexAttributes();

            glBindVertexArray(0);
        }

        return this;
    }

    public GLMesh allocateBuffers(long vertexCapacity, long indexCapacity)
    {
        reset();

        glBindVertexArray(vertexArray);

        this.vertexCapacity = vertexCapacity;
        this.indexCapacity = indexCapacity;

        vertexData = BufferUtils.createFloatBuffer((int) vertexCapacity * (Vertex.BYTES / Float.BYTES));
        indexData = BufferUtils.createIntBuffer((int) indexCapacity);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertexCapacity * Vertex.BYTES, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexCapacity * Integer.BYTES, GL_DYNAMIC_DRAW);

        attributes.enableVertexAttributes();
        attributes.bindVertexAttributes();
        attributes.disableVertexAttributes();
        glBindVertexArray(0);

        return this;
    }

    public GLMesh allocateBuffers(FloatBuffer vertexBuffer, IntBuffer indexBuffer)
    {
        reset();

        glBindVertexArray(vertexArray);

        vertexCapacity = vertexBuffer.capacity() / (Vertex.BYTES / Float.BYTES);
        indexCapacity = indexBuffer.capacity();

        vertexData = vertexBuffer;
        indexData = indexBuffer;

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertexCapacity * Vertex.BYTES, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexCapacity * Integer.BYTES, GL_DYNAMIC_DRAW);

        attributes.enableVertexAttributes();
        attributes.bindVertexAttributes();
        attributes.disableVertexAttributes();
        glBindVertexArray(0);

        return this;
    }

    public GLMesh reset()
    {
        if (vertexData != null)
            vertexData.clear();

        if (indexData != null)
            indexData.clear();

        vertexCount = 0;
        indexCount = 0;

        return this;
    }

    public GLMesh draw()
    {
        glBindVertexArray(vertexArray);

        attributes.enableVertexAttributes();

        if (vertexCount > 0)
        {
            if (indexCount > 0)
                glDrawElements(GL_TRIANGLES, (int) indexCount, GL_UNSIGNED_INT, 0);
            else
                glDrawArrays(GL_TRIANGLES, 0, (int) vertexCount);
        }

        attributes.disableVertexAttributes();

        glBindVertexArray(0);

        return this;
    }

    public void dispose()
    {
        glDeleteBuffers(vertexBuffer);
        glDeleteBuffers(indexBuffer);
        glDeleteVertexArrays(vertexArray);
    }

    public long getVertexCapacity()
    {
        return vertexCapacity;
    }

    public long getIndexCapacity()
    {
        return indexCapacity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLMesh glMesh = (GLMesh) o;
        return vertexArray == glMesh.vertexArray && vertexBuffer == glMesh.vertexBuffer && indexBuffer == glMesh.indexBuffer && Objects.equals(attributes, glMesh.attributes);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(vertexArray, vertexBuffer, indexBuffer, attributes);
    }

    @Override
    public String toString()
    {
        return "GLMesh{" + "vertexArray=" + vertexArray + ", vertexBuffer=" + vertexBuffer + ", indexBuffer=" + indexBuffer + ", vertexCount=" + vertexCount + ", indexCount=" + indexCount + ", attributes=" + attributes + '}';
    }
}
