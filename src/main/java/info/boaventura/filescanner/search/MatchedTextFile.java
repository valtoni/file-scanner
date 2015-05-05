package info.boaventura.filescanner.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MatchedTextFile {

	private File file;
	
	private List<MatchedTextFileSnippet> matches;
	
	public MatchedTextFile(File file) {
		super();
		this.file = file;
		matches = new ArrayList<MatchedTextFileSnippet>();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void addMatch(MatchedTextFileSnippet matchedTextFileSnippet) {
		matches.add(matchedTextFileSnippet);
	}
	
	public List<MatchedTextFileSnippet> getMatches() {
		return matches;
	}
	
	@Override
	public String toString() {
		return file.getAbsolutePath() + " (" + matches.size() + ")"; 
	}
	
}
