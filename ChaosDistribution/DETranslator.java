package ChaosDistribution;
import java.util.*;
import java.util.regex.*;
public class DETranslator
{
	static boolean print = false;
	static String[] names = new String[26];

	public static void main(String[] args)
	{
		Scanner s = new Scanner(System.in);

		// Prompts
		System.out.print("Please enter a differential equation, using capital letters to"
				+ " represent variables:\n0 = ");
		String str = s.nextLine() + " ";
		System.out.println("Which variable would you like to solve for?");
		char c = s.nextLine().charAt(0);

		// Processing
		str = delimit(str);
		str = replaceVars(str);
		str = replaceDerivatives(str);
		print(str, 1);
		str = multiply_dt(str);
		print(str, 2);
		str = distribute(str);
		print(str, 3);

		// Notify if something went wrong
		try
		{
			str = separate(str, c);
			print(str, 4);
			str = multiplyNumbers(str);
			print(str, 5);
			setNames(new char[] {'R', 'T'}, new String[] {"r", "t"});
			generateCode(str, "TNextPulley");
		}
		catch (IndexOutOfBoundsException e)
		{
			System.err.println("Oh no, something broke!  Perhaps your variable doesn't " +
			"show up when the program tries to solve for it?");
			e.printStackTrace();
		}
	}

	// Separate terms in the initial input by using |
	public static String delimit(String str)
	{
		// Ignore signs in trig functions
		String v = "(sin|cos)\\([^\\)]*?(-|\\+)[^\\)]*?\\)";
		while (str.matches(".*" + v + ".*"))
		{
			Pattern p = Pattern.compile(v);
			Matcher m = p.matcher(str);
			m.reset();
			if (str.matches(".*" + v + ".*") && m.find())
			{
				int a = m.start();
				int b = m.end();
				String trig = str.substring(a, b).replace("+", ">").replace("-", "<");
				str = str.substring(0, a) + trig + str.substring(b);
			}
		}

		// Substitute and split, and fix trig functions
		str = str.replace(" - ", " + -");
		str = str.replace("+", "|");
		str = str.replace("<", "-").replace(">", "+");
		return str;
	}

	// Replace X by X_i and leave everything else unchanged
	public static String replaceVars(String str)
	{	
		String v = "[A-Z][^'\"_]";
		while (str.matches(".*" + v + ".*"))
		{
			Pattern p = Pattern.compile(v);
			Matcher m = p.matcher(str);
			m.reset();
			if (str.matches(".*" + v + ".*") && m.find())
			{
				int a = m.start();
				char var = str.charAt(a);
				String rep = var + "_i";
				str = str.substring(0, a) + rep + str.substring(a+1);
			}
		}

		return str;
	}

	// Replace X' and X" with their iterative counterparts
	public static String replaceDerivatives(String str)
	{
		// Replace first derivatives
		for (char c = 'A'; c <= 'Z'; c++)
			str = str.replaceAll(c + "'", " 1/(2dt) (" + c + "_iM1 - " + c + "_iP1) ");

		// Replace second derivatives
		for (char c = 'A'; c <= 'Z'; c++)
			str = str.replaceAll(c + "\"", " 1/sq(dt) (" + c + "_iP1 + " + c + "_iM1 - 2*" + c + "_i) ");

		return str;
	}

	// Multiply all terms by the largest power of dt in the denominators
	public static String multiply_dt(String str)
	{
		String[] terms = str.split("\\|");
		int[] pow_dt = new int[terms.length];
		int[] pow2 = new int[terms.length];

		// Iterate through all terms
		for (int i = 0; i < terms.length; i++)
		{
			String term = terms[i];

			// First derivatives
			int oldLength = term.length();
			term = term.replace("1/(2dt)", "");
			int diff = (oldLength - term.length()) / 7;
			pow_dt[i] += diff;
			pow2[i] += diff;

			// Second derivatives
			oldLength = term.length();
			term = term.replace("1/sq(dt)", "");
			diff = (oldLength - term.length()) / 8;
			pow_dt[i] += 2 * diff;

			terms[i] = term;
		}

		// Find GCDs
		int max_dt = 0;
		int max2 = 0;
		for (int i = 0; i < terms.length; i++)
		{
			max_dt = Math.max(pow_dt[i], max_dt);
			max2 = Math.max(pow2[i], max2);
		}

		// Multiply and join terms
		for (int i = 0; i < terms.length; i++)
		{
			String prefix = "2^" + (max2 - pow2[i]) + "*dt^" + (max_dt - pow_dt[i]) + "* ";
			terms[i] = terms[i].trim();
			if (terms[i].charAt(0) == '-')
				terms[i] = '-' + prefix + terms[i].substring(1);
			else
				terms[i] = prefix + terms[i];
		}
		str = join(terms, '|');
		str = str.replace("2^0", "1").replace("dt^0", "1");

		// Simplify
		str = removeSpaces(str);
		str = simplifySymbols(str);
		str = cleanUp(str);
		return str;
	}

