package ChaosDistribution;
import javax.swing.*;

public class Equation
{
	public static double[] values;
	static double dt = .001;
	static double dt2 = dt * dt;
	static double b = -.5;
	static double w = 5;
	static double k = 3600;
	
	public static void main(String[] args)
	{
		final double x0 = 50;
		final double v0 = 0;
		
		values = new double[8000];
		values[0] = x0;
		values[1] = x0 + dt * v0;
		
		for (int i = 2; i < values.length; i++)
		{
			values[i] = iterate(values[i-2], values[i-1]);
		}
		
		makeFrame("");
	}
	
	public static double iterate(double R_iM1, double R_i)
	{
		double div = -R_i * R_i;
		double term1 = R_i * R_i * (R_iM1 - 2 * R_i);
		double term2 = dt2 * k;
		return (term1 + term2) / div;
	}
	
	public static JFrame makeFrame(String str)
	{
		JFrame frame = new JFrame();
		EquationDrawer d = new EquationDrawer();
		frame.add(d);
		frame.setSize(517, 537);
		frame.setVisible(true);
		frame.setTitle(str);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}