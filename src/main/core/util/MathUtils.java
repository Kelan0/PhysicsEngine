package main.core.util;

import org.lwjgl.util.vector.*;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * @author Kelan
 */
public class MathUtils
{
    public static Vector3f rotateVector3f(Quaternion quat, Vector3f vec, Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        return Matrix3f.transform(quaternionToMatrix3f(quat, null), vec, dest);
    }

    public static Vector4f rotateVector4f(Quaternion quat, Vector4f vec, Vector4f dest)
    {
        if (dest == null)
        {
            dest = new Vector4f();
        }

        return Matrix4f.transform(quaternionToMatrix4f(quat, null), vec, dest);
    }

    public static float getAngleVector3f(Vector3f a, Vector3f b)
    {
        return (float) Math.acos(Vector3f.dot(a, b) / a.length() * b.length());
    }

    public static float interpolate(double v1, double v2, double d)
    {
        return interpolate(new Vector2f((float) v1, 0.0F), new Vector2f((float) v2, 0.0F), d).x;
    }

    public static Vector2f interpolate(Vector2f v1, Vector2f v2, double d)
    {
        return new Vector2f(interpolate(new Vector3f(v1.x, v1.y, 0.0F), new Vector3f(v2.x, v2.y, 0.0F), d));
    }

    public static Vector3f interpolate(Vector3f v1, Vector3f v2, double d)
    {
        return new Vector3f(interpolate(new Vector4f(v1.x, v1.y, v1.z, 0.0F), new Vector4f(v2.x, v2.y, v2.z, 0.0F), d));
    }

    public static Vector4f interpolate(Vector4f v1, Vector4f v2, double d)
    {
        float x = (float) (v1.x + d * (v2.x - v1.x));
        float y = (float) (v1.y + d * (v2.y - v1.y));
        float z = (float) (v1.z + d * (v2.z - v1.z));
        float w = (float) (v1.w + d * (v2.w - v1.w));
        return new Vector4f(x, y, z, w);
    }

    public static Vector2f direction(Vector2f a, Vector2f b)
    {
        return Vector2f.sub(b, a, null);
    }

    public static Vector3f direction(Vector3f a, Vector3f b)
    {
        return Vector3f.sub(b, a, null);
    }

    public static Vector4f direction(Vector4f a, Vector4f b)
    {
        return Vector4f.sub(b, a, null);
    }

    public static float distanceSquared(Vector2f a, Vector2f b)
    {
        return direction(a, b).lengthSquared();
    }

    public static float distanceSquared(Vector3f a, Vector3f b)
    {
        return direction(a, b).lengthSquared();
    }

    public static float distanceSquared(Vector4f a, Vector4f b)
    {
        return direction(a, b).lengthSquared();
    }

    public static float distance(Vector2f a, Vector2f b)
    {
        return direction(a, b).length();
    }

    public static float distance(Vector3f a, Vector3f b)
    {
        return direction(a, b).length();
    }

    public static float distance(Vector4f a, Vector4f b)
    {
        return direction(a, b).length();
    }

    public static Vector3f reflect(Vector3f vector, Vector3f normal, Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        float vDotN = Vector3f.dot(vector, normal);
        dest.x = -2.0F * vDotN * normal.x + vector.x;
        dest.y = -2.0F * vDotN * normal.y + vector.y;
        dest.z = -2.0F * vDotN * normal.z + vector.z;

        return dest;
    }

    public static <T> void shiftArray(T[] arr, T replace, int d)
    {
        for (int i = d < 0 ? arr.length - 1 : 0; d < 0 ? i >= 0 : i < arr.length; i += d < 0 ? -1 : +1)
        {
            if (d < 0 ? i + d >= 0 : i + d < arr.length)
                arr[i] = arr[i + d];
            else
                arr[i] = replace;
        }
    }

    public static float[] getVectorArray4(ReadableVector4f vector)
    {
        return new float[]{vector.getX(), vector.getY(), vector.getZ(), vector.getW()};
    }

    public static float[] getVectorArray3(ReadableVector3f vector)
    {
        return new float[]{vector.getX(), vector.getY(), vector.getZ()};
    }

