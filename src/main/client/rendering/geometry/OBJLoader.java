package main.client.rendering.geometry;

import main.core.util.FileUtils;
import main.core.util.StringUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kelan
 */
public class OBJLoader
{
    public static final String COMMENT_LINE = "# ";
    public static final String OBJ_VERTEX_GEOMETRIC = "v ";
    public static final String OBJ_VERTEX_NORMAL = "vn ";
    public static final String OBJ_VERTEX_TEXTURE = "vt ";
    public static final String OBJ_FACE_INDEX = "f ";
    public static final String OBJ_GROUP = "g ";
    public static final String OBJ_MATERIAL_LIB = "mtllib ";
    public static final String OBJ_MATERIAL_USE = "usemtl ";
    public static final String MTL_DEFINITION = "newmtl ";
    public static final String MTL_AMBIENT_COLOUR = "Ka ";
    public static final String MTL_DEFUSE_OLOUR = "Kd ";
    public static final String MTL_SPECULAR_COLOUR = "Ks ";
    public static final String MTL_SPECULAR_COEFFICIENT = "Ns ";
    public static final String MTL_AMBIENT_TEXTURE = "map_Ka ";
    public static final String MTL_DEFUSE_TEXTURE = "map_Kd ";
    public static final String MTL_SPECULAR_TEXTURE = "map_Ks ";
    public static final String MTL_TEXTURE_BLEND_U = "-blendu ";
    public static final String MTL_TEXTURE_BLEND_V = "-blendv ";
    public static final String MTL_TEXTURE_COLOUR_CORRECTION = "-cc ";
    public static final String MTL_TEXTURE_CLAMP_UV = "-clamp ";
    public static final String MTL_TEXTURE_CHANNEL = "-imfchan ";
    public static final String MTL_TEXTURE_BRIGHTNESS_CONTRAST = "-mm ";
    public static final String MTL_TEXTURE_OFFSET = "-o ";
    public static final String MTL_TEXTURE_SCALE = "-s ";
    public static final String MTL_TEXTURE_TURBULENCE = "-getHead ";
    public static final String MTL_TEXTURE_RESOLUTION = "-texres ";

    public static MeshData loadModel(String file) throws IOException
    {
        System.out.println("Loading OBJ file " + file);
        StringBuilder sb = new StringBuilder();

        if (FileUtils.readFile(file, sb) && sb.length() > 0)
        {
            String[] fileSource = sb.toString().split("\n");

            List<Vector3f> geometrics = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
            List<Vector2f> textures = new ArrayList<>();

            for (String line : fileSource)
            {
                if (line == null || line.isEmpty() || line.startsWith(COMMENT_LINE))
                {
                    continue;
                } else if (line.startsWith(OBJ_VERTEX_GEOMETRIC))
                {
                    geometrics.add(readVector3f(line.substring(OBJ_VERTEX_GEOMETRIC.length()).trim(), new Vector3f()));
                } else if (line.startsWith(OBJ_VERTEX_NORMAL))
                {
                    normals.add(readVector3f(line.substring(OBJ_VERTEX_NORMAL.length()).trim(), new Vector3f()));
                } else if (line.startsWith(OBJ_VERTEX_TEXTURE))
                {
                    textures.add(readVector2f(line.substring(OBJ_VERTEX_TEXTURE.length()).trim(), new Vector2f()));
                } else if (line.startsWith(OBJ_FACE_INDEX))
                {

                }
            }
        }

        return null;
    }

    private static float readFloat(String line, float defaultValue)
    {
        String[] data = line.split(" ");

        return StringUtils.isFloat(data[0]) ? Float.parseFloat(data[0]) : defaultValue;
    }

    private static Vector2f readVector2f(String line, Vector2f defaultValue)
    {
        String[] data = line.split(" ");
        float x = StringUtils.isFloat(data[0]) ? Float.parseFloat(data[0]) : defaultValue.x;
        float y = StringUtils.isFloat(data[1]) ? Float.parseFloat(data[1]) : defaultValue.y;

        return new Vector2f(x, y);
    }

    private static Vector3f readVector3f(String line, Vector3f defaultValue)
    {
        String[] data = line.split(" ");
        float x = StringUtils.isFloat(data[0]) ? Float.parseFloat(data[0]) : defaultValue.x;
        float y = StringUtils.isFloat(data[1]) ? Float.parseFloat(data[1]) : defaultValue.y;
        float z = StringUtils.isFloat(data[2]) ? Float.parseFloat(data[2]) : defaultValue.z;

        return new Vector3f(x, y, z);
    }
}
