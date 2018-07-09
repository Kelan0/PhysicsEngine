package main.client.rendering;

/**
 * @author Kelan
 */
public interface IRenderable
{
    void init();

    /**
     * Render the mesh. The mesh may use any custom shader programs defined
     * by components, children or itself
     * @param delta The update delta time.
     */
    void render(double delta);

    /**
     * Render the mesh to this specific shader program. The implementation of
     * this function may not comply with this though. This is used for things
     * like shadow mapping.
     * @param delta The update delta time.
     * @param shaderProgram The shader program to render to.
     */
    void render(double delta, ShaderProgram shaderProgram);

    /**
     * Dispose of this object, releasing any resources and memory it may have
     * been using, i.e. textures, vbos, fbos etc.
     */
    void dispose();

    /**
     * Apply any uniforms needed to render this object to the shader program
     * supplied.
     * @param shaderProgram The shader program to set the uniforms for.
     */
    void applyUniforms(ShaderProgram shaderProgram);

    default boolean doRender()
    {
        return true;
    }
}
