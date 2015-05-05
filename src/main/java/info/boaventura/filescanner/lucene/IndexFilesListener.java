package info.boaventura.filescanner.lucene;

import java.nio.file.Path;

import org.apache.lucene.document.Document;

public interface IndexFilesListener {
	
	void onError(Throwable error);
	
	void onStartDoIndex();
	
	void onEndDoIndex();

	void afterAddDocument(Document document);

	void beforeIndexPath(Path path);
	
	void afterIndexPath(Path path);
	
}
