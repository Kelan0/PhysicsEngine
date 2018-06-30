package main.client.rendering.geometry;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author Kelan
 */
public class ShaderDataLocations
{
    public int stride;
    public InputLocation[] inputs;
    public OutputLocation[] outputs;

    public ShaderDataLocations(int stride, InputLocation[] inputs, OutputLocation[] outputs)
    {
        this.stride = stride;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public void enableVertexAttributes()
    {
        for (InputLocation inputLocation : inputs)
            glEnableVertexAttribArray(inputLocation.index);
    }

    public void disableVertexAttributes()
    {
        for (InputLocation inputLocation : inputs)
            glDisableVertexAttribArray(inputLocation.index);
    }

    public void bindVertexAttributes()
    {
        for (InputLocation inputLocation : inputs)
            glVertexAttribPointer(inputLocation.index, inputLocation.size, GL_FLOAT, false, this.stride, inputLocation.offset);
    }

    public static ShaderDataLocations getDefaultDataLocations()
    {
        return new ShaderDataLocations(48, new InputLocation[]{
                new InputLocation("vertexPosition", 0, 3, 0),
                new InputLocation("vertexNormal", 1, 3, 12),
                new InputLocation("vertexTexture", 2, 2, 24),
                new InputLocation("vertexColour", 3, 4, 32),
        }, new OutputLocation[]{
                new OutputLocation("outColour", 0),
        });
    }

    public static class InputLocation
    {
        private String name;
        private int index;
        private int size;
        private long offset;

        public InputLocation(String name, int index, int size, long offset)
        {
            this.name = name;
            this.index = index;
            this.size = size;
            this.offset = offset;
        }

        public String getName()
        {
            return name;
        }

        public int getIndex()
        {
            return index;
        }

        public int getSize()
        {
            return size;
        }

        public long getOffset()
        {
            return offset;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InputLocation inputLocation = (InputLocation) o;
            return index == inputLocation.index && size == inputLocation.size && offset == inputLocation.offset && Objects.equals(name, inputLocation.name);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, index, size, offset);
        }

        @Override
        public String toString()
        {
            return "InputLocation{" + "name='" + name + '\'' + ", index=" + index + ", size=" + size + ", getSideOffset=" + offset + '}';
        }
    }

    public static class OutputLocation
    {
        private String name;
        private int index;

        public OutputLocation(String name, int index)
        {
            this.name = name;
            this.index = index;
        }

        public String getName()
        {
            return name;
        }

        public int getIndex()
        {
            return index;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OutputLocation that = (OutputLocation) o;
            return index == that.index && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, index);
        }

        @Override
        public String toString()
        {
            return "OutputLocation{" + "name='" + name + '\'' + ", index=" + index + '}';
        }
    }
}
