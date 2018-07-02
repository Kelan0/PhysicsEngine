package main.client.rendering.geometry;

import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kelan
 */
public class PolygonTriangulator
{
    private List<Vertex2D> points;
    private List<Vertex2D> nonConvexPoints;
    private boolean clockwise;

    public PolygonTriangulator(List<Vertex2D> points)
    {
        this.points = points;
        this.nonConvexPoints = new ArrayList<>();

        this.calculateWindingOrder();
        this.separateConvexPoints();
    }

    private void calculateWindingOrder()
    {
        int index = 0;
        Vector2f p0 = points.get(0);
        for (int i = 1; i < points.size(); i++)
        {
            Vector2f p1 = points.get(i);
            if (p1.x < p0.x)
            {
                p0 = p1;
                index = i;
            } else if (p1.x == p0.x && p1.y > p0.y)
            {
                p0 = p1;
                index = i;
            }
        }

        Vector2f v0 = points.get(getIndex(index, -1));
        Vector2f v1 = new Vector2f(p0.x - v0.x, p0.y - v0.y);
        Vector2f v2 = points.get(getIndex(index, +1));

        float res = v2.x * v1.y - v2.y * v1.x + v1.x * v0.y - v1.y * v0.x;

        this.clockwise = (res <= 0.0F);
    }

    private void separateConvexPoints()
    {
        for (int i = 0; i < points.size() - 1; i++)
        {
            Vertex2D p0 = points.get(getIndex(i, 0));//(i + 0) % (points.size() - 1));
            Vertex2D p1 = points.get(getIndex(i, 1));//(i + 1) % (points.size() - 1));
            Vertex2D p2 = points.get(getIndex(i, 2));//(i + 2) % (points.size() - 1));
            Vector2f v = Vector2f.sub(p1, p0, null);

            float f = p2.x * v.y - p2.y * v.x + v.x * p0.y - v.y * p0.x;

            if ((f > 0 && clockwise) || (f <= 0 && !clockwise) && !nonConvexPoints.contains(p1))
            {
                nonConvexPoints.add(p1);
            }
        }
    }

    public List<Triangle2D> generateTriangles()
    {
        List<Triangle2D> triangles = new ArrayList<>();

        triangles.clear();
        int index = 1;

        while (points.size() > 3)
        {
            if (isEar(points.get(getIndex(index, -1)), points.get(index), points.get(getIndex(index, 1))))
            {
                triangles.add(new Triangle2D(points.get(getIndex(index, -1)), points.get(getIndex(index, 0)), points.get(getIndex(index, 1))));

                points.remove(points.get(index));

                index = getIndex(index, -1);

            } else
            {
                index = getIndex(index, 1);
            }
        }

        triangles.add(new Triangle2D(points.get(0), points.get(1), points.get(2)));

        return triangles;
    }

    private boolean isEar(Vector2f v0, Vector2f v1, Vector2f v2)
    {
        if (!isConvex(v0, v1, v2))
        {
            return false;
        }

        for (Vertex2D point : nonConvexPoints)
        {
            if (insideTriangle(v0, v1, v2, point))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isConvex(Vector2f v0, Vector2f v1, Vector2f v2)
    {
        Vector2f v = new Vector2f(v1.x - v0.x, v1.y - v0.y);

        float f = v2.x * v.y - v2.y * v.x + v.x * v0.y - v.y * v0.x;

        return !((f > 0 && clockwise) || (f <= 0 && !clockwise));
    }

    private int getIndex(int index, int offset)
    {
        if (index + offset > points.size() - 1)
        {
            return points.size() - (index + offset);
        } else if (index + offset < 0)
        {
            return points.size() + (index + offset);
        } else
        {
            return index + offset;
        }
    }

    private boolean insideTriangle(Vector2f v0, Vector2f v1, Vector2f v2, Vector2f p)
    {
        Vector2f a = new Vector2f(v1.x - v0.x, v1.y - v0.y);
        Vector2f b = new Vector2f(v2.x - v0.x, v2.y - v0.y);
        Vector2f c = new Vector2f(p.x - v0.x, p.y - v0.y);

        double d0 = a.x * b.y - b.x * a.y;
        double d1 = (c.x * b.y - b.x * c.y) / d0;
        double d2 = (a.x * c.y - c.x * a.y) / d0;

        return (d1 > 0.0 && d2 > 0.0 && (d1 + d2) < 1.0);
    }

    public static class Triangle2D
    {
        public Vertex2D v0;
        public Vertex2D v1;
        public Vertex2D v2;

        public Triangle2D(Vertex2D v0, Vertex2D v1, Vertex2D v2)
        {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
        }
    }

    public static class Vertex2D extends Vector2f
    {
        public int index;
        public float distance;

        public Vertex2D(int index, float distance, Vector2f position)
        {
            super(position);
            this.index = index;
            this.distance = distance;
        }
    }
}
