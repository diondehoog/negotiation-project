package negotiator.group7;

public class Log {
	private static final boolean DEBUG = false;

	public static void debugInLine(String shizzle) {
		if (DEBUG)
		System.out.print(shizzle);
	}
	public static void debugNewLine(String shizzle) {
		if (DEBUG)
		System.out.println(shizzle);
	}
	public static void inLine(String shizzle) {
		System.out.print(shizzle);
	}
	public static void newLine(String shizzle) {
		System.out.println(shizzle);
	}
}
