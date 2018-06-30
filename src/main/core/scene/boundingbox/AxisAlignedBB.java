package main.core.scene.boundingbox;

import main.core.util.MathUtils;
import org.lwjgl.util.vector.Vector3f;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Kelan
 */
public class AxisAlignedBB extends AbstractBoundingBox
{
    private Vector3f position;
    private Vector3f halfExtents;

    public AxisAlignedBB(Vector3f v0, Vector3f v1, boolean minmax)
    {
        if (minmax)
        {
            this.position = MathUtils.average(v0, v1);
            this.halfExtents = MathUtils.abs((Vector3f) Vector3f.sub(v1, v0, null).scale(0.5F));
        } else
        {
            this.position = new Vector3f(v0);
            this.halfExtents = new Vector3f(v1);
        }
    }

    public AxisAlignedBB(float x0, float y0, float z0, float x1, float y1, float z1, boolean minmax)
    {
        this(new Vector3f(x0, y0, z0), new Vector3f(x1, y1, z1), minmax);
    }

    public AxisAlignedBB(Vector3f position, Vector3f halfExtents)
    {
        this(position, halfExtents, false);
    }

    public AxisAlignedBB(float xPosition, float yPosition, float zPosition, float xHalfExtent, float yHalfExtent, float zHalfExtent)
    {
        this(new Vector3f(xPosition, yPosition, zPosition), new Vector3f(xHalfExtent, yHalfExtent, zHalfExtent), false);
    }

    /**
     * Checks if 3D point B is fully enclosed by bounding box A. A point exactly on the surface of
     * the bounding box is not considered to be inside.
     *
     * @param a The bounding box to check if encloses B
     * @param b The point to check if is enclosed by A
     * @return true if and only if point B is enclosed by bounding box A.
     */
    public static boolean contains(AxisAlignedBB a, Vector3f b)
    {
        Vector3f min = a.getMin();
        Vector3f max = a.getMax();

        if (b.x < min.x || b.x > max.x) // x axis
            return false;
        if (b.y < min.y || b.y > max.y) // y axis
            return false;
        if (b.z < min.z || b.z > max.z) // z axis
            return false;

        return true;
    }

    /**
     * Checks if bounding box B is fully enclosed inside bounding box A, without intersecting the
     * edges. This is implemented by checking if each of the 8 box vertices is contained.
     *
     * @param a The bounding box to check if encloses B
     * @param b The bounding box to check if is enclosed by A
     * @return true if and only if all vertices of B are contained within A
     * @see AxisAlignedBB#contains(AxisAlignedBB a, Vector3f b)
     */
    public static boolean contains(AxisAlignedBB a, AxisAlignedBB b)
    {
        Vector3f min = b.getMin();
        Vector3f max = b.getMax();

        if (!AxisAlignedBB.contains(a, new Vector3f(min.x, min.y, min.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(max.x, min.y, min.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(min.x, max.y, min.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(max.x, max.y, min.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(min.x, min.y, max.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(max.x, min.y, max.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(min.x, max.y, max.z)))
            return false;
        if (!AxisAlignedBB.contains(a, new Vector3f(max.x, max.y, max.z)))
            return false;

        return true;
    }

    /**
     * Checks if bounding boxes A and B are intersecting with each other. This is implemented by checking
     * aMin < bMax && aMax > bMin for each axis.
     *
     * @param a One of the bounding boxes to check if is intersecting the other
     * @param b One of the bounding boxes to check if is intersecting the other
     * @return true if and only if all axis overlap
     */
    public static boolean intersects(AxisAlignedBB a, AxisAlignedBB b)
    {
        Vector3f aMin = a.getMin();
        Vector3f aMax = a.getMax();
        Vector3f bMin = b.getMin();
        Vector3f bMax = b.getMax();

        return aMin.x < bMax.x && aMax.x > bMin.x && aMin.y < bMax.y && aMax.y > bMin.y && aMin.z < bMax.z && aMax.z > bMin.z;

    }

    public static AxisAlignedBB getSmallestEnclosing(Collection<Vector3f> points)
    {
        if (points != null && !points.isEmpty())
        {
            Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
            Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

            for (Vector3f point : points)
            {
                min.x = Math.min(min.x, point.x);
                min.y = Math.min(min.y, point.y);
                min.z = Math.min(min.z, point.z);
                max.x = Math.max(max.x, point.x);
                max.y = Math.max(max.y, point.y);
                max.z = Math.max(max.z, point.z);
            }

            return new AxisAlignedBB(min, max, true);
        }

        return null;
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(Vector3f position)
    {
        this.position = position;
    }

    public Vector3f getHalfExtents()
    {
        return halfExtents;
    }

    public void setHalfExtents(Vector3f halfExtents)
    {
        this.halfExtents = halfExtents;
    }

    public Vector3f getFullExtents()
    {
        return new Vector3f(this.halfExtents.x * 2.0F, this.halfExtents.y * 2.0F, this.halfExtents.z * 2.0F);
    }

    public Vector3f getMin()
    {
        return Vector3f.sub(this.position, this.halfExtents, null);
    }

    public Vector3f getMax()
    {
        return Vector3f.add(this.position, this.halfExtents, null);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AxisAlignedBB that = (AxisAlignedBB) o;
        return Objects.equals(position, that.position) && Objects.equals(halfExtents, that.halfExtents);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(position, halfExtents);
    }

    @Override
    public String toString()
    {
        return "AxisAlignedBB{" + "position=" + position + ", halfExtents=" + halfExtents + '}';
    }

    public static Vector3f getFurthestPoint(AxisAlignedBB aabb, Vector3f direction)
    {
        Vector3f point = new Vector3f();
        Vector3f min = aabb.getMin();
        Vector3f max = aabb.getMax();

        if (direction.x < 0.0F) // get furthest x value in the direction of the planes normal.
            point.x = min.x;
        else
            point.x = max.x;

        if (direction.y < 0.0F) // get furthest y value in the direction of the planes normal.
            point.y = min.y;
        else
            point.y = max.y;

        if (direction.z < 0.0F) // get furthest z value in the direction of the planes normal.
            point.z = min.z;
        else
            point.z = max.z;

        return point;
    }
}