    public static float[] getVectorArray2(ReadableVector2f vector)
    {
        return new float[]{vector.getX(), vector.getY()};
    }

    public static Vector4f getArrayVector4(float[] array)
    {
        return new Vector4f(array[0], array[1], array[2], array[3]);
    }

    public static Vector3f getArrayVector3(float[] array)
    {
        return new Vector3f(array[0], array[1], array[2]);
    }

    public static Vector2f getArrayVector2(float[] array)
    {
        return new Vector2f(array[0], array[1]);
    }

    public static Vector4f setArrayVector4(float[] array, Vector4f vector)
    {
        return vector.set(getArrayVector4(array));
    }

    public static Vector3f setArrayVector3(float[] array, Vector3f vector)
    {
        return vector.set(getArrayVector3(array));
    }

    public static Vector2f setArrayVector2(float[] array, Vector2f vector)
    {
        return vector.set(getArrayVector2(array));
    }

    public static float dotPerp(Vector2f v1, Vector2f v2)
    {
        return v1.x * v2.y - v1.y * v2.x;
    }

    public static void generateComplementBasis(Vector3f u, Vector3f v, Vector3f w)
    {
        float invLength;

        if (Math.abs(w.x) >= Math.abs(w.y))
        {
            invLength = (float) (1.0F / Math.sqrt(w.x * w.x + w.z * w.z));
            u.x = -w.z * invLength;
            u.y = 0.0F;
            u.z = +w.x * invLength;
            v.x = w.y * u.z;
            v.y = w.z * u.x - w.x * u.z;
            v.z = -w.y * u.x;
        } else
        {
            invLength = (float) (1.0F / Math.sqrt(w.y * w.y + w.z * w.z));
            u.x = 0.0F;
            u.y = +w.z * invLength;
            u.z = -w.y * invLength;
            v.x = w.y * u.z - w.z * u.y;
            v.y = -w.x * u.z;
            v.z = w.x * u.y;
        }
    }

    public static Quaternion add(Quaternion left, Quaternion right, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.x = left.x + right.x;
        dest.y = left.y + right.y;
        dest.z = left.z + right.z;
        dest.w = left.w + right.w;

        return dest;
    }

    public static Quaternion quaternionDifference(Quaternion a, Quaternion b)
    {
        return Quaternion.mul(Quaternion.negate(a, null), b, null);
    }

    public static Quaternion axisAngleToQuaternion(Vector3f axis, float radians, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        if (radians != 0.0F && axis.lengthSquared() > 0.0F)
        {
            dest.setFromAxisAngle(new Vector4f(axis.x, axis.y, axis.z, radians));
        }

        return dest;
    }

    public static Quaternion matrix4fToQuaternion(Matrix4f mat, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.setFromMatrix(mat);

        return dest;
    }

    public static Quaternion matrix3fToQuaternion(Matrix3f mat, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.setFromMatrix(mat);

        return dest;
    }

    public static Matrix4f quaternionToMatrix4f(Quaternion quat, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        if (quat.lengthSquared() > 0.0F)
        {
            quat.normalise();
        }

        float s = 2.0F;

        dest.m00 = 1.0F - s * (quat.y * quat.y + quat.z * quat.z);
        dest.m10 = s * (quat.x * quat.y + quat.w * quat.z);
        dest.m20 = s * (quat.x * quat.z - quat.w * quat.y);
        dest.m01 = s * (quat.x * quat.y - quat.w * quat.z);
        dest.m11 = 1.0F - s * (quat.x * quat.x + quat.z * quat.z);
        dest.m21 = s * (quat.y * quat.z + quat.w * quat.x);
        dest.m02 = s * (quat.x * quat.z + quat.w * quat.y);
        dest.m12 = s * (quat.y * quat.z - quat.w * quat.x);
        dest.m22 = 1.0F - s * (quat.x * quat.x + quat.y * quat.y);

        return dest;
    }

    public static Matrix3f quaternionToMatrix3f(Quaternion quat, Matrix3f dest)
    {
        return matrix4fToMatrix3f(quaternionToMatrix4f(quat, null), dest);
    }

