/**
 * This class displays stuff
 */
package ChaosDistribution;
import java.awt.*;
import javax.swing.JComponent;
@SuppressWarnings("serial")
public class EnergySpace extends JComponent
{
	static double minX, maxX, minY, maxY;
	static String scale;

	static Path path1 = new Path();
	static Path path2 = new Path();

	static int system;

	public void paintComponent(Graphics g)
	{
		// Axes
		g.drawLine(0, 0, 0, 500);
		g.drawLine(0, 250, 500, 250);
		g.drawString("Potential", 3, 13);
		g.drawString("Kinetic", 462, 262);
		//g.drawString("Scale: " + scale, 2, 498);

		// Draw path 1
		g.setColor(Color.red);
		path1.add(pixXUnclamped(Position.T1), pixYUnclamped(Position.V1));
		for (int i = 0; i < Path.length - 1; i++)
			g.drawLine(path1.x(i), path1.y(i), path1.x(i+1), path1.y(i+1));
		g.drawOval(path1.x(Path.length-1) - 5, path1.y(Path.length-1) - 5, 10, 10);
		g.drawString("1", path1.x(Path.length-1) + 8, path1.y(Path.length-1) + 13);
		g.fillRect(path1.avgXPix() - 2, path1.avgYPix() - 2, 4, 4);

		// Draw path 2
		if (Displayer.system != 0)
		{
			g.setColor(Color.blue);
			path2.add(pixXUnclamped(Position.T2), pixYUnclamped(Position.V2));
			for (int i = 0; i < Path.length - 1; i++)
				g.drawLine(path2.x(i), path2.y(i), path2.x(i+1), path2.y(i+1));
			g.drawOval(path2.x(Path.length-1) - 5, path2.y(Path.length-1) - 5, 10, 10);
			g.drawString("2", path2.x(Path.length-1) + 8, path2.y(Path.length-1) + 13);
			g.fillRect(path2.avgXPix() - 2, path2.avgYPix() - 2, 4, 4);
			
		}
		
		// Draw average indication above paths
		g.setColor(Color.black);
		g.drawString("1 (avg.)", path1.avgXPix() + 5, path1.avgYPix() + 10);
		if (Displayer.system != 0)
			g.drawString("2 (avg.)", path2.avgXPix() + 5, path2.avgYPix() + 10);

		// Virial Theorem
		/*g.setColor(Color.gray);
		double p1X = path1.avgX(), p1Y = path1.avgY(), p2X = path2.avgX(), p2Y = path2.avgY();
		g.drawString("2<T> = " + round("" + (2.0 * (p1X + p2X) / (p1Y + p2Y)), 4) + "<V>", 2, 483);*/
	}

	public static void setBounds(double E)
	{
		E = Math.max(E, Position.E);
		E = Math.max(maxY, E);
		minX = 0;
		maxX = 2*E;
		minY = -E;
		maxY = E;
		scale = round("" + E, 5);
	}

	public static int pixX(double x)
	{
		return clamp(0, (int) (500 * (x - minX) / (maxX - minX)), 500);
	}

	public static int pixY(double y)
	{
		return clamp(0, (int) (500 * (1 - (y - minY) / (maxY - minY))), 500);
	}
	
	public static int pixXUnclamped(double x)
	{
		return (int) (500 * (x - minX) / (maxX - minX));
	}

	public static int pixYUnclamped(double y)
	{
		return (int) (500 * (1 - (y - minY) / (maxY - minY)));
	}

	public static String round(String str, int n)
	{
		if (str.length() < n+2)
			return str;
		else
			return str.substring(0, n+2);
	}

	public static int clamp(int a, int x, int b)
	{
		if (x < a)
			return a;
		else if (x > b)
			return b;
		else
			return x;
	}
}

class Path
{
	int[] x, y;
	int totalX, totalY;
	int index, points;
	int customPathLength;
	static int length = 1000;

	public Path(int x0, int y0, int len)
	{
		x = new int[len];
		y = new int[len];
		for (int i = 0; i < len; i++)
		{
			x[i] = x0;
			y[i] = y0;
		}
		index = 0;
		customPathLength = len;
	}
	
	public Path()
	{
		x = new int[length];
		y = new int[length];
		for (int i = 0; i < length; i++)
			y[i] = 250;
		totalX = 0;
		totalY = 0;
		index = 0;
		points = 1;
	}

	public void add(int x0, int y0)
	{
		x[index] = x0;
		y[index] = y0;
		totalX += x0;
		totalY += y0;
		index = (index + 1) % length;
		points++;
	}

	public int x(int i)
	{
		return x[(index + i) % length];
	}

	public int y(int i)
	{
		return y[(index + i) % length];
	}
	
	public void addC(int x0, int y0)
	{
		x[index] = x0;
		y[index] = y0;
		index = (index + 1) % customPathLength;
	}
	
	public int xC(int i)
	{
		return x[(index + i) % customPathLength];
	}

	public int yC(int i)
	{
		return y[(index + i) % customPathLength];
	}

	public int avgXPix()
	{
		return totalX / points;
	}

	public int avgYPix()
	{
		return totalY / points;
	}

	public double avgX()
	{
		return 1.0 * totalX / points;
	}

	public double avgY()
	{
		return 250.0 - totalY / points;
	}
}