package main.client.rendering;

import main.client.rendering.geometry.ShaderDataLocations;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;

/**
 * @author Kelan
 */
public class ShaderProgram
{
    private final int programID;

    private ShaderDataLocations dataLocations;

    private List<Shader> shaders = new ArrayList<>();
    private HashMap<String, Integer> uniforms = new HashMap<>();

    private FloatBuffer buffer16 = BufferUtils.createFloatBuffer(16);
    private FloatBuffer buffer9 = BufferUtils.createFloatBuffer(9);
    private FloatBuffer buffer4 = BufferUtils.createFloatBuffer(4);

    public ShaderProgram(ShaderDataLocations dataLocations, Shader... shaders) throws UnexpectedException
    {
        if (dataLocations == null || dataLocations.inputs == null || dataLocations.inputs.length == 0)
            dataLocations = ShaderDataLocations.getDefaultDataLocations();

        if (shaders == null || shaders.length == 0)
            throw new IllegalStateException("Cannot create shader program, no shaders provided");

        this.dataLocations = dataLocations;

        for (Shader shader : shaders)
        {
            if (shader != null && this.shaders.stream().noneMatch(s -> s.file.equals(shader.file)))
                this.shaders.add(shader);
        }

        this.programID = glCreateProgram();

        if (this.dataLocations.inputs != null && this.dataLocations.inputs.length > 0)
        {
            for (ShaderDataLocations.InputLocation input : this.dataLocations.inputs)
                glBindAttribLocation(this.programID, input.getIndex(), input.getName());
        }

        if (this.dataLocations.outputs != null && this.dataLocations.outputs.length > 0)
        {
            for (ShaderDataLocations.OutputLocation output : this.dataLocations.outputs)
                glBindFragDataLocation(this.programID, output.getIndex(), output.getName());
        }

        for (Shader shader : this.shaders)
            shader.compile(this);

        glLinkProgram(this.programID);
        glValidateProgram(this.programID);

        if (!checkProgramStatus(this.programID, GL_LINK_STATUS))
            throw new UnexpectedException("Failed to link shader program.");

        if (!checkProgramStatus(this.programID, GL_VALIDATE_STATUS))
            throw new UnexpectedException("Failed to validate shader program.");
    }

    public ShaderDataLocations getDataLocations()
    {
        return dataLocations;
    }

    public int getUniformLocation(String uniform)
    {
        return this.uniforms.computeIfAbsent(uniform, x -> glGetUniformLocation(this.programID, uniform));
//        return glGetUniformLocation(program, uniform);
    }

    public void setUniformVector1f(String uniform, float f)
    {
        glUniform1f(this.getUniformLocation(uniform), f);
    }

    public void setUniformVector2f(String uniform, float f, float f1)
    {
        glUniform2f(this.getUniformLocation(uniform), f, f1);
    }

    public void setUniformVector2f(String uniform, Vector2f v)
    {
        setUniformVector2f(uniform, v.x, v.y);
    }

    public void setUniformMatrix2f(String uniform, Matrix2f m)
    {
        glUniformMatrix2fv(this.getUniformLocation(uniform), false, storeAndFlip(m, buffer4));
    }

    public void setUniformVector3f(String uniform, float f, float f1, float f2)
    {
        glUniform3f(this.getUniformLocation(uniform), f, f1, f2);
    }

    public void setUniformVector3f(String uniform, Vector3f v)
    {
        setUniformVector3f(uniform, v.x, v.y, v.z);
    }

    public void setUniformMatrix3f(String uniform, Matrix3f m)
    {
        glUniformMatrix3fv(this.getUniformLocation(uniform), false, storeAndFlip(m, buffer9));
    }

    public void setUniformVector4f(String uniform, float f, float f1, float f2, float f3)
    {
        glUniform4f(this.getUniformLocation(uniform), f, f1, f2, f3);
    }

    public void setUniformVector4f(String uniform, Vector4f v)
    {
        setUniformVector4f(uniform, v.x, v.y, v.z, v.w);
    }

    public void setUniformMatrix4f(String uniform, Matrix4f m)
    {
        glUniformMatrix4fv(this.getUniformLocation(uniform), false, storeAndFlip(m, buffer16));
    }

    public void setUniformVector1i(String uniform, int i)
    {
        glUniform1i(this.getUniformLocation(uniform), i);
    }

