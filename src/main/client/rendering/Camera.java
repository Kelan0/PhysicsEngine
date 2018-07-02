package main.client.rendering;

import main.client.rendering.geometry.GLMesh;
import main.client.rendering.geometry.MeshData;
import main.client.rendering.geometry.Vertex;
import main.core.Engine;
import main.core.scene.Component;
import main.core.scene.Transformation;
import main.core.scene.boundingbox.AxisAlignedBB;
import main.core.scene.boundingbox.IntersectionType;
import main.core.util.MathUtils;
import org.lwjgl.util.vector.*;

import java.util.Arrays;

/**
 * @author Kelan
 */
public class Camera extends Component
{
    private Transformation transformationOffset;

    private float near;
    private float far;
    private float fov;

    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();

    private Vector4f[] frustum = new Vector4f[6];
    private GLMesh frustumMesh;

    public Camera(Transformation transformationOffset, float near, float far, float fov)
    {
        this.transformationOffset = transformationOffset;

        this.near = near;
        this.far = far;
        this.fov = fov;
    }

    public Camera(Transformation transformationOffset)
    {
        this(transformationOffset, 0.001F, 1000.0F, 90.0F);
    }

    public Camera(float near, float far, float fov)
    {
        this(new Transformation(), near, far, fov);
    }

    public Camera(float near, float far)
    {
        this(new Transformation(), near, far, 90.0F);
    }

    public Camera(float fov)
    {
        this(new Transformation(), 0.001F, 1000.0F, fov);
    }

    public Camera()
    {
        this(new Transformation(), 0.001F, 1000.0F, 90.0F);
    }

