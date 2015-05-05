package info.boaventura.filescanner.search;

public class MatchedTextFileSnippet {

	private int line;
	
	private String textLine;
	
	public MatchedTextFileSnippet(int line, String textLine) {
		super();
		this.line = line;
		this.textLine = textLine;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getTextLine() {
		return textLine;
	}

	public void setTextLine(String textLine) {
		this.textLine = textLine;
	}
		
}