    public static Matrix3f matrix4fToMatrix3f(Matrix4f matrix, Matrix3f dest)
    {
        if (dest == null)
        {
            dest = new Matrix3f();
        }

        dest.m00 = matrix.m00;
        dest.m01 = matrix.m01;
        dest.m02 = matrix.m02;
        dest.m10 = matrix.m10;
        dest.m11 = matrix.m11;
        dest.m12 = matrix.m12;
        dest.m20 = matrix.m20;
        dest.m21 = matrix.m21;
        dest.m22 = matrix.m22;

        return dest;
    }

    public static Matrix4f matrix3fToMatrix4f(Matrix3f matrix, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = matrix.m00;
        dest.m01 = matrix.m01;
        dest.m02 = matrix.m02;
        dest.m10 = matrix.m10;
        dest.m11 = matrix.m11;
        dest.m12 = matrix.m12;
        dest.m20 = matrix.m20;
        dest.m21 = matrix.m21;
        dest.m22 = matrix.m22;

        return dest;
    }

    public static Matrix3f computeCovarianceMatrix(Vector3f... points)
    {
        if (points != null && points.length > 0)
        {
            float[] averageArr = getVectorArray3(average(points));
            float[][] covarianceMatrix = new float[3][3];

            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    for (Vector3f point : points)
                    {
                        float[] pointArr = getVectorArray3(point);

                        covarianceMatrix[i][j] += (averageArr[i] - pointArr[i]) * (averageArr[j] - pointArr[j]);
                    }

                    covarianceMatrix[i][j] /= points.length - 1;
                }
            }

            Matrix3f matrix = new Matrix3f();

            matrix.m00 = covarianceMatrix[0][0];
            matrix.m01 = covarianceMatrix[0][1];
            matrix.m02 = covarianceMatrix[0][2];
            matrix.m10 = covarianceMatrix[1][0];
            matrix.m11 = covarianceMatrix[1][1];
            matrix.m12 = covarianceMatrix[1][2];
            matrix.m20 = covarianceMatrix[2][0];
            matrix.m21 = covarianceMatrix[2][1];
            matrix.m22 = covarianceMatrix[2][2];

