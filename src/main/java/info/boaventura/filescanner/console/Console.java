package info.boaventura.filescanner.console;

public final class Console {

	public static final String ESCAPE = "\033[";
	
	public static final String CLEAR = "2J";
	
	public static final String LINE_COLUMN = "%d;%dH";
	
	public static final String RESET = "0m";
	
	private static class ConsolePrinter {
		
		private String escCode = "";
		
		public ConsolePrinter add(String escCode) {
			this.escCode += ESCAPE + escCode;
			return this;
		}

		public ConsolePrinter add(String escCode, Object... parameters) {
			this.escCode += String.format(escCode, parameters);
			return this;
		}
		
		@Override
		public String toString() {
			return escCode;
		}
		
	}
	
	public static void clear() {
		System.out.printf(new ConsolePrinter().add(CLEAR).toString());
	}

	public static void gotoXY(int line, int column) {
		System.out.printf(new ConsolePrinter().add(LINE_COLUMN, line, column).toString());
	}



	public static void printc(String message, String... listArgs) {
		ConsolePrinter cp = new ConsolePrinter();
		int i = 0;
		for (String arg: listArgs) {
			cp.add(arg + (++i >= listArgs.length ? "m" : ";"));
		}
		cp.add(RESET);
		System.out.printf(cp.toString());
	} 

	public static final void main(String[] args) {
		clear();
		gotoXY(1, 16);
		printc("Legal filhooo");
	}
	
}
