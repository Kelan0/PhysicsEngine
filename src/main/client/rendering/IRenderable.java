package main.client.rendering;

/**
 * @author Kelan
 */
public interface IRenderable
{
    void init();

    void render(double delta);

    void dispose();

    void applyUniforms(ShaderProgram shaderProgram);

    default boolean doRender()
    {
        return true;
    }
}
