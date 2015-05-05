package info.boaventura.filescanner.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;

public class SearchResult {

	public class Occurrence {
		
		int line;
		String text;
		
		public Occurrence(int line, String text) {
			this.line = line;
			this.text = text;
		}

		public int getLine() {
			return line;
		}

		public String getText() {
			return text;
		}
		
	}
	
	private Document document;
	
	private int hints;
	
	private List<Occurrence> occurrencies;
	
	public SearchResult(Document document) {
		this.document = document;
		this.occurrencies = new ArrayList<SearchResult.Occurrence>();
	}

	public void addOccurrence(int line, String text) {
		this.hints++;
		this.occurrencies.add(new Occurrence(line, text));
	}

	public Document getDocument() {
		return document;
	}

	public List<Occurrence> getOccurrencies() {
		return occurrencies;
	}
	
	public int getHints() {
		return hints;
	}
	
}