    @Override
    public void render(double delta)
    {
        float aspect = Engine.getClientThread().getWindowAspectRatio();
        float tangent = (float) (1.0 / Math.tan(Math.toRadians(this.fov * 0.5)));

        float right = this.near / tangent * aspect;
        float left = -this.near / tangent * aspect;
        float top = this.near / tangent;
        float bottom = -this.near / tangent;
        float nudge = 1.0F;

        if (this.projectionMatrix == null)
            this.projectionMatrix = new Matrix4f();

        // This is a projection matrix that allows an unlimited far plane :D
        this.projectionMatrix.setZero();
        this.projectionMatrix.m00 = 2.0F * this.near / (right - left);
        this.projectionMatrix.m11 = 2.0F * this.near / (top - bottom);
        this.projectionMatrix.m20 = (right + left) / (right - left);
        this.projectionMatrix.m21 = (top + bottom) / (top - bottom);
        this.projectionMatrix.m22 = nudge * -1.0F;
        this.projectionMatrix.m23 = -1.0F;
        this.projectionMatrix.m32 = this.near * nudge * -2.0F;
//        this.projectionMatrix.m00 = (2.0F * this.near) / (right - left);
//        this.projectionMatrix.m11 = (2.0F * this.near) / (top - bottom);
//        this.projectionMatrix.m20 = (right + left) / (right - left);
//        this.projectionMatrix.m21 = (top + bottom) / (top - bottom);
//        this.projectionMatrix.m22 = -(this.far + this.near) / (this.far - this.near);
//        this.projectionMatrix.m23 = -1.0F;
//        this.projectionMatrix.m32 = -(2.0F * this.far * this.near) / (this.far - this.near);

        this.viewMatrix = MathUtils.quaternionToMatrix4f(this.getRotationWithOffset(), null).translate(this.getPositionWithOffset().negate(null));

        if (Engine.getClientThread().doUpdateFrustum())
        {
            Matrix4f viewProjection = Matrix4f.mul(this.projectionMatrix, this.viewMatrix, null);

            if (this.frustumMesh == null)
                this.frustumMesh = new GLMesh().allocateBuffers(8 * (Vertex.BYTES / Float.BYTES), 24);

            Vector3f p = this.getPositionWithOffset();

            Vector3f forward = getPickingVector(new Vector2f(0.0F, 0.0F));
            Vector3f v00 = getPickingVector(new Vector2f(-1.0F, -1.0F));
            Vector3f v10 = getPickingVector(new Vector2f(+1.0F, -1.0F));
            Vector3f v01 = getPickingVector(new Vector2f(-1.0F, +1.0F));
            Vector3f v11 = getPickingVector(new Vector2f(+1.0F, +1.0F));

            float r00 = 1.0F / Vector3f.dot(forward, v00);
            float r10 = 1.0F / Vector3f.dot(forward, v10);
            float r01 = 1.0F / Vector3f.dot(forward, v01);
            float r11 = 1.0F / Vector3f.dot(forward, v11);

            Vertex[] vertices = new Vertex[]{
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v00).scale(r00 * this.near * 2.0F), p, null)),   // TLN // 0
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v10).scale(r10 * this.near * 2.0F), p, null)),   // TRN // 1
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v01).scale(r11 * this.near * 2.0F), p, null)),   // BLN // 2
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v11).scale(r01 * this.near * 2.0F), p, null)),   // BRN // 3
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v00).scale(r00 * this.far * 2.0F), p, null)),    // TLF // 4
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v10).scale(r10 * this.far * 2.0F), p, null)),    // TRF // 5
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v01).scale(r11 * this.far * 2.0F), p, null)),    // BLF // 6
                    new Vertex(Vector3f.add((Vector3f) new Vector3f(v11).scale(r01 * this.far * 2.0F), p, null)),    // BRF // 7
            };

            Integer[] indices = new Integer[]{
                    6, 2, 4, 2, 0, 4, // left
                    5, 1, 7, 1, 3, 7, // right
                    0, 1, 5, 0, 5, 4, // top
                    2, 6, 3, 6, 7, 3, // bottom
            };

            MeshData mesh = new MeshData(Arrays.asList(vertices), Arrays.asList(indices));

            this.frustumMesh.reset().uploadMeshData(mesh);

            this.updateFrustum(viewProjection);
        }
    }

    @Override
    public void dispose()
    {

    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {
        Matrix4f viewMatrix = this.getViewMatrix();
        Matrix4f projectionMatrix = this.getProjectionMatrix();

        if (viewMatrix != null)
        {
            shaderProgram.setUniformMatrix4f("viewMatrix", viewMatrix);
            shaderProgram.setUniformVector3f("cameraDirection", new Vector3f(viewMatrix.m20, viewMatrix.m21, viewMatrix.m22));
        }

        if (projectionMatrix != null)
        {
            shaderProgram.setUniformMatrix4f("projectionMatrix", projectionMatrix);
        }

        shaderProgram.setUniformVector1f("Fcoef", (float) (2.0 / (Math.log(this.far + 1.0) / Math.log(2.0)))); // For logarithmic depth buffer.
        shaderProgram.setUniformVector1f("nearPlane", this.near);
        shaderProgram.setUniformVector1f("farPlane", this.far);
        shaderProgram.setUniformVector3f("cameraPosition", this.getPositionWithOffset());
    }

    @Override
    public void init()
    {

    }

    @Override
    public void update(double delta)
    {

    }


    public void updateFrustum(Matrix4f viewProjection)
    {
        float t;

        if (frustum[0] == null) frustum[0] = new Vector4f();
        /* Extract the numbers for the RIGHT plane */
        frustum[0].x = viewProjection.m03 - viewProjection.m00;
        frustum[0].y = viewProjection.m13 - viewProjection.m10;
        frustum[0].z = viewProjection.m23 - viewProjection.m20;
        frustum[0].w = viewProjection.m33 - viewProjection.m30;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[0].x * frustum[0].x + frustum[0].y * frustum[0].y + frustum[0].z * frustum[0].z);
        frustum[0].x /= t;
        frustum[0].y /= t;
        frustum[0].z /= t;
        frustum[0].w /= t;

        if (frustum[1] == null) frustum[1] = new Vector4f();
        /* Extract the numbers for the LEFT plane */
        frustum[1].x = viewProjection.m03 + viewProjection.m00;
        frustum[1].y = viewProjection.m13 + viewProjection.m10;
        frustum[1].z = viewProjection.m23 + viewProjection.m20;
        frustum[1].w = viewProjection.m33 + viewProjection.m30;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[1].x * frustum[1].x + frustum[1].y * frustum[1].y + frustum[1].z * frustum[1].z);
        frustum[1].x /= t;
        frustum[1].y /= t;
        frustum[1].z /= t;
        frustum[1].w /= t;

        if (frustum[2] == null) frustum[2] = new Vector4f();
        /* Extract the BOTTOM plane */
        frustum[2].x = viewProjection.m03 + viewProjection.m01;
        frustum[2].y = viewProjection.m13 + viewProjection.m11;
        frustum[2].z = viewProjection.m23 + viewProjection.m21;
        frustum[2].w = viewProjection.m33 + viewProjection.m31;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[2].x * frustum[2].x + frustum[2].y * frustum[2].y + frustum[2].z * frustum[2].z);
        frustum[2].x /= t;
        frustum[2].y /= t;
        frustum[2].z /= t;
        frustum[2].w /= t;

        if (frustum[3] == null) frustum[3] = new Vector4f();
        /* Extract the TOP plane */
        frustum[3].x = viewProjection.m03 - viewProjection.m01;
        frustum[3].y = viewProjection.m13 - viewProjection.m11;
        frustum[3].z = viewProjection.m23 - viewProjection.m21;
        frustum[3].w = viewProjection.m33 - viewProjection.m31;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[3].x * frustum[3].x + frustum[3].y * frustum[3].y + frustum[3].z * frustum[3].z);
        frustum[3].x /= t;
        frustum[3].y /= t;
        frustum[3].z /= t;
        frustum[3].w /= t;

        if (frustum[4] == null) frustum[4] = new Vector4f();
        /* Extract the FAR plane */
        frustum[4].x = viewProjection.m03 - viewProjection.m02;
        frustum[4].y = viewProjection.m13 - viewProjection.m12;
        frustum[4].z = viewProjection.m23 - viewProjection.m22;
        frustum[4].w = viewProjection.m33 - viewProjection.m32;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[4].x * frustum[4].x + frustum[4].y * frustum[4].y + frustum[4].z * frustum[4].z);
        frustum[4].x /= t;
        frustum[4].y /= t;
        frustum[4].z /= t;
        frustum[4].w /= t;

        if (frustum[5] == null) frustum[5] = new Vector4f();
        /* Extract the NEAR plane */
        frustum[5].x = viewProjection.m03 + viewProjection.m02;
        frustum[5].y = viewProjection.m13 + viewProjection.m12;
        frustum[5].z = viewProjection.m23 + viewProjection.m22;
        frustum[5].w = viewProjection.m33 + viewProjection.m32;

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[5].x * frustum[5].x + frustum[5].y * frustum[5].y + frustum[5].z * frustum[5].z);
        frustum[5].x /= t;
        frustum[5].y /= t;
        frustum[5].z /= t;
        frustum[5].w /= t;
    }

    public boolean pointInFrustum(Vector3f point)
    {
        if (frustum != null)
        {
            for (int i = 0; i < 6; i++)
            {
                if (frustum[i] != null)
                {
                    if (MathUtils.getSignedPlaneDistance(point, frustum[i]) < 0)
                    {
                        return false;
                    }
                }
            }
        }

        return true; // Return true if the point is inside the frustum, or if the frustum is null. No frustum means we just updateUnloadedChunks everything.
    }

    public IntersectionType aabbInFrustum(AxisAlignedBB aabb)
    {
        boolean flag = true;

        if (frustum != null)
        {
            for (int i = 0; i < 6; i++)
            {
                Vector4f plane = this.frustum[i];
                if (plane != null)
                {
                    float furthestNormalPos = MathUtils.getSignedPlaneDistance(AxisAlignedBB.getFurthestPoint(aabb, new Vector3f(+plane.x, +plane.y, +plane.z)), plane);
                    float furthestNormalNeg = MathUtils.getSignedPlaneDistance(AxisAlignedBB.getFurthestPoint(aabb, new Vector3f(-plane.x, -plane.y, -plane.z)), plane);

                    // If the furthest point in the direction of the planes normal is below the plane, there is no way we can be intersecting any of the other planes.
                    if (furthestNormalPos < 0.0F)
                        return IntersectionType.OUTSIDE;

                    // If the furthest point in the positive normal direction and the furthest point in the negative direction are on different sides to the plane, then
                    // we are not fully outside or inside this plane, we are intersecting it, but we may still be fully outside one or more of the other planes.
                    if (furthestNormalPos >= 0.0F && furthestNormalNeg <= 0.0F)
                        flag = false;
                }
            }
        }

        return flag ? IntersectionType.INSIDE : IntersectionType.INTERSECT;
    }

    public Transformation getTransformationOffset()
    {
        return transformationOffset;
    }

    public Vector3f getPositionWithOffset()
    {
        return Vector3f.add(this.getPosition(), this.getPositionOffset(), null);
    }

    public Vector3f getPositionOffset()
    {
        return this.getTransformationOffset().getTranslation();
    }

    public Vector3f getPosition()
    {
        return this.getParent().getTransformation().getTranslation();
    }

    public Quaternion getRotationWithOffset()
    {
        return Quaternion.mul(this.getRotation(), this.getRotationOffset(), null);
    }

    public Quaternion getRotationOffset()
    {
        return this.getTransformationOffset().getRotation();
    }

    public Quaternion getRotation()
    {
        return this.getParent().getTransformation().getRotation();
    }

    public Vector3f getLookVector()
    {
        return new Vector3f(this.viewMatrix.m20, this.viewMatrix.m21, this.viewMatrix.m22);
    }

    public Vector3f getPickingVector(Vector2f position)
    {
        if (position != null && this.projectionMatrix != null && this.viewMatrix != null)
        {
            Matrix4f invProjectionMatrix = Matrix4f.invert(this.projectionMatrix, null);
            Matrix4f invViewMatrix = Matrix4f.invert(this.viewMatrix, null);
            Vector4f clipCoords, eyeCoords, worldCoords;

            clipCoords = new Vector4f(position.x, -position.y, -1.0F, 1.0F);
            eyeCoords = Matrix4f.transform(invProjectionMatrix, clipCoords, null);
            eyeCoords = new Vector4f(eyeCoords.x, eyeCoords.y, -1.0F, 0.0F);
            worldCoords = Matrix4f.transform(invViewMatrix, eyeCoords, null);

            return new Vector3f(worldCoords).normalise(null);
        }

        return null;
    }

    public float getNear()
    {
        return near;
    }

    public float getFar()
    {
        return far;
    }

    public float getFov()
    {
        return fov;
    }

    public Matrix4f getProjectionMatrix()
    {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }

    public void setNear(float near)
    {
        this.near = near;
    }

    public void setFar(float far)
    {
        this.far = far;
    }

    public void setFov(float fov)
    {
        this.fov = fov;
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix)
    {
        this.projectionMatrix = projectionMatrix;
    }

    public void setViewMatrix(Matrix4f viewMatrix)
    {
        this.viewMatrix = viewMatrix;
    }
}
