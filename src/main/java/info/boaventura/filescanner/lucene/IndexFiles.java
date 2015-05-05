package info.boaventura.filescanner.lucene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;


public class IndexFiles implements Runnable {
  
	private final String dataPath;
	
	private final String indexPath;
	
	private final String globPattern;
	
	private boolean create;
	
	private double ramBufferSize = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
	
	private IndexFilesListener listener = new IndexFilesListener() {
		
		@Override
		public void onError(Throwable error) {
			throw new RuntimeException(error);
		}

		@Override
    public void onStartDoIndex() {
    }

		@Override
    public void onEndDoIndex() {
    }

		@Override
    public void afterAddDocument(Document document) {
    }

		@Override
    public void beforeIndexPath(Path path) {
    }

		@Override
    public void afterIndexPath(Path path) {
    }
		
	};

	public IndexFiles(String dataPath, String indexPath, String globPattern, boolean create) {
  	this.dataPath = dataPath;
  	this.indexPath = indexPath;
  	this.create = create;
  	this.globPattern = globPattern;
  }

	/**
	 * 
	 * @param dataPath
	 * @param indexPath
	 * @param globPattern
	 * @param create
	 * @param ramBufferSize for better indexing performance, if you are indexing many documents, increase the RAM
	 *        buffer.  But if you do this, increase the max heap size to the JVM.
	 */
  public IndexFiles(String dataPath, String indexPath, String globPattern, boolean create, double ramBufferSize) {
  	this.dataPath = dataPath;
  	this.indexPath = indexPath;
  	this.create = create;
  	this.globPattern = globPattern;
  	this.ramBufferSize = ramBufferSize;
  }
  
  public void setListener(IndexFilesListener listener) {
	  this.listener = listener;
  }

  public void setRamBufferSize(double ramBufferSize) {
	  this.ramBufferSize = ramBufferSize;
  }
  
  @Override
  public void run() {
    final Path docsPathDir = Paths.get(dataPath);
    listener.onStartDoIndex();

    // TODO "isReadable" isn't works in windows (???)
/*    if (!Files.isReadable(dataDir)) {
    	listener.onError(new Exception("Document directory '" + dataDir.toAbsolutePath() + "' does not exist or is not readable, please check the path"));
    	listener.onEndDoIndex();
    	return;
    }
*/   
    
    IndexWriter writer = null;
    
    try {
    	Directory luceneDir = NIOFSDirectory.open(Paths.get(indexPath));
	    Analyzer analyzer = new StandardAnalyzer();
	    
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);

	    config.setRAMBufferSizeMB(ramBufferSize);
	
	    if (create) {
	    	// Remove any previously indexed documents:
	      config.setOpenMode(OpenMode.CREATE);
	    } else {
	      // Add new documents to an existing index:
	      config.setOpenMode(OpenMode.CREATE_OR_APPEND);
	    }
	
	    // Create index writer
	    writer = new IndexWriter(luceneDir, config);

	    // Walks recursively in informed path
	 		Files.walkFileTree(docsPathDir, new FileIndexSearchVisitor(listener, writer, globPattern));

    } catch (IOException e) {
    	listener.onError(new Exception("Não foi possível indexar os arquivos", e));
    } finally {
			if (writer != null) {
				try {
	        writer.commit();
	        writer.close();
        } catch (IOException e) {
        	throw new RuntimeException("Writer não pôde ser fechado", e);
        }
			}
    	listener.onEndDoIndex();
    }
    
  }
  
	public static void main(String[] args) {
		String dataPath = "C:/work/workspaces/eclipse-sdiep-impact",
				indexPath = "C:/TEMP/lucene-3", 
				globPattern = "**/*.{gsp,groovy,css,xhtml,html,htm,tld,nsi,bat,sh,py,java,xml,properties}";
		boolean create = true;
		Thread thread = new Thread(new IndexFiles(dataPath, indexPath, globPattern, create));
		thread.start();
  }

  
  
}
