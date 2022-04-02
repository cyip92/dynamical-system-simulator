/**
 * This class calculates positions of the system's objects
 */
package ChaosDistribution;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.stream.*;
import javax.swing.*;
import javax.swing.border.Border;
public class Position
{
	// Stuff used for displaying
	static double x1, y1, x2, y2;
	static double T1, V1, T2, V2, E;
	static double bound;
	static int step;

	// Animation variables
	static JComponent source;
	static int ms = 1;
	static int start = 0;
	static int interval = 2;
	static int delay = 1;

	// Simulation parameters
	static final double dt = .0005;
	static final int numSteps = 1000000;
	static boolean display = true;
	static boolean showHeader = true;
	static boolean showInit = true;
	static boolean hideSystem = false;
	static boolean hideCoM = true;
	static boolean tracePath = true;
	static boolean PlotEnergySpace = true;
	static boolean makeAnimation = false;
	static boolean showOnlyFinal = false;

	// Physical parameters
	static double g = 9.81;
	static double k = 1200;
	static double l1 = .2;
	static double l2 = .15;
	static double m1 = 1;
	static double m2 = 3;

	static double pi = Math.PI;
	static double dt2 = dt * dt;
	/* Kid on a swing:
	 * g = 9.81, l1 = .3, l2 = .1, m1 = 10, m2 = 2
	 * initT1 = initT2 = 1.57, initT1Dot = 1.2, initT2Dot = 0
	 */

	public static void main(String[] args)
	{
		run(3);
	}

