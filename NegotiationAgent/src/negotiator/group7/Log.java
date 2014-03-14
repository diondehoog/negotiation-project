package negotiator.group7;

import java.text.DecimalFormat;

public class Log {

	// General logging
	private static final int BLACK_HOLE = Integer.MIN_VALUE; // no debug output at all
	private static final int NONE = -1; // no ones debug output
	private static final int ALL = 0; 	// everyones debug output
	
	// Personal logging
	private static final int SCHUTTER = 1;
	private static final int VIERING = 2;
	private static final int RUNIA = 3;
	private static final int DADO = 4;
	private static final int HOKKE = 5;

	// Only very important outputs
	private static int PRINT = BLACK_HOLE;
	
	public static void s(String msg) {
		inLine(msg, SCHUTTER);
	}
	public static void sln(String msg) {
		newLine(msg, SCHUTTER);
	}
	
	public static void v(String msg) {
		inLine(msg, VIERING);
	}
	public static void vln(String msg) {
		newLine(msg, VIERING);
	}
	
	public static void r(String msg) {
		inLine(msg, RUNIA);
	}
	public static void rln(String msg) {
		newLine(msg, RUNIA);
	}
	
	public static void d(String msg) {
		inLine(msg, DADO);
	}
	public static void dln(String msg) {
		newLine(msg, DADO);
	}
	
	public static void h(String msg) {
		inLine(msg, HOKKE);
	}
	public static void hln(String msg) {
		newLine(msg, HOKKE);
	}
	
	public static void inLine(String msg) {
		if (PRINT != BLACK_HOLE)
			System.out.print(msg);
	}
	public static void newLine(String msg) {
		if (PRINT != BLACK_HOLE)
			System.out.println(msg);
	}
	
	public static void inLine(String msg, int type) {
		if (type == PRINT || PRINT == ALL)
			inLine(msg);
	}
	public static void newLine(String msg, int type) {
		if (type == PRINT || PRINT == ALL)
			newLine(msg);
	}
	public static String format(double d) {
		return format(d, "##.000");
	}
	
	public static String format(double d, String decimalFormat) {
		DecimalFormat f = new DecimalFormat(decimalFormat);  
		return f.format(d);
	}
}
