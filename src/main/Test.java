package main;

import org.lwjgl.util.vector.Vector2f;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Needed to visualize the distribution of SSAO sample points.
 *
 * @author Kelan
 */
public class Test extends JPanel
{
    private long lastChange;
    private Random random = new Random();
    private Vector2f[] samples = new Vector2f[64];

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setContentPane(new Test());
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (System.nanoTime() - lastChange > 1000000000)
        {
            lastChange = System.nanoTime();

            for (int i = 0; i < samples.length; i++)
            {
                float x = random.nextFloat() * 2.0F - 1.0F;
                float y = random.nextFloat() * 2.0F - 1.0F;
                float scale = (float) i / samples.length;

                samples[i] = (Vector2f) new Vector2f(x, y).normalise().scale(random.nextFloat() * scale);
            }
        }

        int r = (int) (Math.min(getWidth(), getHeight()) * 0.375F);
        int cx = getWidth() / 2;
        int cy = getWidth() / 2;

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(1.0F));

        for (int i = 0; i < samples.length; i++)
        {
            int x = (int) (cx + samples[i].x * r);
            int y = (int) (cy + samples[i].y * r);
            g2.drawLine(cx, cy, x, y);
            g2.fillOval(x - 3, y - 3, 6, 6);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(5.0F));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        repaint();
    }
}
