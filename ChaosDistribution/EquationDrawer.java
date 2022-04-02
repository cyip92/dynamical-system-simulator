package ChaosDistribution;
import java.awt.*;
import javax.swing.*;
@SuppressWarnings("serial")
public class EquationDrawer extends JComponent
{
	static double min = -60;
	static double max = 60;
	
	public void paintComponent(Graphics g)
	{
		int length = Equation.values.length;
		for (int i = 0 ; i < length; i++)
			g.drawLine(500 * i / length, pix(Equation.values[i]),
					500 * i / length, pix(Equation.values[i]));
		
		g.setColor(Color.red);
		g.drawLine(0, 250, 500, 250);
	}
	
	public static int pix(double val)
	{
		return clamp(0, (int) (500 * (1 - (val - min) / (max - min))), 520);
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