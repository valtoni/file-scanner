package info.boaventura.filescanner.lucene;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;

public class FileIndexSearchVisitor implements FileVisitor<Path> {

	private IndexWriter writer;
	
	private Logger log = LogManager.getLogger(FileIndexSearchVisitor.class);
	
	private PathMatcher matcher;
	
	private IndexFilesListener listener;
	
	public FileIndexSearchVisitor(IndexFilesListener listener, IndexWriter writer, String globPattern) {
		this.listener = listener;
	  this.writer = writer;
	  this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
  }
	
  public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
  	listener.beforeIndexPath(path);
	  return FileVisitResult.CONTINUE;
  }

	
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
  	
  	// File name must match specified pattern
  	if (path == null || !matcher.matches(path)) {
  		return FileVisitResult.CONTINUE;
  	}
  	
  	try (File file = new File(path)) {
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				log.debug("Adding to index: " + file);
				writer.addDocument(file.toDocument());
				listener.afterAddDocument(file.toDocument());
			} else {
				log.debug("Updating on index: " + file);
				writer.updateDocument(new Term("path", file.toString()), file.toDocument());
				listener.afterAddDocument(file.toDocument());
			}
  	} catch (IOException e) {
  		listener.onError(e);
  	}
		return FileVisitResult.CONTINUE;
  }

	
  public FileVisitResult visitFileFailed(Path path, IOException exception) throws IOException {
  	log.error("Cannot read file " + path.toString(), exception);
  	listener.onError(exception);
	  return FileVisitResult.CONTINUE;
  }

	
  public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
  	log.debug("Scanned directory " + path.toString());
  	listener.afterIndexPath(path);
  	return FileVisitResult.CONTINUE;
  }

}
