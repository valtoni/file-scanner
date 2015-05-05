package info.boaventura.filescanner.search;

public class JiraFormatFoundSnippet implements FormatFoundedSnippet {

	private String format(String input, Object... vars) {
		return String.format(input + "\n", vars);
	}
	
	public String format(MatchedTextFile matchedTextFile) {
		StringBuffer mat = new StringBuffer(format("(*b) *Localização*: %s", matchedTextFile.getFile().getAbsoluteFile()));
		mat.append(format("{code:title=%s|borderStyle=solid}", matchedTextFile.getFile().getName()));
		for (MatchedTextFileSnippet snippet: matchedTextFile.getMatches()) {
			 mat.append(format("%d: %s", snippet.getLine(), snippet.getTextLine()));
		}
		mat.append(format("{code}"));
		return mat.toString();
	}

}
