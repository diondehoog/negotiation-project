package negotiator.group7;

public class Log {
	
	/**
	 * OPTIONS:
	 */
	private static final int NONE = -1; // no ones debug output
	private static final int ALL = 0; // everyones debug output
	private static final int SCHUTTER = 1;
	private static final int VIERING = 2;
	private static final int RUNIA = 3;
	private static final int DADO = 4;
	private static final int HOKKE = 5;
	
	/**
	 * What to print?
	 */
	private static final int PRINT = ALL; // set to NONE for only important outputs
	
	
	
	public static void s(String shizzle) {
		inLine(shizzle, SCHUTTER);
	}
	public static void sln(String shizzle) {
		newLine(shizzle, SCHUTTER);
	}
	
	public static void v(String shizzle) {
		inLine(shizzle, VIERING);
	}
	public static void vln(String shizzle) {
		newLine(shizzle, VIERING);
	}
	
	public static void r(String shizzle) {
		inLine(shizzle, RUNIA);
	}
	public static void rln(String shizzle) {
		newLine(shizzle, RUNIA);
	}
	
	public static void d(String shizzle) {
		inLine(shizzle, DADO);
	}
	public static void dln(String shizzle) {
		newLine(shizzle, DADO);
	}
	
	public static void h(String shizzle) {
		inLine(shizzle, HOKKE);
	}
	public static void hln(String shizzle) {
		newLine(shizzle, HOKKE);
	}
	
	public static void inLine(String shizzle) {
		System.out.print(shizzle);
	}
	public static void newLine(String shizzle) {
		System.out.println(shizzle);
	}
	
	public static void inLine(String shizzle, int type) {
		if (type == PRINT || PRINT == ALL) {
			inLine(shizzle);
		}
	}
	public static void newLine(String shizzle, int type) {
		if (type == PRINT || PRINT == ALL) {
			newLine(shizzle);
		}
	}
}