	public static void run(int method)
	{
		// Initial conditions
		double initX = 0, initR = 0, initT1 = 0, initT2 = 0;
		double initXDot = 0, initRDot = 0, initT1Dot = 0, initT2Dot = 0;
		Displayer.setZeros();
		switch (method)
		{
		case 0: // Spring Pendulum
			initR  = l1;
			initT1 = 2.781;
			initRDot  = 1.4;
			initT1Dot = .7;
			Displayer.initCond = "r(0) = " + Displayer.round(initR)
				+ "     a(0) = " + Displayer.round(initT1)
				+ "     r'(0) = " + Displayer.round(initRDot)
				+ "     a'(0) = " + Displayer.round(initT1Dot);
			PlotEnergySpace = false;
			break;
		case 1: // Double Pendulum
			initT1 = pi;
			initT2 = pi;
			initT1Dot = 0;
			initT2Dot = -0.001;
			Displayer.initCond = "a1(0) = " + Displayer.round(initT1)
				+ "     a2(0) = " + Displayer.round(initT2)
				+ "     a1'(0) = " + Displayer.round(initT1Dot)
				+ "     a2'(0) = " + Displayer.round(initT2Dot);
			break;
		case 2: // Mass-spring Pendulum
			initX = l1;
			initT1 = 1;
			initXDot = 3;
			initT1Dot = 0;
			Displayer.initCond = "x(0) = " + Displayer.round(initX)
				+ "     a(0) = " + Displayer.round(initT1)
				+ "     x'(0) = " + Displayer.round(initXDot)
				+ "     a'(0) = " + Displayer.round(initT1Dot);
			break;
		case 3: // Swinging Atwood
			initR = .25;
			initT1 = -pi/2;
			initRDot = 0;
			initT1Dot = 0;
			Displayer.initCond = "r(0) = " + Displayer.round(initR)
				+ "     a(0) = " + Displayer.round(initT1)
				+ "     r'(0) = " + Displayer.round(initRDot)
				+ "     a'(0) = " + Displayer.round(initT1Dot);
			break;
		case 10: // Swinging Child
			g = 9.81;
			l1 = .3;
			l2 = .1;
			m1 = 10;
			m2 = 2;
			initT1 = initT2 = 1.57;
			initT1Dot = 1.2;
			initT2Dot = 0;
			method = 1;
			Displayer.initCond = "a1(0) = " + Displayer.round(initT1)
			+ "     a2(0) = " + Displayer.round(initT2)
			+ "     a1'(0) = " + Displayer.round(initT1Dot)
			+ "     a2'(0) = " + Displayer.round(initT2Dot);
			hideCoM = true;
			tracePath = false;
			break;
		case 11: // Spring Pendulum with critical energy
			initR  = l1;
			initT1 = 3.1;
			initRDot  = 0;
			initT1Dot = 0;
			Displayer.initCond = "r(0) = " + Displayer.round(initR)
				+ "     a(0) = " + Displayer.round(initT1)
				+ "     r'(0) = " + Displayer.round(initRDot)
				+ "     a'(0) = " + Displayer.round(initT1Dot);
			k = 600;
			method = 0;
			hideCoM = true;
			tracePath = false;
			PlotEnergySpace = false;
			break;
		case 12: // Spring Pendulum with no gravity and closed orbit
			initR  = l1;
			initT1 = 0;
			initRDot  = 1.5;
			initT1Dot = 5.9;
			Displayer.initCond = "r(0) = " + Displayer.round(initR)
				+ "     a(0) = " + Displayer.round(initT1)
				+ "     r'(0) = " + Displayer.round(initRDot)
				+ "     a'(0) = " + Displayer.round(initT1Dot);
			k = 600;
			g = 0;
			method = 0;
			hideCoM = true;
			tracePath = true;
			PlotEnergySpace = false;
			break;
		}
		step = 0;
		int dispInt = numSteps / 20;

		// Translation to array and rectangular
		switch (method)
		{
		case 0: // Spring Pendulum
			x1 =  initR * sin(initT1);
			y1 = -initR * cos(initT1);
			break;
		case 1: // Double Pendulum
			x1 =      l1 * sin(initT1);
			y1 =    - l1 * cos(initT1);
			x2 = x1 + l2 * sin(initT2);
			y2 = y1 - l2 * cos(initT2);
			break;
		case 2: // Mass-spring Pendulum
			x1 = initX;
			y1 = 0;
			x2 = x1 + l2 * sin(initT1);
			y2 =    - l2 * cos(initT1);
			break;
		case 3: // Swinging Atwood
			x1 =  initR * sin(initT1);
			y1 = -initR * cos(initT1);
			x2 =  l1;
			y2 = -l2;
			break;
		}

		// Calculate initial energy and bound
		double minV = 0;
		switch (method)
		{
		case 0: // Spring Pendulum
			E = m1/2*(sq(initRDot) + sq(initR * initT1Dot)) + m1*g*y1 + k/2*sq(initR - l1);
			minV = -m1*g*l1;
			break;
		case 1: // Double Pendulum
			E = m1/2*sq(l1 * initT1Dot) + m2/2*(sq(l1*cos(initT1)*initT1Dot
					+ l2*cos(initT2)*initT2Dot) + sq(l1*sin(initT1)*initT1Dot
							+ l2*sin(initT2)*initT2Dot)) + m1*g*y1 + m2*g*y2;
			minV = -m1*g*l1 - m2*g*(l1 + l2);
			break;
		case 2: // Mass-spring Pendulum
			E = m1/2*sq(initXDot) + m2/2*(sq(initXDot + l2*cos(initT1)*initT1Dot)
					+ sq(l2*sin(initT1)*initT1Dot)) + m2*g*y2;
			minV = -m2*g*l2;
			break;
		case 3: // Swinging Atwood
			E = m1/2*(sq(initRDot) + sq(initR * initT1Dot)) + m2/2*sq(initRDot);
			minV = -m2*g*initR;
			break;
		}
		bound = E - minV;

		// Put together header information in a file
		String header = "", sig = "";
		switch (method)
		{
		case 0: // Spring Pendulum
			sig = "g l1 k m1";
			break;
		case 1: // Double Pendulum
			sig = "g l1 l2 m1 m2";
			break;
		case 2: // Mass-spring Pendulum
			sig = "g k l1 l2 m1 m2";
			break;
		case 3: // Swinging Atwood
			sig = "g m1 m2";
			break;
		}
		header = generateHeader(header, sig);

		// Pass information to the displayers
		Displayer.setZeros();
		if (showHeader)
			Displayer.header = header.replace("; ", "          ");
		if (PlotEnergySpace)
			EnergySpace.setBounds(Math.abs(minV));


		// Initialize window
		JFrame frame = null, frame2 = null;
		if (display)
		{
			Displayer.system = method;
			switch (method)
			{
			case 0: // Spring Pendulum
				frame = makeFrame("Spring Pendulum", 0);
				break;
			case 1:
				frame = makeFrame("Double Pendulum", 0);
				break;
			case 2:
				frame = makeFrame("Mass-Spring Pendulum", 0);
				break;
			case 3:
				frame = makeFrame("Swinging Atwood's Machine", 0);
				break;
			}
		}
		if (PlotEnergySpace)
			frame2 = makeFrame("Energy Space", 1);
		if (showOnlyFinal)
		{
			frame.setVisible(false);
			if (frame2 != null)
				frame2.setVisible(false);
		}

		// Distributions
		int[][] dist1 = new int[500][500];
		int[][] dist2 = new int[500][500];
		int[] ADist = new int[1000];
		int[] BDist = new int[1000];

		// Translate initial conditions
		double APrev = 0, ACurr = 0;
		double BPrev = 0, BCurr = 0;
		switch (method)
		{
		case 0: // Spring Pendulum
			APrev = initR;
			ACurr = initR + initRDot * dt;
			BPrev = initT1;
			BCurr = initT1 + initT1Dot * dt;
			break;
		case 1: // Double Pendulum
			APrev = initT1;
			ACurr = initT1 + initT1Dot * dt;
			BPrev = initT2;
			BCurr = initT2 + initT2Dot * dt;
			break;
		case 2: // Mass-spring Pendulum
			APrev = initX;
			ACurr = initX + initXDot * dt;
			BPrev = initT1;
			BCurr = initT1 + initT1Dot * dt;
			break;
		case 3: // Swinging Atwood
			APrev = initR;
			ACurr = initR + initRDot * dt;
			BPrev = initT1;
			BCurr = initT1 + initT1Dot * dt;
			break;
		}

		// Image maker parameters
		int num_images = (numSteps - start) / interval;
		JFrame fr = new JFrame("Creating image...");
		Container content = fr.getContentPane();
		Border border = BorderFactory.createTitledBorder("Be patient, this might be slow...");
		JProgressBar bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setBorder(border);
		bar.setMaximum(num_images);
		content.add(bar, BorderLayout.NORTH);
		fr.setSize(520, 100);
		fr.setLocation(0, 540);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (makeAnimation)
			fr.setVisible(true);
		int images_written = 0;
		GifSequenceWriter writer = null;
		try
		{
			ImageOutputStream output = new FileImageOutputStream(new File("Animation.gif"));
			writer = new GifSequenceWriter(output, 1, ms, true);
		}
		catch (IOException e)
		{
			System.err.println("IOException for GifSequenceWriter");
		}

		// Simulate
		double ANext, BNext;
		while (step <= numSteps)
		{
			// Iterate
			ANext = ACurr;
			BNext = BCurr;
			switch (method)
			{
			case 0: // Spring Pendulum
				for (int i = 0; i < 10; i++)
				{
					ANext = rNextSP(APrev, ACurr, BPrev, BCurr, BNext);
					BNext = thetaNextSP(APrev, ACurr, ANext, BPrev, BCurr);
				}
				break;
			case 1: // Double Pendulum
				for (int i = 0; i < 500; i++)
				{
					ANext = T1NextDP(APrev, ACurr, BPrev, BCurr, BNext);
					BNext = T2NextDP(APrev, ACurr, ANext, BPrev, BCurr);
				}
				break;
			case 2: // Mass-spring Pendulum
				for (int i = 0; i < 80; i++)
				{
					ANext = XNextMSP(BPrev, BCurr, BNext, APrev, ACurr);
					BNext = TNextMSP(BPrev, BCurr, APrev, ACurr, ANext);
				}
				break;
			case 3: // Swinging Atwood
				for (int i = 0; i < 1500; i++)
				{
					ANext = RNextAtwood(APrev, ACurr, BPrev, BCurr, BNext);
					BNext = TNextAtwood(APrev, ACurr, ANext, BPrev, BCurr);
				}
				break;
			}

			// Translate to rectangular
			switch (method)
			{
			case 0: // Spring Pendulum
				x1 =  ANext * sin(BNext);
				y1 = -ANext * cos(BNext);
				break;
			case 1: // Double Pendulum
				x1 =      l1 * sin(ANext);
				y1 =    - l1 * cos(ANext);
				x2 = x1 + l2 * sin(BNext);
				y2 = y1 - l2 * cos(BNext);
				break;
			case 2: // Mass-spring Pendulum
				x1 = ANext;
				x2 = x1 + l2 * sin(BNext);
				y2 =    - l2 * cos(BNext);
				break;
			case 3: // Swinging Atwood
				x1 =  ANext * sin(BNext);
				y1 = -ANext * cos(BNext);
				y2 = -l2 + (ANext - initR);
				break;
			}

			// Calculate energies
			if (display || step % dispInt == 0)
			{
				double dx1, dy1, dx2, dy2;
				switch (method)
				{
				case 0: // Spring Pendulum
					T1 = .5 * m1 * (sq(ANext - APrev) / (4*dt2)
							+ sq(ACurr) * sq(BNext - BPrev) / (4*dt2));
					V1 = - m1 * g * (ACurr * cos(BCurr)) + .5 * k
					* sq(l1 - Math.sqrt(sq(ACurr * sin(BCurr)) + sq(ACurr * cos(BCurr))));
					break;
				case 1: // Double Pendulum
					dx1 = x1 - l1 * sin(APrev);
					dy1 = y1 + l1 * cos(APrev);
					dx2 = x2 - (l1 * sin(APrev) + l2 * sin(BPrev));
					dy2 = y2 + (l1 * cos(APrev) + l2 * cos(BPrev));
					T1 = .5 * m1 * (sq(dx1) + sq(dy1)) / (4 * dt2);
					T2 = .5 * m2 * (sq(dx2) + sq(dy2)) / (4 * dt2);
					V1 = - m1 * g * l1 * cos(ACurr);
					V2 = - m2 * g * (l1 * cos(ACurr) + l2 * cos(BCurr));
					break;
				case 2: // Mass-spring Pendulum
					dx1 = (ANext - APrev) / (2*dt);
					dx2 = x2 - (APrev + l2 * sin(BPrev));
					dy2 = y2 + (l2 * cos(BPrev));
					T1 = .5 * m1 * sq(dx1);
					T2 = .5 * m2 * (sq(dx2) + sq(dy2)) / (4*dt2);
					V1 = .5 * k * sq(ACurr - l1);
					V2 = - m2 * g * l2 * cos(BCurr);
					break;
				case 3: // Swinging Atwood
					dx1 = (ANext * sin(BNext)) - (APrev * sin(BPrev));
					dy1 = (ANext * cos(BNext)) - (APrev * cos(BPrev));
					T1 = .5 * m1 * (sq(dx1) + sq(dy1)) / (4 * dt2);
					T2 = .5 * m2 * sq((ANext - APrev) / (2*dt));
					V1 = - m1 * g * ACurr * cos(BCurr);
					V2 = m2 * g * ACurr;
					break;
				}
				if (!display)
					System.out.println(step + ": " + (T1+V1+T2+V2));
			}

			// Increment distributions
			if (!display)
			{
				dist1[Displayer.pixY1()][Displayer.pixX1()]++;
				switch (method)
				{
				case 0: // Spring Pendulum
				case 3: // Swinging Atwood
					ADist[clamp(0, (int) (500 * ANext / l1), 1000)]++;
					BDist[(int) (1000 * ((((.5 * BNext / pi) % 1) + 1.5) % 1))]++;
					break;
				case 1: // Double Pendulum
					dist2[Displayer.pixY2()][Displayer.pixX2()]++;
					ADist[(int) (1000 * ((((.5 * ANext / pi) % 1) + 1.5) % 1))]++;
					BDist[(int) (1000 * ((((.5 * BNext / pi) % 1) + 1.5) % 1))]++;
					break;
				case 2: // Mass-spring Pendulum
					dist2[Displayer.pixY2()][Displayer.clamp(0,
							(int) (500 * (Position.x2 - Displayer.minX)
									/ (Displayer.maxX - Displayer.minX)) - 125, 500)]++;
					ADist[Displayer.pixX1()]++;
					BDist[(int) (1000 * ((((.5 * BNext / pi) % 1) + 1.5) % 1))]++;
					break;
				}
			}

			// Repaint and wait
			if (display)
			{
				frame.repaint();
				if (PlotEnergySpace)
					frame2.repaint();
				if (makeAnimation && step > start && step % interval == 0)
				{
					BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
					source.paint(image.getGraphics());
					try
					{
						writer.writeToSequence(image);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					bar.setValue(++images_written);
					bar.setString((10000 * images_written / num_images) / 100.0 + "%");
					fr.setTitle("Frame " + images_written + " / " + num_images);
				}
				try
				{
					if (!showOnlyFinal)
						Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{}
			}
			step++;

			// Shift indices
			APrev = ACurr;
			ACurr = ANext;
			BPrev = BCurr;
			BCurr = BNext;
		}
		
		// Write all the data
		Toolkit.getDefaultToolkit().beep();
		if (!display)
		{
			System.out.println("Writing data...");
			switch (method)
			{
			case 0: // Spring Pendulum
			case 3: // Swinging Atwood
				writeArray(dist1, "RawData.txt", header);
				writeArray(ADist, "rData.txt", header);
				writeArray(BDist, "tData.txt", header);
				break;
			case 1: // Double Pendulum
				writeArray(dist1, "T1PosData.txt", header);
				writeArray(dist2, "T2PosData.txt", header);
				writeArray(ADist, "T1AngData.txt", header);
				writeArray(BDist, "T2AngData.txt", header);
				break;
			case 2: // Mass-spring Pendulum
				writeArray(dist1, "XPosData.txt", header);
				writeArray(dist2, "TPosData.txt", header);
				writeArray(ADist, "XDistData.txt", header);
				writeArray(BDist, "TDistData.txt", header);
				break;
			}
		}
		
		// Finish everything
		System.out.println("Done!  (" + numSteps + " points simulated)");
		if (display || showOnlyFinal) // If displaying, pause a minute before exiting
		{
			if (showOnlyFinal)
			{
				frame.setVisible(true);
				if (frame2 != null)
					frame2.setVisible(true);
			}
			try
			{
				Thread.sleep(60000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	public static double rNextSP(double r_iM1, double r_i, double T_iM1, double T_i, double T_iP1)
	{
		double term1 = r_i * Math.pow(T_iP1 - T_iM1, 2) / 4;
		double term2 = 2*r_i - r_iM1;
		double term3 = Math.pow(dt, 2) * (g * cos(T_i) + k / m1 * (l1 - r_i));
		return term1 + term2 + term3;
	}

	public static double thetaNextSP(double r_iM1, double r_i, double r_iP1, double T_iM1, double T_i)
	{
		double div = r_i + (r_iP1 - r_iM1) / 2;
		double term1 = T_iM1 * (r_iP1 - r_iM1) / 2;
		double term2 = -r_i * (T_iM1 - 2*T_i);
		double term3 = -g * Math.pow(dt, 2) * sin(T_i);
		return (term1 + term2 + term3) / div;
	}

	public static double T1NextDP(double T1_iM1, double T1_i, double T2_iM1, double T2_i, double T2_iP1)
	{
		double div = -m2*l1*l2*sin(T1_i - T2_i)*T2_iM1 - m2*l1*l2*sin(T1_i - T2_i)*T2_iP1 - 4*m1*l1*l1 - 4*m2*l1*l1 + m2*l1*l2*sin(T1_i - T2_i)*T2_iM1 + m2*l1*l2*sin(T1_i - T2_i)*T2_iP1;
		double term1 = 4*dt2*m1*g*l1*sin(T1_i);
		double term2 = m2*l1*l2*sin(T1_i - T2_i)*(T1_iM1*T2_iM1 - T1_iM1*T2_iP1);
		double term3 = 4*dt2*m2*g*l1*sin(T1_i);
		double term4 = 4*m1*l1*l1*(T1_iM1 - 2*T1_i);
		double term5 = 4*m2*l1*l1*(T1_iM1 - 2*T1_i);
		double term6 = 4*m2*l1*l2*cos(T1_i - T2_i)*(T2_iP1 + T2_iM1 - 2*T2_i);
		double term7 = -1*m2*l1*l2*sin(T1_i - T2_i)*(T1_iM1*T2_iM1 - T1_iM1*T2_iP1);
		double term8 = m2*l1*l2*sin(T1_i - T2_i)*(T2_iM1*T2_iM1 - T2_iM1*T2_iP1 - T2_iP1*T2_iM1 + T2_iP1*T2_iP1);
		return (term1 + term2 + term3 + term4 + term5 + term6 + term7 + term8) / div;
	}

	public static double T2NextDP(double T1_iM1, double T1_i, double T1_iP1, double T2_iM1, double T2_i)
	{
		double div = l1*l2*sin(T1_i - T2_i)*T1_iM1 - l1*l2*sin(T1_i - T2_i)*T1_iP1 + 4*l2*l2 - 1*l1*l2*sin(T1_i - T2_i)*T1_iM1 + l1*l2*sin(T1_i - T2_i)*T1_iP1;
		double term1 = l1*l2*sin(T1_i - T2_i)*(T1_iM1*T2_iM1 - T1_iP1*T2_iM1);
		double term2 = -4*dt2*g*l2*sin(T2_i);
		double term3 = -4*l2*l2*(T2_iM1 - 2*T2_i);
		double term4 = -4*l1*l2*cos(T1_i - T2_i)*(T1_iP1 + T1_iM1 - 2*T1_i);
		double term5 = l1*l2*sin(T1_i - T2_i)*(T1_iM1*T1_iM1 - T1_iM1*T1_iP1 - T1_iP1*T1_iM1 + T1_iP1*T1_iP1);
		double term6 = -1*l1*l2*sin(T1_i - T2_i)*(T1_iM1*T2_iM1 - T1_iP1*T2_iM1);
		return (term1 + term2 + term3 + term4 + term5 + term6) / div;
	}

	public static double XNextMSP(double T_iM1, double T_i, double T_iP1, double X_iM1, double X_i)
	{
		double div = 4*m1 + 4*m2;
		double term1 = 4*dt2*k*l1;
		double term2 = -4*dt2*k*X_i;
		double term3 = -4*m1*(X_iM1 - 2*X_i);
		double term4 = -4*m2*(X_iM1 - 2*X_i);
		double term5 = -4*m2*l2*cos(T_i)*(T_iP1 + T_iM1 - 2*T_i);
		double term6 = m2*l2*sin(T_i)*(T_iM1*T_iM1 - T_iM1*T_iP1 - T_iP1*T_iM1 + T_iP1*T_iP1);
		return (term1 + term2 + term3 + term4 + term5 + term6) / div;
	}

	public static double TNextMSP(double T_iM1, double T_i, double X_iM1, double X_i, double X_iP1)
	{
		double div = -l2;
		double term1 = dt2*g*sin(T_i);
		double term2 = cos(T_i)*(X_iP1 + X_iM1 - 2*X_i);
		double term3 = l2*(T_iM1 - 2*T_i);
		return (term1 + term2 + term3) / div;
	}

	public static double RNextAtwood(double r_iM1, double r_i, double t_iM1, double t_i, double t_iP1)
	{
		double div = 4*m1 + 4*m2;
		double term1 = m1*r_i*(t_iM1*t_iM1 - t_iM1*t_iP1 - t_iP1*t_iM1 + t_iP1*t_iP1);
		double term2 = 4*dt2*m1*g*cos(t_i);
		double term3 = -4*dt2*m2*g;
		double term4 = -4*m1*(r_iM1 - 2*r_i);
		double term5 = -4*m2*(r_iM1 - 2*r_i);
		return (term1 + term2 + term3 + term4 + term5) / div;
	}

	public static double TNextAtwood(double r_iM1, double r_i, double r_iP1, double t_iM1, double t_i)
	{
		double div = 2*r_iM1 - 2*r_iP1 - 4*r_i;
		double term1 = 4*dt2*g*sin(t_i);
		double term2 = 2*(r_iM1*t_iM1 - r_iP1*t_iM1);
		double term3 = 4*r_i*(t_iM1 - 2*t_i);
		return (term1 + term2 + term3) / div;
	}

	// Given a string indicating used variables, generate the header
	public static String generateHeader(String header, String sig)
	{
		if (sig.contains("g"))
			header = header + "g = " + g + "; ";
		if (sig.contains("k"))
			header = header + "k = " + k + "; ";
		if (sig.contains("l1"))
			header = header + "l1 = " + l1 + "; ";
		if (sig.contains("l2"))
			header = header + "l2 = " + l2 + "; ";
		if (sig.contains("m1"))
			header = header + "m1 = " + m1 + "; ";
		if (sig.contains("m2"))
			header = header + "m2 = " + m2 + "; ";
		return header;
	}

	// Write a 1D array to a file
	public static void writeArray(int[] ar, String name, String header)
	{
		FileWriter f = null;
		try
		{
			f = new FileWriter(name);
			f.write(header);
			for (int i = 0; i < ar.length-1; i++)
				f.write(ar[i] + ",");
			f.write(ar[ar.length-1] + "\r\n");
			f.flush();
			f.close();
		}
		catch (IOException e)
		{}
	}

	// Write a 500x500 array to a file
	public static void writeArray(int[][] ar, String name, String header)
	{
		FileWriter f = null;
		try
		{
			f = new FileWriter(name);
			f.write(header);
			for (int a = 0; a < 500; a++)
			{
				for (int b = 0; b < 500; b++)
					f.write(ar[a][b] + ",");
				f.write("\r\n");
			}
			f.flush();
			f.close();
		}
		catch (IOException e)
		{}
	}

	public static JFrame makeFrame(String str, int type)
	{
		JFrame frame = new JFrame();
		switch (type)
		{
		case 0:
			Displayer d = new Displayer();
			source = d;
			frame.add(d);
			break;
		case 1:
			EnergySpace e = new EnergySpace();
			EnergySpace.setBounds(bound);
			frame.add(e);
			frame.setLocation(527, 0);
			break;
		}
		frame.setSize(517, 537);
		frame.setVisible(true);
		frame.setTitle(str);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
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

	public static double sin(double x)
	{
		return Math.sin(x);
	}

	public static double cos(double x)
	{
		return Math.cos(x);
	}

	public static double sq(double x)
	{
		return x * x;
	}
}