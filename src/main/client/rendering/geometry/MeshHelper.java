package main.client.rendering.geometry;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;

/**
 * @author Kelan
 */
public class MeshHelper
{
    public static MeshData createCuboid(float min, float max)
    {
        return createCuboid(new Vector3f(min, min, min), new Vector3f(max, max, max));
    }

    public static MeshData createCuboid(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax)
    {
        return createCuboid(new Vector3f(xMin, yMin, zMin), new Vector3f(xMax, yMax, zMax));
    }

    public static MeshData createCuboid(Vector3f min, Vector3f max)
    {
        float xMin = Math.min(min.x, max.x);
        float yMin = Math.min(min.y, max.y);
        float zMin = Math.min(min.z, max.z);
        float xMax = Math.max(min.x, max.x);
        float yMax = Math.max(min.y, max.y);
        float zMax = Math.max(min.z, max.z);

        Integer[] indices = {0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23};
        Vertex[] vertices = new Vertex[24];

        int i = 0;

        // z-neg
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMin), new Vector3f(0.0F, 0.0F, -1.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMin), new Vector3f(0.0F, 0.0F, -1.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMin), new Vector3f(0.0F, 0.0F, -1.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMin), new Vector3f(0.0F, 0.0F, -1.0F), new Vector2f(1.0F, 1.0F));

        // x-neg
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMax), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMax), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMin), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMin), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector2f(1.0F, 1.0F));

        // z-pos
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMax), new Vector3f(0.0F, 0.0F, +1.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMax), new Vector3f(0.0F, 0.0F, +1.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMax), new Vector3f(0.0F, 0.0F, +1.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMax), new Vector3f(0.0F, 0.0F, +1.0F), new Vector2f(1.0F, 1.0F));

        // x-pos
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMin), new Vector3f(+1.0F, 0.0F, 0.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMin), new Vector3f(+1.0F, 0.0F, 0.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMax), new Vector3f(+1.0F, 0.0F, 0.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMax), new Vector3f(+1.0F, 0.0F, 0.0F), new Vector2f(1.0F, 1.0F));

        // y-pos
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMin), new Vector3f(0.0F, +1.0F, 0.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMax, zMax), new Vector3f(0.0F, +1.0F, 0.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMax), new Vector3f(0.0F, +1.0F, 0.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMax, zMin), new Vector3f(0.0F, +1.0F, 0.0F), new Vector2f(1.0F, 1.0F));

        // y-neg
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMin), new Vector3f(0.0F, -1.0F, 0.0F), new Vector2f(0.0F, 1.0F));
        vertices[i++] = new Vertex(new Vector3f(xMax, yMin, zMax), new Vector3f(0.0F, -1.0F, 0.0F), new Vector2f(0.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMax), new Vector3f(0.0F, -1.0F, 0.0F), new Vector2f(1.0F, 0.0F));
        vertices[i++] = new Vertex(new Vector3f(xMin, yMin, zMin), new Vector3f(0.0F, -1.0F, 0.0F), new Vector2f(1.0F, 1.0F));

        return new MeshData(Arrays.asList(vertices), Arrays.asList(indices));
    }


    public static MeshData createQuad(float width, float height, int xDivisions, int yDivisions)
    {
        int xResolution = Math.max(1, xDivisions);
        int yResolution = Math.max(1, yDivisions);
        int xVertices = xResolution + 1;
        int yVertices = yResolution + 1;

        Vertex[] vertices = new Vertex[xVertices * yVertices];
        Integer[] indices = new Integer[xResolution * yResolution * 6];

        for (int i = 0; i < xVertices; i++)
        {
            for (int j = 0; j < yVertices; j++)
            {
                float x = ((float) i / xResolution) * width;
                float y = ((float) j / yResolution) * height;

                vertices[yVertices * j + i] = new Vertex(new Vector3f(x, 0.0F, y), new Vector3f(0.0F, 1.0F, 0.0F), new Vector2f(x, y));
            }
        }

        int index = 0;
        for (int i = 0; i < xResolution; i++)
        {
            for (int j = 0; j < yResolution; j++)
            {
                int i00 = yVertices * (j + 0) + (i + 0); // Top left.
                int i10 = yVertices * (j + 1) + (i + 0); // Top right.
                int i01 = yVertices * (j + 0) + (i + 1); // Bottom left.
                int i11 = yVertices * (j + 1) + (i + 1); // Bottom right.

                indices[index++] = i00;
                indices[index++] = i10;
                indices[index++] = i01;
                indices[index++] = i10;
                indices[index++] = i11;
                indices[index++] = i01;
            }
        }

        return new MeshData(Arrays.asList(vertices), Arrays.asList(indices));
    }

    public static MeshData createEllipsoid(float xRadius, float yRadius, float zRadius)
    {
        return null;
    }


    public static MeshData createUVSphere(float radius, int rings, int sectors)
    {
        float R = 1.0F / (float) (rings - 1);
        float S = 1.0F / (float) (sectors - 1);
        int r, s;

        Vertex[] vertices = new Vertex[rings * sectors];
        Integer[] indices = new Integer[rings * sectors * 6];

        int pointer = 0;

        for (r = 0; r < rings; r++)
        {
            for (s = 0; s < sectors; s++)
            {
                float x = (float) (Math.cos(2.0F * Math.PI * s * S) * Math.sin(Math.PI * r * R));
                float y = (float) Math.sin(-Math.PI * 0.5F + Math.PI * r * R);
                float z = (float) (Math.sin(2.0F * Math.PI * s * S) * Math.sin(Math.PI * r * R));

                Vector3f v = new Vector3f(x * radius, y * radius, z * radius);
                Vector3f n = new Vector3f(x, y, z);
                Vector2f t = new Vector2f(s * S, r * R);

                vertices[pointer++] = new Vertex(v, n, t);
            }
        }

        pointer = 0;

        for (r = 0; r < rings; r++)
        {
            for (s = 0; s < sectors; s++)
            {
                int r0 = r;
                int r1 = r0 + 1;
                int s0 = s;
                int s1 = s0 + 1;

                indices[pointer++] = r0 * sectors + s0;
                indices[pointer++] = r1 * sectors + s0;
                indices[pointer++] = r0 * sectors + s1;
                indices[pointer++] = r1 * sectors + s0;
                indices[pointer++] = r1 * sectors + s1;
                indices[pointer++] = r0 * sectors + s1;

//                indices[pointer++] = r * sectors + s;
//                indices[pointer++] = r * sectors + (s + 1);
//                indices[pointer++] = (r + 1) * sectors + (s + 1);
//                indices[pointer++] = (r + 1) * sectors + s;
            }
        }

        return new MeshData(Arrays.asList(vertices), Arrays.asList(indices));
    }
}
