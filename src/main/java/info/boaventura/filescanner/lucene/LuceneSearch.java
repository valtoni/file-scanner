package info.boaventura.filescanner.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.NIOFSDirectory;

public class LuceneSearch implements AutoCloseable {

	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;

	private void setup(String  indexPath) throws IOException {
		reader = DirectoryReader.open(NIOFSDirectory.open(Paths.get(indexPath)));
    searcher = new IndexSearcher(reader);
    analyzer = new StandardAnalyzer();
	}
	
	public LuceneSearch(String indexPath) throws IOException {
		setup(indexPath);
	}
	
	public List<Document> simpleTopQuery(int hintNumber, String field, String text) throws ParseException, IOException {

		Query query = new QueryParser(field, analyzer).parse(text);
    TopScoreDocCollector collector = TopScoreDocCollector.create(hintNumber);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;

    searcher.search(query, collector);
    
    return toDocuments(hits);
	}
	
	private List<Document> toDocuments(ScoreDoc[] scoreDocs) throws IOException {
    List<Document> results = new ArrayList<Document>();
    for (ScoreDoc scoreDoc: scoreDocs) {
    	results.add(searcher.doc(scoreDoc.doc));
    }
    return results;
	}
	
	
	public List<Document> search(String field, String text) throws Exception {
		Query query = new QueryParser(field, analyzer).parse(text);
		TopDocs topDocs = searcher.search(query, 20);
		return toDocuments(topDocs.scoreDocs);
	}
	
	private boolean empty(String[] input) {
		return input == null || input.length == 0;
	}
	
	
	public List<Document> search(String[] field, String... text) throws Exception {
		// Validate inputs
		if (empty(field) || empty(text)) {
			throw new RuntimeException("Text and field cannot be empty");
		}
		
		// One-to-one QueryParser to text
		QueryParser[] parsers = new QueryParser[ field.length ];
		for (int i = 0; i < field.length; i++) {
			parsers[i] = new QueryParser(field[i], analyzer);
		}
		
		BooleanQuery booleanQuery = new BooleanQuery();
		for (int i = 0; i < parsers.length; i++) {
			for (int j = 0; j < text.length; j++) {
				booleanQuery.add(parsers[i].parse(text[j]), BooleanClause.Occur.SHOULD);
			}
		}
		
		TopScoreDocCollector topScoreCollector = TopScoreDocCollector.create(100);
		CachingCollector cachingCollector = CachingCollector.create(topScoreCollector, true, 16.0);
		
		searcher.search(booleanQuery, cachingCollector);
		
		return toDocuments(topScoreCollector.topDocs().scoreDocs);
		
	}
	
	public List<String> searchStr(String[] field, String... text) throws Exception {
		List<String> documentsPaths = new ArrayList<String>();
		for (Document document: search(field, text)) {
			documentsPaths.add(document.getField(File.FIELD_PATH).stringValue());
		}
		return documentsPaths;
	}
	
	public void close() throws Exception {
		reader.close();
	}

	public static void main2(String[] args) throws Exception {
		List<Document> results;
	  try (LuceneSearch search = new LuceneSearch("C:/TEMP/lucene-3")) {
	  	results = search.search("contents", "root");
		  System.out.format("%d results was found\n", results.size());
		  int i = 0;
		  for (Document d: results) {
		  	System.out.println(++i + ": " + d.getField("path").stringValue());
		  }
    }
  }

	
	public static void main(String[] args) throws Exception {
		List<Document> results;
	  try (LuceneSearch search = new LuceneSearch("C:/TEMP/lucene-3")) {
	  	//results = search.simpleTopQuery(20, "classpath", "jdk");
	  	results = search.search(new String[] { "path", "contents" }, "cgEPBR");
		  System.out.format("%d results was found\n\n", results.size());
		  int i = 0;
		  for (Document d: results) {
		  	System.out.println(++i + ": " + d.getField("path").stringValue());
		  }
    }
  }



}