	// Expand out all products
	public static String distribute(String str)
	{
		boolean change = true;
		str = removeSpaces(str);
		while (change)
		{
			change = false;
			String[] terms = str.split("\\|");
			for (int i = 0; i < terms.length; i++)
			{	
				int oldLength = terms[i].length();
				terms[i] = multiplyPairwise(terms[i]);
				change = change || (terms[i].length() != oldLength);
			}
			str = join(terms, '|');
			str = removeSpaces(str);
		}
		return str;
	}

	// Takes out all terms corresponding to var_iP1 and places the coefficients in the first term
	public static String separate(String str, char v)
	{
		String[] terms = str.split("\\|");
		String all = "";
		String var = v + "_iP1";

		// Go through all terms, one by one
		for (int i = 0; i < terms.length; i++)
		{
			String term = " " + terms[i];
			int start = term.indexOf(" (");

			// Find the coefficient, if it exists
			String coeff = "";
			if (start != -1)
				coeff = term.substring(0, start);
			if (coeff.length() == 0)
				coeff = "1";

			// Grab the expression inside the parentheses of a single term
			String parens = "(\\s|-)\\(.*?\\)";
			if (term.matches(".*" + parens + ".*"))
			{
				Pattern p1 = Pattern.compile(parens);
				Matcher m1 = p1.matcher(term);
				m1.reset();
				if (term.matches(".*" + parens + ".*") && m1.find())
				{	
					int a = m1.start();
					int b = m1.end();
					String mult = term.substring(a+1, b-1);

					/* Split each terms into more terms and get the ones with the
					 * variable we want, add them to the other side, then delete
					 */
					String[] innerTerms = mult.split("\\+");
					for (int j = 0; j < innerTerms.length; j++)
					{
						innerTerms[j] = innerTerms[j].trim().replace("(", "").replace(")", "");
						if (innerTerms[j].contains(var))
						{
							boolean flip = false;
							if (innerTerms[j].contains("-" + var))
								flip = true;

							String mod = innerTerms[j].replace(var, "");
							mod = mod.replace("* ", "").replace(" *", "").replace("**", "*");
							mod = mod.replace("(", "").replace(")", "");
							if (mod.length() == 0)
								mod = "1";

							if (flip)
								all = all + " + " + normalize(coeff + "*" + mod);
							else
								all = all + " + " + normalize("-" + coeff + "*" + mod);

							innerTerms[j] = "";
						}
					}

					// Join together the non-zeroed terms
					terms[i] = term.substring(0, a) + "(" + join(innerTerms, '+')
					+ ")" + term.substring(b);
				}
			}
		}

		// Rejoin all terms and the things on the other side, then filter
		str = join(terms, '|');
		str = all.substring(3) + " | " + str;
		str = str.replace(" 1*", "");

		// Simplify
		str = removeSpaces(str);
		str = simplifySymbols(str);
		return str;
	}

