package info.boaventura.filescanner.search;

public class TextFormatFoundedSnippet implements FormatFoundedSnippet {

	private String format(String input, Object... vars) {
		return String.format(input, vars);
	}
	
	public String format(MatchedTextFile matchedTextFile) {
		StringBuffer mat = new StringBuffer(format("Localização: %s", matchedTextFile.getFile().getAbsoluteFile()) + "\n");
		for (MatchedTextFileSnippet snippet: matchedTextFile.getMatches()) {
			 mat.append(format("%d: %s", snippet.getLine(), snippet.getTextLine()));
		}
		return mat.toString();
	}

}
