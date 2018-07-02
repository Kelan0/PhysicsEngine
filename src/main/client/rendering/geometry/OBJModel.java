package main.client.rendering.geometry;

import main.client.rendering.TextureLoader;
import main.client.rendering.geometry.PolygonTriangulator.*;
import main.core.util.FileUtils;
import main.core.util.MathUtils;
import main.core.util.StringUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * @author Kelan
 */
public class OBJModel
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

    private List<OBJFace> faces;

    private OBJModel(List<OBJFace> faces)
    {
        this.faces = faces;
    }

    public static OBJModel parseObj(String file) throws IOException
    {
        System.out.println("Loading OBJ filePath " + file);
        StringBuilder sb = new StringBuilder();

        if (FileUtils.readFile(file, sb) && sb.length() > 0)
        {
//            MeshBuilder meshBuilder = new MeshBuilder();

            File f = new File(file);
            String[] fileSource = sb.toString().split("\n");

            List<Vector3f> geometrics = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
            List<Vector2f> textures = new ArrayList<>();

            List<OBJFace> faces = new ArrayList<>();
            List<Material> materials = new ArrayList<>();

            Material currentMaterial = null;

            String INVALID_VALUE = "\0INVALID_LINE\0";

            int counter = 0;
            for (String line : fileSource)
            {
                counter++;
                if (line == null || line.isEmpty() || line.startsWith(COMMENT_LINE))
                {
                    if (line == null || line.isEmpty())
                    {
                        continue;
//                        System.err.println("Invalid line in OBJ file, skipping");
                    } else if (line.startsWith(COMMENT_LINE))
                    {
                        System.out.println(line.substring(COMMENT_LINE.length())); // Print comments for debugging.
                    }
                } else if (line.startsWith(OBJ_VERTEX_GEOMETRIC))
                {
                    line = line.substring(OBJ_VERTEX_GEOMETRIC.length());
                    geometrics.add(readVector3f(line, new Vector3f(0.0F, 0.0F, 0.0F)));
                } else if (line.startsWith(OBJ_VERTEX_NORMAL))
                {
                    line = line.substring(OBJ_VERTEX_NORMAL.length());
                    normals.add(readVector3f(line, new Vector3f(0.0F, 0.0F, 0.0F)));
                } else if (line.startsWith(OBJ_VERTEX_TEXTURE))
                {
                    line = line.substring(OBJ_VERTEX_TEXTURE.length());
                    textures.add(readVector2f(line, new Vector2f(0.0F, 0.0F)));
                } else if (line.startsWith(OBJ_FACE_INDEX))
                {
                    line = line.substring(OBJ_FACE_INDEX.length()).trim();

                    String[] data = line.split(" ");

                    int[] indicesArr = new int[data.length * 3];
                    int i = readIndices(line, indicesArr);
                    int pointer = 0;

                    if (i == 0)
                    {
                        System.err.println("Failed to load face indices \"" + line + "\" from OBJ. This may cause errors.");
                        continue;
                    }

                    if (i >= 1)
                    {
                        Vertex[] vertices = new Vertex[data.length];

                        for (int j = 0; j < data.length; j++)
                        {
                            vertices[j] = new Vertex(geometrics.get(indicesArr[pointer++]));
                        }

                        if (i >= 2)
                        {
                            if (i >= 3)
                            {
                                for (int j = 0; j < data.length; j++)
                                {
                                    vertices[j].texture = textures.get(indicesArr[pointer++]);
                                }

                                for (int j = 0; j < data.length; j++)
                                {
                                    vertices[j].normal = normals.get(indicesArr[pointer++]);
                                }
                            } else
                            {
                                for (int j = 0; j < data.length; j++)
                                {
                                    vertices[j].normal = normals.get(indicesArr[pointer++]);
                                }
                            }
                        }

                        faces.add(new OBJFace(vertices, currentMaterial));
                    }
                } else if (line.startsWith(OBJ_MATERIAL_LIB))
                {
                    line = line.substring(OBJ_MATERIAL_LIB.length());
                    String mtlStr = readString(line, INVALID_VALUE); // default value padded with null characters incase someone decides to name an MTL filePath "INVALID_MTL" for some reason.

                    if (!mtlStr.endsWith(INVALID_VALUE) && mtlStr.endsWith(".mtl"))
                    {
//                        materials.addAll(parseMtl(f.getParentFile() + File.separator + mtlStr));
                    } else
                    {
                        System.err.println("Line \"" + line + "\" specifying an MTL filePath was not valid");
                    }
                } else if (line.startsWith(OBJ_MATERIAL_USE))
                {
                    line = line.substring(OBJ_MATERIAL_USE.length());
                    String name = readString(line, INVALID_VALUE);
                    for (Material material : materials)
                    {
                        if (material == null)
                        {
                            materials.remove(null);
                        } else if (material.name.equals(name))
                        {
                            currentMaterial = material;
                            break;
                        }
                    }
                }
            }

            System.out.println("Successfully loaded and compiled OBJ file");

            return new OBJModel(faces);
        } else
        {
            System.err.println("Failed to load OBJ filePath " + file);
//        throw new UnexpectedException("An unexpected error occurred while loading the OBJ filePath " + filePath);

            return null;
        }
    }

    public static List<Material> parseMtl(String file) throws IOException
    {
        System.out.println("Loading MTL filePath " + file);
        List<Material> materials = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        if (FileUtils.readFile(file, sb) && sb.length() > 0)
        {
            File f = new File(file);
            String[] fileSource = sb.toString().split("\n");
            Material currentMaterial = null;

            for (String line : fileSource)
            {
                // TODO: filter comment lines.
                if (line.startsWith(MTL_DEFINITION))
                {
                    line = line.substring(MTL_DEFINITION.length());
                    if (currentMaterial != null)
                    {
                        materials.add(currentMaterial);
                    }

                    currentMaterial = new Material(line.split(" ")[0]);
                }

                if (currentMaterial != null)
                {
                    if (line.startsWith(MTL_AMBIENT_COLOUR))
                    {
                        line = line.substring(MTL_AMBIENT_COLOUR.length());
                        currentMaterial.ambientColour = readVector3f(line, currentMaterial.ambientColour);
                    } else if (line.startsWith(MTL_DEFUSE_OLOUR))
                    {
                        line = line.substring(MTL_DEFUSE_OLOUR.length());
                        currentMaterial.diffuseColour = readVector3f(line, currentMaterial.diffuseColour);
                    } else if (line.startsWith(MTL_SPECULAR_COLOUR))
                    {
                        line = line.substring(MTL_SPECULAR_COLOUR.length());
                        Vector3f specularColour = readVector3f(line, new Vector3f(currentMaterial.specularColour));

                        currentMaterial.specularColour = new Vector4f(specularColour.x, specularColour.y, specularColour.z, 1.0F);
                    } else if (line.startsWith(MTL_SPECULAR_COEFFICIENT))
                    {
                        line = line.substring(MTL_SPECULAR_COEFFICIENT.length());
                        currentMaterial.specularColour.w = readFloat(line, currentMaterial.specularColour.w);
                    } else if (line.startsWith(MTL_AMBIENT_TEXTURE))
                    {
                        line = line.substring(MTL_AMBIENT_TEXTURE.length());
                        TextureLoader texture = readTextureMap(line);
                        texture.setFilePath(f.getParentFile() + File.separator + texture.filePath);
//                        currentMaterial.ambientTexture = texture.loadTexture(); // TODO
                    } else if (line.startsWith(MTL_DEFUSE_TEXTURE))
                    {
                        TextureLoader texture = readTextureMap(line);
                        texture.setFilePath(f.getParentFile() + File.separator + texture.filePath);
//                        currentMaterial.diffuseTexture = texture.loadTexture(); // TODO
                    } else if (line.startsWith(MTL_SPECULAR_TEXTURE))
                    {
                        TextureLoader texture = readTextureMap(line);
                        texture.setFilePath(f.getParentFile() + File.separator + texture.filePath);
//                        currentMaterial.specularTexture = texture.loadTexture(); // TODO
                    }
                }
            }

            if (currentMaterial != null)
            {
                materials.add(currentMaterial);
            }
        } else
        {
            System.err.println("Failed to load MTL filePath " + file);
        }

        return materials;
    }

    private static TextureLoader readTextureMap(String line)
    {
        TextureLoader loader = new TextureLoader();

        /*
        Sources that this code is based on. Most of this is translated from C++ code.

        https://github.com/syoyo/tinyobjloader/blob/master/tiny_obj_loader.h
        http://paulbourke.net/dataformats/mtl/
        https://github.com/jaredloomis/YFNH-LWJGL/blob/master/3DYFNH/src/net/future/model/OBJLoader.java
         */

        String[] data = line.split("(?=-)");
        int i = 0;
        while (i < data.length)
        {
            String temp = data[i++];
            if (temp.startsWith(MTL_TEXTURE_BLEND_U))
            {
                loader.blendu = readBoolean(temp.substring(MTL_TEXTURE_BLEND_U.length()), loader.blendu);
            } else if (temp.startsWith(MTL_TEXTURE_BLEND_V))
            {
                loader.blendv = readBoolean(temp.substring(MTL_TEXTURE_BLEND_V.length()), loader.blendv);
            } else if (temp.startsWith(MTL_TEXTURE_COLOUR_CORRECTION))
            {
                loader.colourCorrection = readBoolean(temp.substring(MTL_TEXTURE_COLOUR_CORRECTION.length()), loader.colourCorrection);
            } else if (temp.startsWith(MTL_TEXTURE_CLAMP_UV))
            {
                loader.clampUV = readBoolean(temp.substring(MTL_TEXTURE_CLAMP_UV.length()), loader.clampUV);
            } else if (temp.startsWith(MTL_TEXTURE_CHANNEL))
            {
                loader.channel = temp.substring(MTL_TEXTURE_CHANNEL.length());
            } else if (temp.startsWith(MTL_TEXTURE_BRIGHTNESS_CONTRAST))
            {
                Vector2f mm = readVector2f(temp.substring(MTL_TEXTURE_BRIGHTNESS_CONTRAST.length()), new Vector2f(loader.brightness, loader.contrast));
                loader.brightness = mm.x;
                loader.contrast = mm.y;
            } else if (temp.startsWith(MTL_TEXTURE_OFFSET))
            {
                loader.offset = readVector3f(temp.substring(MTL_TEXTURE_OFFSET.length()), new Vector3f(0.0F, 0.0F, 0.0F));
            } else if (temp.startsWith(MTL_TEXTURE_SCALE))
            {
                loader.scale = readVector3f(temp.substring(MTL_TEXTURE_SCALE.length()), new Vector3f(1.0F, 1.0F, 1.0F));
            } else if (temp.startsWith(MTL_TEXTURE_TURBULENCE))
            {
                loader.turbulence = readVector3f(temp.substring(MTL_TEXTURE_TURBULENCE.length()), new Vector3f(0.0F, 0.0F, 0.0F));
            } else if (temp.startsWith(MTL_TEXTURE_RESOLUTION))
            {
                loader.resolution = readFloat(line.substring(MTL_TEXTURE_RESOLUTION.length()), loader.resolution);
            }
        }

        String[] values = line.split(" ");
        loader.filePath = values[values.length - 1]; //Assuming filePath is always at the end of the line, and it contains no spaces.

        return loader;
    }

    private static int readIndices(String line, int[] indices)
    {
        if (line == null || line.isEmpty() || indices == null || indices.length < 3)
        {
            return 0;
        }

        int maxIndex; //1 guarantees a vertex translation. 2 guarantees a translation and a normal. 3 guarantees a translation, texture and normal
        if (line.contains("/")) //This vertex has normals and/or textures
        {
            if (line.contains("//")) //This vertex does not have textures
            {
                maxIndex = 2;
                line = line.replace("//", "/");
            } else
            {
                maxIndex = 3;
            }
        } else
        {
            maxIndex = 1;
        }

        String[] data = line.split(" ");

        int numVertices = data.length;

        if (indices.length < maxIndex * numVertices)
        {
            return 0;
        }

        String[][] vertices = new String[data.length][];

        int lengthCheck = -1;

        for (int i = 0; i < data.length; i++)
        {
            vertices[i] = data[i].split("/");

            if (lengthCheck == -1)
            {
                lengthCheck = vertices[i].length;
            } else if (vertices[i].length != lengthCheck)
            {
                return 0;
            }
        }

        int pointer = 0;
        for (int i = 0; i < maxIndex; i++)
        {
            for (int j = 0; j < data.length; j++)
            {
                indices[pointer++] = Integer.valueOf(vertices[j][i]) - 1;
            }
        }

        return maxIndex;
    }

    private static boolean readBoolean(String str, boolean defaultValue)
    {
        if (str != null && str.length() > 0)
        {
            str = str.toLowerCase();

            if (StringUtils.stringCompare("true", str, 4) == 0)
                return true;

            if (StringUtils.stringCompare("yes", str, 3) == 0)
                return true;

            if (StringUtils.stringCompare("on", str, 2) == 0)
                return true;

            if (StringUtils.stringCompare("false", str, 5) == 0)
                return false;

            if (StringUtils.stringCompare("no", str, 2) == 0)
                return false;

            if (StringUtils.stringCompare("off", str, 3) == 0)
                return false;
        }

        return defaultValue;
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

    private static String readString(String line, String defaultValue)
    {
        String[] data = line.split(" ");

        return data[0] == null || data[0].isEmpty() ? defaultValue : data[0];
    }

    public void triangulate()
    {
        List<OBJFace> triangles = new ArrayList<>();

        for (OBJFace face : this.faces)
        {
            face.triangulate(triangles, false);
        }

        this.faces = triangles;
    }

    public MeshData compileMesh()
    {
        return this.compileMesh(ShaderDataLocations.getDefaultDataLocations());
    }

    public MeshData compileMesh(ShaderDataLocations attributes)
    {
        System.out.println("Compiling mesh\nTriangulating");
        this.triangulate();

        System.out.println("Compiling mesh");
        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (OBJFace face : this.faces)
        {
            for (int i = 0; i < 3; i++)
            {
                Vertex vertex = face.vertices[i];

                if (vertex != null)
                {
                    boolean flag = true;
//                    for (int j = 0; j < vertices.size(); ++j)
//                    {
//                        Vertex v = vertices.get(j);
//                        if (v == null)
//                        {
//                            vertices.remove(j);
//                        } else if (v.equals(vertex))
//                        {
//                            flag = false;
//                            indices.add(j);
//                        }
//                    }

                    if (flag)
                    {
                        indices.add(vertices.size());
                        vertices.add(vertex);
                    }
                }
            }
        }

        System.out.println("Done");

        return new MeshData(vertices, indices);
    }

    public static class OBJFace
    {
        public Vertex[] vertices;
        public Material material;

        public OBJFace(Vertex[] vertices, Material material)
        {
            this.vertices = vertices;
            this.material = material;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OBJFace objFace = (OBJFace) o;
            return Arrays.equals(vertices, objFace.vertices) && Objects.equals(material, objFace.material);
        }

        @Override
        public int hashCode()
        {
            int result = Objects.hash(material);
            result = 31 * result + Arrays.hashCode(vertices);
            return result;
        }

        @Override
        public String toString()
        {
            return "OBJFace{" + "vertices=" + Arrays.toString(vertices) + ", material=" + material + '}';
        }

        public void triangulate(List<OBJFace> faces, boolean convex)
        {
            if (this.vertices.length >= 3)
            {
                if (this.vertices.length == 3)
                {
                    faces.add(this);
                } else
                {
                    if (convex)
                    {
                        Queue<Vertex> queue = new LinkedList(Arrays.asList(this.vertices));

                        Vertex v0 = queue.poll();
                        Vertex v1 = queue.poll();

                        while (!queue.isEmpty())
                        {
                            Vertex v2 = queue.poll();

                            faces.add(new OBJFace(new Vertex[]{v0, v1, v2}, this.material));

                            v1 = v2;
                        }
                    } else
                    {
                        Vector3f[] points3d = new Vector3f[this.vertices.length];
                        Vertex2D[] points2d = new Vertex2D[this.vertices.length];

                        for (int i = 0; i < this.vertices.length; i++)
                        {
                            points3d[i] = this.vertices[i].position;
                        }

                        Matrix3f covarianceMatrix = (Matrix3f) MathUtils.computeCovarianceMatrix(points3d).invert();
                        Vector3f planeNormal = new Vector3f(1.0F, 1.0F, 1.0F);
                        Vector3f planePosition = MathUtils.average(points3d);

                        if (covarianceMatrix != null)
                        {
                            // calculate the largest eigenvector for the inverse of the covariance matrix. this is the smallest eigenvector of the un-inverted matrix.
                            // This vector is the direction of least variance, and so will be the normal of a plane that will cause the least distortion to the polygon,
                            // although, if a polygon overlaps itself, it may cause undefined behaviour.
                            for (int i = 0; i < 200; i++)
                            {
                                Matrix3f.transform(covarianceMatrix, planeNormal, planeNormal).normalise();
                            }
                        } else
                        {
                            // These vertices all lie on the same plane, and thus the inverse of the covariance matrix cannot be computed.
                            // Use the cross product between the first two edges to calculate the plane normal.
                            Vector3f e0 = Vector3f.sub(this.vertices[2].position, this.vertices[0].position, null);
                            Vector3f e1 = Vector3f.sub(this.vertices[1].position, this.vertices[0].position, null);

                            planeNormal = Vector3f.cross(e0, e1, null).normalise(null);
                        }

                        Vector3f u = (Vector3f) Vector3f.cross(planeNormal, new Vector3f(0.0F, 1.0F, 0.0F), null).normalise(); // The x-axis of the plane.
                        Vector3f v = (Vector3f) Vector3f.cross(planeNormal, u, null).normalise(); // The y-axis of the plane.

                        for (int i = 0; i < this.vertices.length; i++)
                        {
                            float distance = -Vector3f.dot(planeNormal, Vector3f.sub(points3d[i], planePosition, null)); // The distance to the plane, from point.

                            Vector3f dir = (Vector3f) new Vector3f(planeNormal).scale(distance); // The vector to add to point to place it on the plane.

                            Vector3f planePoint = Vector3f.add(points3d[i], dir, null); // The point is now on the surface of the plane, but is still in 3D world space.

                            // The dot product between the planes u/v axis and the point on the plane should give me a point in plane coordinates relative to the planes origin.
                            float x = Vector3f.dot(Vector3f.sub(planePoint, planePosition, null), u);
                            float y = Vector3f.dot(Vector3f.sub(planePoint, planePosition, null), v);

                            points2d[i] = new Vertex2D(i, distance, new Vector2f(x, y));
                        }

                        List<Triangle2D> triangles = new PolygonTriangulator(new ArrayList<>(Arrays.asList(points2d))).generateTriangles();

                        for (Triangle2D triangle : triangles)
                        {
                            Vertex v0 = new Vertex(vertices[triangle.v0.index]);
                            Vertex v1 = new Vertex(vertices[triangle.v1.index]);
                            Vertex v2 = new Vertex(vertices[triangle.v2.index]);

                            v0.setPosition(getUnprojectedWorldCoords(triangle.v0, u, v, planeNormal, planePosition));
                            v1.setPosition(getUnprojectedWorldCoords(triangle.v1, u, v, planeNormal, planePosition));
                            v2.setPosition(getUnprojectedWorldCoords(triangle.v2, u, v, planeNormal, planePosition));

                            faces.add(new OBJFace(new Vertex[]{v0, v1, v2}, this.material));
                        }
                    }
                }
            }
        }

        private Vector3f getUnprojectedWorldCoords(Vertex2D point, Vector3f u, Vector3f v, Vector3f planeNormal, Vector3f planePosition)
        {
            u = (Vector3f) u.normalise(null).scale(point.x);
            v = (Vector3f) v.normalise(null).scale(point.y);

            Vector3f pointOnPlane = Vector3f.add(planePosition, Vector3f.add(u, v, null), null);

            return Vector3f.add(pointOnPlane, (Vector3f) new Vector3f(planeNormal).scale(-point.distance), null);
        }
    }
}