            return matrix;
        }

        return null;
    }

    public static Vector3f closestPointOnTriangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f point)
    {
        Vector3f aEdge = Vector3f.sub(v1, v0, null);
        Vector3f bEdge = Vector3f.sub(v2, v0, null);
        Vector3f relativePoint = Vector3f.sub(v0, point, null);

        float aEdgeLengthSq = aEdge.lengthSquared();
        float bEdgeLengthSq = bEdge.lengthSquared();
        float aDotb = Vector3f.dot(aEdge, bEdge);
        float aDotRel = Vector3f.dot(aEdge, relativePoint);
        float bDotRel = Vector3f.dot(bEdge, relativePoint);

        float barycentricScalar = aEdgeLengthSq * bEdgeLengthSq - aDotb * aDotb;
        float u = aDotb * bDotRel - bEdgeLengthSq * aDotRel;
        float v = aDotb * aDotRel - aEdgeLengthSq * bDotRel;

        if (u + v < barycentricScalar)
        {
            if (u < 0.0F)
            {
                if (v < 0.0F)
                {
                    if (aDotRel < 0.0F)
                    {
                        u = clamp(-aDotRel / aEdgeLengthSq, 0.0F, 1.0F);
                        v = 0.0F;
                    } else
                    {
                        u = 0.0F;
                        v = clamp(-bDotRel / bEdgeLengthSq, 0.0F, 1.0F);
                    }
                } else
                {
                    u = 0.0F;
                    v = clamp(-bDotRel / bEdgeLengthSq, 0.0F, 1.0F);
                }
            } else if (v < 0.0F)
            {
                u = clamp(-aDotRel / aEdgeLengthSq, 0.0F, 1.0F);
                v = 0.0F;
            } else
            {
                float invDet = 1.0F / barycentricScalar;
                u *= invDet;
                v *= invDet;
            }
        } else
        {
            if (u < 0.0F)
            {
                float tmp0 = aDotb + aDotRel;
                float tmp1 = bEdgeLengthSq + bDotRel;
                if (tmp1 > tmp0)
                {
                    float numer = tmp1 - tmp0;
                    float denom = aEdgeLengthSq - 2 * aDotb + bEdgeLengthSq;
                    u = clamp(numer / denom, 0.0F, 1.0F);
                    v = 1.0F - u;
                } else
                {
                    v = clamp(-bDotRel / bEdgeLengthSq, 0.0F, 1.0F);
                    u = 0.0F;
                }
            } else if (v < 0.0F)
            {
                if (aEdgeLengthSq + aDotRel > aDotb + bDotRel)
                {
                    float numer = bEdgeLengthSq + bDotRel - aDotb - aDotRel;
                    float denom = aEdgeLengthSq - 2 * aDotb + bEdgeLengthSq;
                    u = clamp(numer / denom, 0.0F, 1.0F);
                    v = 1 - u;
                } else
                {
                    u = clamp(-bDotRel / bEdgeLengthSq, 0.0F, 1.0F);
                    v = 0.0F;
                }
            } else
            {
                float numer = bEdgeLengthSq + bDotRel - aDotb - aDotRel;
                float denom = aEdgeLengthSq - 2 * aDotb + bEdgeLengthSq;
                u = clamp(numer / denom, 0.0F, 1.0F);
                v = 1.0F - u;
            }
        }

        return Vector3f.add(v0, Vector3f.add((Vector3f) aEdge.scale(u), (Vector3f) bEdge.scale(v), null), null);
    }

    public static float triangleDistanceSquared(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f point)
    {
        return distanceSquared(closestPointOnTriangle(v0, v1, v2, point), point);
    }

    public static float triangleDistance(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f point)
    {
        return distance(closestPointOnTriangle(v0, v1, v2, point), point);
    }

    public static float average(double... values)
    {
        if (values != null && values.length > 0)
        {
            float x = 0.0F;

            for (double v : values)
            {
                x += v;
            }

            return x / values.length;
        }

        return Float.NaN;
    }

    public static float average(float... values)
    {
        if (values != null && values.length > 0)
        {
            float x = 0.0F;

            for (float v : values)
            {
                x += v;
            }

            return x / values.length;
        }

        return Float.NaN;
    }

    public static Vector2f average(Vector2f... vectors)
    {
        if (vectors != null && vectors.length > 0)
        {
            float x = 0.0F;
            float y = 0.0F;

            for (Vector2f v : vectors)
            {
                x += v.x;
                y += v.y;
            }

            return new Vector2f(x / vectors.length, y / vectors.length);
        }

        return null;
    }

    public static Vector3f average(Vector3f... vectors)
    {
        if (vectors != null && vectors.length > 0)
        {
            float x = 0.0F;
            float y = 0.0F;
            float z = 0.0F;

            for (Vector3f v : vectors)
            {
                x += v.x;
                y += v.y;
                z += v.z;
            }

            return new Vector3f(x / vectors.length, y / vectors.length, z / vectors.length);
        }

        return null;
    }

    public static Vector4f average(Vector4f... vectors)
    {
        if (vectors != null && vectors.length > 0)
        {
            float x = 0.0F;
            float y = 0.0F;
            float z = 0.0F;
            float w = 0.0F;

            for (Vector4f v : vectors)
            {
                x += v.x;
                y += v.y;
                z += v.z;
                w += v.w;
            }

            return new Vector4f(x / vectors.length, y / vectors.length, z / vectors.length, w / vectors.length);
        }

        return null;
    }

    public static float average(List<Float> vectors)
    {
        if (vectors != null && !vectors.isEmpty())
        {
            float x = 0.0F;

            for (float v : vectors)
            {
                x += v;
            }

            return x / vectors.size();
        }

        return Float.NaN;
    }

    public static Vector2f average(List<Vector2f> vectors, Vector2f dest)
    {
        if (vectors != null && !vectors.isEmpty())
        {
            if (dest == null)
                dest = new Vector2f();
            else
                dest.set(new Vector3f());

            for (Vector2f v : vectors)
            {
                dest.x += v.x;
                dest.y += v.y;
            }

            return (Vector2f) dest.scale(1.0F / vectors.size());
        }

        return null;
    }

    public static Vector3f average(List<Vector3f> vectors, Vector3f dest)
    {
        if (vectors != null && !vectors.isEmpty())
        {
            if (dest == null)
                dest = new Vector3f();
            else
                dest.set(new Vector3f());

            for (Vector3f v : vectors)
            {
                dest.x += v.x;
                dest.y += v.y;
                dest.z += v.z;
            }

            return (Vector3f) dest.scale(1.0F / vectors.size());
        }

        return null;
    }

    public static Vector4f average(List<Vector4f> vectors, Vector4f dest)
    {
        if (vectors != null && !vectors.isEmpty())
        {
            if (dest == null)
                dest = new Vector4f();
            else
                dest.set(new Vector4f());

            for (Vector4f v : vectors)
            {
                dest.x += v.x;
                dest.y += v.y;
                dest.z += v.z;
                dest.w += v.w;
            }

            return (Vector4f) dest.scale(1.0F / vectors.size());
        }

        return null;
    }

    public static Vector2f getClosest(Vector2f vector, Vector2f... vectors)
    {
        Vector2f closest = null;
        float distance = Float.MAX_VALUE;

        for (Vector2f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                closest = v;
                distance = a;
            }
        }

        return closest;
    }

    public static Vector3f getClosest(Vector3f vector, Vector3f... vectors)
    {
        Vector3f closest = null;
        float distance = Float.MAX_VALUE;

        for (Vector3f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                closest = v;
                distance = a;
            }
        }

        return closest;
    }

    public static Vector4f getClosest(Vector4f vector, Vector4f... vectors)
    {
        Vector4f closest = null;
        float distance = Float.MAX_VALUE;

        for (Vector4f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                closest = v;
                distance = a;
            }
        }

        return closest;
    }

    public static Vector2f getClosest(Vector2f vector, List<Vector2f> vectors, Vector2f dest)
    {
        if (dest == null)
            dest = new Vector2f();

        float distance = Float.MAX_VALUE;

        for (Vector2f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                dest.set(v);
                distance = a;
            }
        }

        return dest;
    }

    public static Vector3f getClosest(Vector3f vector, List<Vector3f> vectors, Vector3f dest)
    {
        if (dest == null)
            dest = new Vector3f();

        float distance = Float.MAX_VALUE;

        for (Vector3f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                dest.set(v);
                distance = a;
            }
        }

        return dest;
    }

    public static Vector4f getClosest(Vector4f vector, List<Vector4f> vectors, Vector4f dest)
    {
        if (dest == null)
            dest = new Vector4f();

        float distance = Float.MAX_VALUE;

        for (Vector4f v : vectors)
        {
            float a = distanceSquared(v, vector);

            if (a < distance)
            {
                dest.set(v);
                distance = a;
            }
        }

        return dest;
    }

    public static Vector3f quaternionToEuler(Quaternion orientation, Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        dest.x = (float) Math.atan2(2.0F * orientation.x * orientation.w - 2.0F * orientation.y * orientation.z, 1.0F - 2.0F * orientation.x * orientation.x - 2.0F * orientation.z * orientation.z);
        dest.y = (float) Math.atan2(2.0F * orientation.y * orientation.w - 2.0F * orientation.x * orientation.z, 1.0F - 2.0F * orientation.y * orientation.y - 2.0F * orientation.z * orientation.z);
        dest.z = (float) Math.asin(2.0F * orientation.x * orientation.y + 2.0F * orientation.z * orientation.w);

        return dest;
    }

    public static int clamp(int val, int min, int max)
    {
        return (int) clamp((float) val, (float) min, (float) max);
    }

    public static float clamp(double val, double min, double max)
    {
        return clamp((float) val, (float) min, (float) max);
    }

    public static float clamp(float val, float min, float max)
    {
        return clamp(new Vector2f(val, 0.0F), new Vector2f(min, 0.0F), new Vector2f(max, 0.0F)).x;
    }

    public static Vector2f clamp(Vector2f val, float min, float max)
    {
        return clamp(val, new Vector2f(min, min), new Vector2f(max, max));
    }

    public static Vector3f clamp(Vector3f val, float min, float max)
    {
        return clamp(val, new Vector3f(min, min, min), new Vector3f(max, max, max));
    }

    public static Vector4f clamp(Vector4f val, float min, float max)
    {
        return clamp(val, new Vector4f(min, min, min, min), new Vector4f(max, max, max, max));
    }

    public static Vector2f clamp(Vector2f val, Vector2f min, Vector2f max)
    {
        return new Vector2f(clamp(new Vector3f(val.x, val.y, 0.0F), new Vector3f(min.x, min.y, 0.0F), new Vector3f(max.x, max.y, 0.0F)));
    }

    public static Vector3f clamp(Vector3f val, Vector3f min, Vector3f max)
    {
        return new Vector3f(clamp(new Vector4f(val.x, val.y, val.z, 0.0F), new Vector4f(min.x, min.y, min.z, 0.0F), new Vector4f(max.x, max.y, max.z, 0.0F)));
    }

    public static Vector4f clamp(Vector4f val, Vector4f min, Vector4f max)
    {
        float x = Math.max(Math.min(val.x, Math.max(min.x, max.x)), Math.min(min.x, max.x));
        float y = Math.max(Math.min(val.y, Math.max(min.y, max.y)), Math.min(min.y, max.y));
        float z = Math.max(Math.min(val.z, Math.max(min.z, max.z)), Math.min(min.z, max.z));
        float w = Math.max(Math.min(val.w, Math.max(min.w, max.w)), Math.min(min.w, max.w));

        return new Vector4f(x, y, z, w);
    }

    public static float sign(float f)
    {
        return f > 0.0F ? +1.0F : f < 0.0F ? -1.0F : 0.0F;
    }

    public static Vector3f setLength(float length, Vector3f vector, Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        vector.normalise(dest).scale(length);
        return dest;
    }

    public static Vector3f abs(Vector3f vector)
    {
        return new Vector3f(Math.abs(vector.x), Math.abs(vector.y), Math.abs(vector.z));
    }

    public static boolean getRoots(float a, float b, float c, FloatBuffer buf)
    {
        float determinant = b * b - 4.0F * a * c;
        if (determinant >= 0.0F)
        {
            float sqrtD = (float) Math.sqrt(determinant);
            float temp1 = (-b - sqrtD) / (2.0F * a);
            float temp2 = (-b + sqrtD) / (2.0F * a);
            float x1 = Math.min(temp1, temp2);
            float x2 = Math.max(temp1, temp2);

            buf.put(x1).put(x2).flip();
            return true;
        } else
        {
            buf.put(Float.NaN).put(Float.NaN).flip();
            return false;
        }
    }

    public static float getInterpolatedTriangleCoordinate(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos)
    {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0F - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static float getSignedPlaneDistance(Vector3f point, Vector4f plane)
    {
        return plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
    }

    public static float getUnsignedLineSegmentDistance(Vector3f point, Vector3f p0, Vector3f p1)
    {
        Vector3f ab = Vector3f.sub(p1, p0, null);

        Vector3f av = Vector3f.sub(point, p0, null);
        if (Vector3f.dot(av, ab) <= 0.0)            // Point is lagging behind start of the segment, so perpendicular distance is not viable.
            return av.length();                     // Use distance to start of segment instead.

        Vector3f bv = Vector3f.sub(point, p1, null);
        if (Vector3f.dot(bv, ab) >= 0.0)            // Point is advanced past the end of the segment, so perpendicular distance is not viable.
            return bv.length();                     // Use distance to end of the segment instead.

        return Vector3f.cross(ab, av, null).length() / ab.length();       // Perpendicular distance of point to segment.
    }

    public static float getUnsignedLineDistance(Vector3f point, Vector3f linePoint, Vector3f lineDirection)
    {
        return Vector3f.cross(lineDirection, Vector3f.sub(point, linePoint, null), null).length() / lineDirection.length();
    }

    public static float getColourBrightness(Color colour)
    {
        float r = colour.getRed() / 255.0F;
        float g = colour.getGreen() / 255.0F;
        float b = colour.getBlue() / 255.0F;

        return (0.299F * r + 0.587F * g + 0.114F * b);
    }

    public static Vector4f getColour(Color colour)
    {
        return new Vector4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
    }
}
