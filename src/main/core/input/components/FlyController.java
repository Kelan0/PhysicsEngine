package main.core.input.components;

import main.client.rendering.ShaderProgram;
import main.core.Engine;
import main.core.input.InputContext;
import main.core.input.InputHandler;
import main.core.scene.Component;
import main.core.scene.Transformation;
import main.core.util.MathUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author Kelan
 */
public class FlyController extends Component
{
    private float moveSpeed;
    private float mouseSpeed;
    private float accelerationSpeed = 5.0F;
    private InputContext inputContext = new InputContext(3);

    private Vector3f motion = new Vector3f();

    private float motionTimer = 0.0F;

    public FlyController(float moveSpeed, float mouseSpeed)
    {
        this.moveSpeed = moveSpeed;
        this.mouseSpeed = mouseSpeed;

        this.inputContext.registerState("forward", () -> {
            motion.z--;
            return true;
        });
        this.inputContext.registerState("backward", () -> {
            motion.z++;
            return true;
        });
        this.inputContext.registerState("left", () -> {
            motion.x--;
            return true;
        });
        this.inputContext.registerState("right", () -> {
            motion.x++;
            return true;
        });
        this.inputContext.registerState("up", () -> {
            motion.y++;
            return true;
        });
        this.inputContext.registerState("down", () -> {
            motion.y--;
            return true;
        });
    }

    public FlyController()
    {
        this(20.0F, 0.1F);
    }

    @Override
    public void render(double delta)
    {
        this.moveSpeed = 1.0F;
        Engine.getInputHandler().addContext(this.inputContext);
        Matrix3f orientation = this.getOrientation(true);

        if (motion.lengthSquared() > 0.0)
        {
            float acceleration = (float) (Math.min(motionTimer, 1.0F) * delta);

            Matrix3f.transform(orientation, motion, motion);
            motion.normalise().scale(this.moveSpeed * acceleration);

            this.getParent().getTransformation().translate(motion);

            motionTimer += delta * accelerationSpeed;
        } else
        {
            motionTimer = 0.0F;
        }

        this.motion = new Vector3f();
    }

    @Override
    public void render(double delta, ShaderProgram shaderProgram)
    {

    }

    @Override
    public void dispose()
    {

    }

    @Override
    public void applyUniforms(ShaderProgram shaderProgram)
    {

    }

    @Override
    public void init()
    {

    }

    @Override
    public void update(double delta)
    {

    }

    private Matrix3f getOrientation(boolean yAxisLock)
    {
        Transformation transformation = this.getParent().getTransformation();
        Quaternion orientation = transformation.getRotation();

        InputHandler inputHandler = Engine.getInputHandler();
        Vector2f mouseVelocity = new Vector2f();

        if (inputHandler.isMouseGrabbed())
            mouseVelocity = inputHandler.getMouseVelocity();

        float pitch = (float) Math.toRadians(mouseVelocity.y * this.mouseSpeed);
        float yaw = (float) Math.toRadians(mouseVelocity.x * this.mouseSpeed);

        Vector3f xAxis = MathUtils.rotateVector3f(orientation.negate(null), new Vector3f(1.0F, 0.0F, 0.0F), null);
        Vector3f zAxis = MathUtils.rotateVector3f(orientation.negate(null), new Vector3f(0.0F, 0.0F, 1.0F), null);
        Vector3f yAxis;

        if (yAxisLock)
            yAxis = new Vector3f(0.0F, 1.0F, 0.0F);
        else
            yAxis = MathUtils.rotateVector3f(orientation.negate(null), new Vector3f(0.0F, 1.0F, 0.0F), null);

        Quaternion.mul(MathUtils.axisAngleToQuaternion(xAxis.negate(null), pitch, null), orientation, orientation).normalise();
        Quaternion.mul(MathUtils.axisAngleToQuaternion(yAxis.negate(null), yaw, null), orientation, orientation).normalise();

        Matrix3f matrix = new Matrix3f();
        matrix.m00 = xAxis.x;
        matrix.m01 = xAxis.y;
        matrix.m02 = xAxis.z;
        matrix.m10 = yAxis.x;
        matrix.m11 = yAxis.y;
        matrix.m12 = yAxis.z;
        matrix.m20 = zAxis.x;
        matrix.m21 = zAxis.y;
        matrix.m22 = zAxis.z;
        return matrix;
    }

    public float getMoveSpeed()
    {
        return moveSpeed;
    }

    public float getMouseSpeed()
    {
        return mouseSpeed;
    }

    public float getAccelerationSpeed()
    {
        return accelerationSpeed;
    }

    public void setMoveSpeed(float moveSpeed)
    {
        this.moveSpeed = moveSpeed;
    }

    public void setMouseSpeed(float mouseSpeed)
    {
        this.mouseSpeed = mouseSpeed;
    }

    public void setAccelerationSpeed(float accelerationSpeed)
    {
        this.accelerationSpeed = accelerationSpeed;
    }
}