	// Modify the expressions to be more program code-friendly and cleaner-looking
	public static String cleanUp(String str)
	{
		// Replace 2^n and dt^n
		String v = "(2|dt)\\^\\d";
		while (str.matches(".*" + v + ".*"))
		{
			Pattern p = Pattern.compile(v);
			Matcher m = p.matcher(str);
			m.reset();
			if (str.matches(".*" + v + ".*") && m.find())
			{
				int a = m.start();
				if (str.charAt(a) == '2')
					str = str.substring(0, a) + "Math.pow(2, " + str.charAt(a+2)
					+ ")" + str.substring(a+3);
				else
					str = str.substring(0, a) + "Math.pow(dt, " + str.charAt(a+3)
					+ ")" + str.substring(a+4);
			}
		}

		// Replace Math.pow(2, n) and Math.pow(dt, 2) with numbers
		for (int i = 1; i <= 9; i++)
			str = str.replace("Math.pow(2, " + i + ")", "" + (1 << i));
		str = str.replace("Math.pow(dt, 1)", "dt");
		str = str.replace("Math.pow(dt, 2)", "dt2");

		// Process multi-asterisks and extra 1s, remove all multi-spaces, make subtraction look nice
		str = simplifySymbols(str);
		str = removeSpaces(str);
		str = str.replace(" + -", " - ");
		str = simplifySymbols(str);
		return str;
	}

	// Sets the capital-letter variable name mapping
	public static void setNames(char[] vars, String[] alias)
	{
		for (int i = 0; i < vars.length; i++)
			if (vars[i] >= 'A' && vars[i] <= 'Z' && vars[i] != 'M' && vars[i] != 'P')
				names[vars[i] - 'A'] = alias[i];
	}

	// Generates (prints) method code for the specified iteration process
	public static void generateCode(String str, String name)
	{
		// Split and trim the whole string
		String[] parts = str.split("\\|");
		for (int i = 0; i < parts.length; i++)
			parts[i] = parts[i].trim();

		// Determine what's needed in the header
		String v = "[A-Z]_i(M1|P1)?";
		boolean[] iM1_list = new boolean[26];
		boolean[] i_list = new boolean[26];
		boolean[] iP1_list = new boolean[26];
		if (str.matches(".*" + v + ".*"))
		{
			Pattern p = Pattern.compile(v);
			Matcher m = p.matcher(str);
			m.reset();
			while (m.find())
			{
				int a = m.start();
				int b = m.end();
				String var = str.substring(a,b);
				int index = var.charAt(0) - 'A';

				if (var.contains("iM1"))
					iM1_list[index] = true;
				else if (var.contains("iP1"))
					iP1_list[index] = true;
				else
					i_list[index] = true;
			}
		}

		// Put all variable names into a list
		LinkedList<String> all = new LinkedList<String>();
		for (int i = 0; i < 26; i++)
		{
			if (iM1_list[i])
				all.add((char) (i + 'A') + "_iM1");
			if (i_list[i])
				all.add((char) (i + 'A') + "_i");
			if (iP1_list[i])
				all.add((char) (i + 'A') + "_iP1");
		}

		// Generate the header
		String code = "\npublic static double _____(";
		Iterator<String> iter = all.iterator();
		if (iter.hasNext())
			code += "double " + iter.next();
		while (iter.hasNext())
			code += ", double " + iter.next();
		code += ")\n{";

		// Print the divisor and the terms to add, then clean up subtraction and "compress" terms
		code += "\n\tdouble div = " + parts[0] + ";\n";
		for (int i = 1; i < parts.length; i++)
			code += "\tdouble term" + i + " = "+ parts[i] + ";\n";
		code = code.replace("+ -", "- ");
		code = code.replace(" * ", "*");
		code = code.replace("-;", "-1;");

		// Print return statement
		code += "\treturn (term1";
		for (int i = 2; i < parts.length; i++)
			code += " + term" + i;
		code += ") / div;\n}";
		
		// Replace square brackets
		code = code.replace("[", "(").replace("]", ")");

		// Replace all variables with their specified names
		for (int i = 0; i < 26; i++)
			if (i + 'A' != 'M' && i + 'A' != 'P' && names[i] != null)
				code = code.replace("" + (char) (i + 'A'), names[i]);
		code = code.replace("_____", name);
		System.out.print(code);
	}

