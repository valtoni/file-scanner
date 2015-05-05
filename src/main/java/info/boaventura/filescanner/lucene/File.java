package info.boaventura.filescanner.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;

public class File implements AutoCloseable {
	
	public static final String FIELD_PATH = "path";
	
	public static final String FIELD_LAST_MODIFIED = "modified";
	
	public static final String FIELD_CREATION_TIME = "creation";
	
	public static final String FIELD_LAST_ACCESS_TIME = "accessed";
	
	public static final String FIELD_CONTENTS = "contents";
	
	private Path path;
	
	private BasicFileAttributes attrs;
	
	private InputStream stream;
	
	public File(Path path) throws IOException {
		if (path == null) {
			throw new RuntimeException("Cannot initialize path with null");
		}
		this.path = path;
		this.attrs = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
		stream = Files.newInputStream(path);
	}
	
	public Document toDocument() throws IOException {
    Document doc = new Document();
    doc.add(new TextField(FIELD_PATH, path.toString(), Field.Store.YES));
    doc.add(new LongField(FIELD_LAST_MODIFIED, attrs.lastModifiedTime().toMillis(), Field.Store.NO));
    doc.add(new LongField(FIELD_CREATION_TIME, attrs.creationTime().toMillis(), Field.Store.NO));
    doc.add(new LongField(FIELD_LAST_ACCESS_TIME, attrs.lastAccessTime().toMillis(), Field.Store.NO));
    doc.add(new TextField(FIELD_CONTENTS, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
    return doc;
	}

	@Override
	public void close() throws IOException {
	  stream.close();
	}
	
	public String toString() {
	  return path.toString();
	}
	
}
