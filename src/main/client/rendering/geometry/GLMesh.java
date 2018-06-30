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

        this.createBuffers();

        if (meshData != null)
        {
            this.allocateBuffers(meshData.getNumVertices(), meshData.getNumIndices());
            this.uploadMeshData(meshData);
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
        this.vertexArray = glGenVertexArrays();
        this.vertexBuffer = glGenBuffers();
        this.indexBuffer = glGenBuffers();
    }

    public GLMesh uploadMeshData(MeshData meshData)
    {
//        System.out.println("Uploading (vertices / indices) " + meshData.getNumVertices() + " / " + meshData.getNumVertices() + " to mesh position " + vertexCount + " / " + indexCount + " out of " + vertexCapacity + " / " + indexCapacity);

        if (meshData != null)
        {
            glBindVertexArray(this.vertexArray);

            if (meshData.hasVertices())
            {
                FloatBuffer vertexBufferData = meshData.getVertexBufferData(this.vertexData);
                glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffer);
                glBufferSubData(GL_ARRAY_BUFFER, this.vertexCount * Vertex.BYTES, vertexBufferData);
                this.vertexCount += meshData.getNumVertices();
            }

            if (meshData.hasIndices())
            {
                IntBuffer indexBufferData = meshData.getIndexBufferData(this.indexData);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
                glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, this.indexCount * Integer.BYTES, indexBufferData);
                this.indexCount += meshData.getNumIndices();
            }

            this.attributes.enableVertexAttributes();
            this.attributes.bindVertexAttributes();
            this.attributes.disableVertexAttributes();

            glBindVertexArray(0);
        }

        return this;
    }

    public GLMesh allocateBuffers(long vertexCapacity, long indexCapacity)
    {
        this.reset();

        glBindVertexArray(this.vertexArray);

        this.vertexCapacity = vertexCapacity;
        this.indexCapacity = indexCapacity;

        this.vertexData = BufferUtils.createFloatBuffer((int) vertexCapacity * (Vertex.BYTES / Float.BYTES));
        this.indexData = BufferUtils.createIntBuffer((int) indexCapacity);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertexCapacity * Vertex.BYTES, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexCapacity * Integer.BYTES, GL_DYNAMIC_DRAW);

        this.attributes.enableVertexAttributes();
        this.attributes.bindVertexAttributes();
        this.attributes.disableVertexAttributes();
        glBindVertexArray(0);

        return this;
    }

    public GLMesh allocateBuffers(FloatBuffer vertexBuffer, IntBuffer indexBuffer)
    {
        this.reset();

        glBindVertexArray(this.vertexArray);

        this.vertexCapacity = vertexBuffer.capacity() / (Vertex.BYTES / Float.BYTES);
        this.indexCapacity = indexBuffer.capacity();

        this.vertexData = vertexBuffer;
        this.indexData = indexBuffer;

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertexCapacity * Vertex.BYTES, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexCapacity * Integer.BYTES, GL_DYNAMIC_DRAW);

        this.attributes.enableVertexAttributes();
        this.attributes.bindVertexAttributes();
        this.attributes.disableVertexAttributes();
        glBindVertexArray(0);

        return this;
    }

    public GLMesh reset()
    {
        if (this.vertexData != null)
            this.vertexData.clear();

        if (this.indexData != null)
            this.indexData.clear();

        this.vertexCount = 0;
        this.indexCount = 0;

        return this;
    }

    public GLMesh draw()
    {
        glBindVertexArray(this.vertexArray);

        this.attributes.enableVertexAttributes();

        if (this.vertexCount > 0)
        {
            if (this.indexCount > 0)
                glDrawElements(GL_TRIANGLES, (int) this.indexCount, GL_UNSIGNED_INT, 0);
            else
                glDrawArrays(GL_TRIANGLES, 0, (int) this.vertexCount);
        }

        this.attributes.disableVertexAttributes();

        glBindVertexArray(0);

        return this;
    }

    public void dispose()
    {
        glDeleteBuffers(this.vertexBuffer);
        glDeleteBuffers(this.indexBuffer);
        glDeleteVertexArrays(this.vertexArray);
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