	// Take adjacent pairs of terms as (.*) and multiplies them together
	private static String multiplyPairwise(String str)
	{
		// Trim spaces, filter minus signs, and delete extraneous asterisks
		str = removeSpaces(str);
		str = simplifySymbols(str);
		str = str.replace(" - ", " + -");
		str = "  " + str;

		// Treat it on a term-by-term basis
		String dot = "(\\s|-)\\(.*?\\) \\* \\(.*?\\)";
		if (str.matches(".*" + dot + ".*"))
		{
			Pattern p = Pattern.compile(dot);
			Matcher m = p.matcher(str);
			m.reset();
			if (str.matches(".*" + dot + ".*") && m.find())
			{
				// Find match and separate the terms
				int a = m.start();
				int b = m.end();
				String mult = str.substring(a+2, b-1);
				int boundary = mult.indexOf(") * (");

				// Split and multiply
				String[] first = mult.substring(0, boundary).split("\\+");
				String[] second = mult.substring(boundary+5).split("\\+");
				String prod = "";
				for (int i = 0; i < first.length; i++)
					for (int j = 0; j < second.length; j++)
						prod = prod + " + " + normalize(first[i].trim() + "*" + second[j].trim());

				// Post-processing work to put it back in the original expression
				prod = prod.substring(3);
				prod = "(" + prod + ")";
				str = str.substring(0, a) + prod + str.substring(b);
			}
		}

		// Remove all multi-spaces and return
		str = removeSpaces(str);
		return str;
	}

	// Remove double asterisks, extraneous 1s and double parentheses, then pad asterisks with spaces
	private static String simplifySymbols(String str)
	{
		int oldLength = 0;
		while (str.length() != oldLength)
		{
			oldLength = str.length();
			str = str.replace("* |", "|");
			str = str.replace("* ", "*");
			str = str.replace(" *", "*");
			str = str.replace("**", "*");
			str = str.replace("*1 ", " ");
			str = str.replace(" 1*", " ");
			str = str.replace("*1*", "*");
		}
		str = str.replace("*", " * ");
		return str;
	}

	// Filter multi-spaces and remove spaces inside parentheses
	private static String removeSpaces(String str)
	{
		int oldLength = 0;
		while (str.length() != oldLength)
		{
			oldLength = str.length();
			str = str.replace("  ", " ");
			str = str.replace(" )", ")");
			str = str.replace("( ", "(");
		}
		return str;
	}

	// Try to multiply the first two numbers together
	private static String multiplyNumbers(String str)
	{
		int oldLength = 0;
		while (str.length() != oldLength)
		{
			oldLength = str.length();
			String[] terms = str.split("\\|");
			for (int i = 0; i < terms.length; i++)
			{
				String[] factors = terms[i].split("\\*");
				try
				{
					int i1 = Integer.parseInt(factors[0].trim());
					int i2 = Integer.parseInt(factors[1].trim());
					int product = i1 * i2;
					factors[0] = "";
					factors[1] = "" + product;
					terms[i] = join(factors, '*');
				}
				catch (NumberFormatException e)
				{}
			}
			str = join(terms, '|').trim();
			str = removeSpaces(str);
		}
		return str;
	}

	// Takes an array of terms and joins them together, separated by the given char
	private static String join(String[] a, char separator)
	{
		String str = "";
		for (int j = 0; j < a.length; j++)
			if (a[j].length() != 0)
				str = str + " " + separator + " " + a[j];
		str = str.substring(3);
		return str;
	}

	// Remove redundant negatives
	private static String normalize(String str)
	{
		// Ignore signs in trig functions
		String v = "(sin|cos)\\([^\\)]*?(-|\\+)[^\\)]*?\\)";
		while (str.matches(".*" + v + ".*"))
		{
			Pattern p = Pattern.compile(v);
			Matcher m = p.matcher(str);
			m.reset();
			if (str.matches(".*" + v + ".*") && m.find())
			{
				int a = m.start();
				int b = m.end();
				String trig = str.substring(a, b).replace("+", ">").replace("-", "<");
				str = str.substring(0, a) + trig + str.substring(b);
			}
		}

		// Count remaining negatives
		int count = str.split("-").length - 1;
		boolean isNegative = count % 2 == 1;
		str = str.replace("-", "");
		if (isNegative)
			str = "-" + str.trim();

		// Recover the original function
		str = str.replace("<", "-").replace(">", "+");
		return str;
	}

	private static void print(String str, int num)
	{
		if (!print)
			return;
		if (num <= 3)
			str = " | " + str;
		String[] a = str.split("\\|");
		System.out.println(num + " >>>");
		for (int i = 0; i < a.length; i++)
			System.out.println("\t" + i + ") \"" + a[i] + "\"");
	}
}