    public void setUniformVector2i(String uniform, int i, int i1)
    {
        glUniform2i(this.getUniformLocation(uniform), i, i1);
    }

    public void setUniformVector3i(String uniform, int i, int i1, int i2)
    {
        glUniform3i(this.getUniformLocation(uniform), i, i1, i2);
    }

    public void setUniformVector4i(String uniform, int i, int i1, int i2, int i3)
    {
        glUniform4i(this.getUniformLocation(uniform), i, i1, i2, i3);
    }

    public void setUniformBoolean(String uniform, boolean b)
    {
        glUniform1i(this.getUniformLocation(uniform), b ? 1 : 0);
    }

    private FloatBuffer storeAndFlip(Matrix matrix, FloatBuffer buf)
    {
        matrix.store(buf);
        buf.flip();
        return buf;
    }

    public void delete()
    {
        for (Shader shader : this.shaders)
            shader.delete(this);

        glDeleteProgram(this.programID);
    }

    public static void bind(ShaderProgram shaderProgram)
    {
        if (shaderProgram != null)
            glUseProgram(shaderProgram.programID);
        else
            ShaderProgram.unbind();
    }

    public static void unbind()
    {
        glUseProgram(0);
    }

    private static boolean checkProgramStatus(int program, int parameter)
    {
        if (glGetProgrami(program, parameter) == GL_FALSE)
        {
            System.err.println("Failed to create shader program " + program + ". This may cause visual errors\n" + getProgramLog(program));
            return false;
        }

        return true;
    }

    private static boolean checkShaderStatus(int shader, int parameter)
    {
        if (glGetShaderi(shader, parameter) == GL_FALSE)
        {
            System.err.println("Failed to create shader " + shader + "\n" + getShaderLog(shader));
            return false;
        }
        return true;
    }

    private static String getShaderLog(int shader)
    {
        return getShaderName(shader) + ": " + glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
    }

    private static String getProgramLog(int program)
    {
        return glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
    }

    private static String getShaderName(int shader)
    {
        String shaderTypeName;
        switch (shader)
        {
            case GL_VERTEX_SHADER:
                shaderTypeName = "vertex_shader";
                break;
            case GL_TESS_CONTROL_SHADER:
                shaderTypeName = "tessellation_control_shader";
                break;
            case GL_TESS_EVALUATION_SHADER:
                shaderTypeName = "tessellation_evaluation_shader";
                break;
            case GL_GEOMETRY_SHADER:
                shaderTypeName = "geometry_shader";
                break;
            case GL_FRAGMENT_SHADER:
                shaderTypeName = "fragment_shader";
                break;
            case GL_COMPUTE_SHADER:
                shaderTypeName = "compute_shader";
                break;
            default:
                shaderTypeName = "unknown_shader";
                break;
        }

        return shaderTypeName;
    }

    public static class Shader
    {
        private File file;
        private String source;
        private int type;
        protected int shaderID;

        public Shader(String filePath, int type) throws IOException
        {
            this.file = new File(filePath);
            this.source = "";
            this.type = type;

            if (this.file.exists() && !this.file.isDirectory())
            {
                Files.lines(this.file.toPath()).forEach(s -> {
                    this.source += s + "\n";
                });
            } else
            {
                throw new FileNotFoundException("GLSL source file \"" + filePath + "\" could not be found.");
            }
        }

        boolean compile(ShaderProgram shaderProgram)
        {
            System.out.println("Compiling " + ShaderProgram.getShaderName(this.type) + " from \"" + this.file.getAbsolutePath() + "\"");

            if (this.source.isEmpty())
            {
                System.err.println("No shader source. Cannot compile shader.");
                return false;
            }

            this.shaderID = glCreateShader(this.type);

            if (this.shaderID <= 0)
            {
                System.err.println("Failed to create shader");
                return false;
            }

            glShaderSource(this.shaderID, this.source);
            glCompileShader(this.shaderID);

            if (checkShaderStatus(this.shaderID, GL_COMPILE_STATUS))
            {
                glAttachShader(shaderProgram.programID, this.shaderID);
            } else
            {
                System.err.println("Failed to compile shader");
                this.delete(shaderProgram);
                return false;
            }

            return true;
        }

        void delete(ShaderProgram shaderProgram)
        {
            glDetachShader(shaderProgram.programID, this.shaderID);
            glDeleteShader(this.shaderID);
        }
    }
}
