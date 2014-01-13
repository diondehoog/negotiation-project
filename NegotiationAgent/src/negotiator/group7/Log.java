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
	private static final int PRINT = HOKKE; // set to NONE for only important outputs
	
	
	
	public static void s(String dikke_grote_tieten) {
		inLine(dikke_grote_tieten, SCHUTTER);
	}
	public static void sln(String dikke_grote_tieten) {
		newLine(dikke_grote_tieten, SCHUTTER);
	}
	
	public static void v(String dikke_grote_tieten) {
		inLine(dikke_grote_tieten, VIERING);
	}
	public static void vln(String dikke_grote_tieten) {
		newLine(dikke_grote_tieten, VIERING);
	}
	
	public static void r(String dikke_grote_tieten) {
		inLine(dikke_grote_tieten, RUNIA);
	}
	public static void rln(String dikke_grote_tieten) {
		newLine(dikke_grote_tieten, RUNIA);
	}
	
	public static void d(String dikke_grote_tieten) {
		inLine(dikke_grote_tieten, DADO);
	}
	public static void dln(String dikke_grote_tieten) {
		newLine(dikke_grote_tieten, DADO);
	}
	
	public static void h(String dikke_grote_tieten) {
		inLine(dikke_grote_tieten, HOKKE);
	}
	public static void hln(String dikke_grote_tieten) {
		newLine(dikke_grote_tieten, HOKKE);
	}
	
	public static void inLine(String dikke_grote_tieten) {
		System.out.print(dikke_grote_tieten);
	}
	public static void newLine(String dikke_grote_tieten) {
		System.out.println(dikke_grote_tieten);
	}
	
	public static void inLine(String dikke_grote_tieten, int type) {
		if (type == PRINT || PRINT == ALL) {
			inLine(dikke_grote_tieten);
		}
	}
	public static void newLine(String dikke_grote_tieten, int type) {
		if (type == PRINT || PRINT == ALL) {
			newLine(dikke_grote_tieten);
		}
	}
}
