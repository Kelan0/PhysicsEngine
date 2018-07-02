package main.core.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Kelan
 */
public class FileUtils
{
    public static boolean readFile(String file, StringBuilder dest) throws IOException
    {
        return readFile(new File(file), dest);
    }

    public static boolean readFile(File file, StringBuilder dest) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            while (true)
            {
                String line = reader.readLine();

                if (line != null)
                {
                    dest.append(line).append("\n");
                } else
                {
                    break;
                }
            }
            return true;
        } catch (FileNotFoundException e)
        {
            System.err.println("File \"" + file + "\" not found.");
            e.printStackTrace();
            return false;
        }
    }

    public static BufferedImage loadImage(String file)
    {
        try
        {
            return ImageIO.read(new File(file));
        } catch (IOException e)
        {
            System.err.println("Failed to load image from file \"" + file + "\"");
        }

        return null;
    }

    /**
     * Creates the file directory if it does not exist.
     *
     * @param file The file path to create a directory for.
     * @return The file in the directory.
     */
    public static File checkOrCreate(File file)
    {
        if (file != null)
        {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists())
            {
                parentFile.mkdirs();
            }
        }

        return file;
    }

    public static boolean exists(String file)
    {
        if (file == null || file.isEmpty())
        {
            return false;
        }

        return new File(file).exists();
    }

    public static boolean isDirectory(String file)
    {
        if (file == null || file.isEmpty())
        {
            return false;
        }

        return new File(file).isDirectory();
    }
}
