/**
 * This class displays stuff
 */
package ChaosDistribution;
import java.awt.*;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;

import javax.swing.JComponent;
@SuppressWarnings("serial")
public class Displayer extends JComponent
{
	static double minX = -.5;
	static double maxX = .5;
	static double minY = -.5;
	static double maxY = .5;
	static int roundPlaces = 3;

	static String zeros;
	static String header;
	static String initCond;
	static int system;

	static Path path1, path2;
	static int pathLength = 2000;

	public void paintComponent(Graphics g)
	{
		// White background and header
		g.setColor(Color.white);
		g.fillRect(0, 0, 500, 500);
		g.setColor(Color.black);
		if (Position.showHeader)
			g.drawString(header, 2, 12);
		if (Position.showInit)
			g.drawString(initCond, 2, 27);

		// System objects
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0, xMid = 0, yMid = 0, equil = 0;
		double m1 = 0, m2 = 0, sum = 0, dx2 = 0, dy2 = 0;
		Graphics2D g2;
		if (!Position.hideSystem)
		{
			switch (system)
			{
			case 0: // Spring Pendulum
				x1 = pixX1();
				y1 = pixY1();

				// Equilibrium length
				g.setColor(Color.red);
				equil = (int) (500 * (Position.l1 / (maxX - minX)));
				g.drawOval(250 - equil, 250 - equil, 2*equil, 2*equil);
				g.drawString("Equilibrium", 253 + equil, 250);
				g.drawString("Length", 255 + equil, 265);
				g.setColor(Color.black);

				// Drawing
				g.fillOval(x1 - 10, y1 - 10, 20, 20);
				dx2 = Position.x1 * Position.x1;
				dy2 = Position.y1 * Position.y1;
				double length = Math.sqrt(dx2 + dy2);
				g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(clamp(0, (int) (3 * (1 - (length - Position.l1) / Position.l1)),
						6), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.draw(new Line2D.Float(x1, y1, 250, 250));
				break;
			case 1: // Double Pendulum
				x1 = pixX1();
				y1 = pixY1();
				x2 = pixX2();
				y2 = pixY2();

				g.setColor(Color.black);
				g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

				// Rods
				g2.draw(new Line2D.Float(250, 250, x1, y1));
				g2.draw(new Line2D.Float(x1, y1, x2, y2));

				// Bobs
				g.fillOval(x1 - 10, y1 - 10, 20, 20);
				g.fillOval(x2 - 10, y2 - 10, 20, 20);
				break;
			case 2: // Mass-Spring Pendulum
				x1 = clamp(0, (int) (500 * (Position.x1 - minX) / (maxX - minX)) - 125, 500);
				y1 = pixY1();
				x2 = clamp(0, (int) (500 * (Position.x2 - minX) / (maxX - minX)) - 125, 500);
				y2 = pixY2();

				// Support and equilibrium
				g.setColor(Color.red);
				g.drawLine(0, 250, 500, 250);
				equil = (int) (125 + 500 * (Position.l1 / (maxX - minX)));
				g.drawLine(equil, y1 - 15, equil, y1 + 15);
				g.drawString("Equilibrium", equil - 4, y1 - 18);

				g.setColor(Color.black);
				g2 = (Graphics2D) g;

				// Rods
				g2.setStroke(new BasicStroke(clamp(2, (int) (3 * (1 - (Position.x1 - Position.l1)
						/ Position.l1)), 6), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.draw(new Line2D.Float(x1, 250, 125, 250));
				g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.draw(new Line2D.Float(x1, 250, x2, y2));

				// Objects
				g.fillRect(x1 - 10, y1 - 10, 20, 20);
				g.fillOval(x2 - 10, y2 - 10, 20, 20);
				break;
			case 3: // Swinging Atwood
				x1 = pixX1();
				y1 = pixY1();
				x2 = pixX2();
				y2 = pixY2();
				
				g.setColor(Color.black);
				g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
				// Rods
				g2.draw(new Line2D.Float(250, 250, x1, y1));
				g2.draw(new Line2D.Float(250, 250, x2, 250));
				g2.draw(new Line2D.Float(x2, 250, x2, y2));
				
				// Bobs
				g.fillOval(x1 - 10, y1 - 10, 20, 20);
				g.fillOval(x2 - 15, y2 - 15, 30, 30);
				break;
			default:
				System.err.println("Unrecognized system id!");
				break;
			}
		}

		// Center of Mass
		if (!Position.hideCoM)
		{
			switch (system)
			{
			case 0: // Spring Pendulum
				break;
			case 1: // Double Pendulum
			case 2: // Mass-Spring Pendulum
			case 3: // Swinging Atwood
				g.setColor(Color.red);
				m1 = Position.m1;
				m2 = Position.m2;
				sum = m1 + m2;
				xMid = (int) ((m1*x1 + m2*x2) / sum);
				yMid = (int) ((m1*y1 + m2*y2) / sum);
				g.drawLine(xMid - 10, yMid - 10, xMid + 10, yMid + 10);
				g.drawLine(xMid - 10, yMid + 10, xMid + 10, yMid - 10);
				g.drawString("CoM", xMid + 15, yMid + 20);
				g.setColor(Color.black);
				break;
			default:
				System.err.println("Unrecognized system id!");
				break;
			}
		}
		
		// Paths
		if (Position.tracePath)
		{
			g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke());
			switch (system)
			{
			case 0: // Spring Pendulum
			case 3: // Swinging Atwood
				g.setColor(Color.gray);
				if (path1 == null)
					path1 = new Path(x1, y1, pathLength);
				path1.addC(x1, y1);
				for (int i = 0; i < pathLength - 1; i++)
					g.drawLine(path1.xC(i), path1.yC(i), path1.xC(i+1), path1.yC(i+1));
				break;
			case 1: // Double Pendulum
			case 2: // Mass-Spring Pendulum
				g.setColor(Color.gray);
				if (path2 == null)
					path2 = new Path(x2, y2, pathLength);
				path2.addC(x2, y2);
				for (int i = 0; i < pathLength - 1; i++)
					g.drawLine(path2.xC(i), path2.yC(i), path2.xC(i+1), path2.yC(i+1));
				break;
			default:
				System.err.println("Unrecognized system id!");
				break;
			}
			g.setColor(Color.black);
		}

		// Energies
		if (!Position.PlotEnergySpace)
		{
			double T = (Position.T1 + Position.T2);
			double V = (Position.V1 + Position.V2);
			double E = T + V;
			g.drawString("Step " + Position.step, 2, 453);
			g.drawString("T = " + round(T) + " J", 2, 468);
			g.drawString("V = " + round(V) + " J", 2, 483);
			g.drawString("E = " + round(E) + " J", 2, 498);
		}
		else
			g.drawString("i = " + Position.step, 2, 498);
	}

	public static int pixX1()
	{
		return clamp(0, (int) (500 * (Position.x1 - minX) / (maxX - minX)), 500);
	}

	public static int pixY1()
	{
		return clamp(0, (int) (500 * (1 - (Position.y1 - minY) / (maxY - minY))), 500);
	}

	public static int pixX2()
	{
		return clamp(0, (int) (500 * (Position.x2 - minX) / (maxX - minX)), 500);
	}

	public static int pixY2()
	{
		return clamp(0, (int) (500 * (1 - (Position.y2 - minY) / (maxY - minY))), 500);
	}

	public static void setZeros()
	{
		zeros = "";
		for (int i = 0; i < roundPlaces; i++)
			zeros += "0";
	}

	public static String round(double x)
	{
		DecimalFormat df = new DecimalFormat("0." + zeros);
		return df.format(x);
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