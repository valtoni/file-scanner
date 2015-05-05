package info.boaventura.filescanner;

import info.boaventura.filescanner.codepage.CharsetDetector;
import info.boaventura.filescanner.search.FormatFoundedSnippet;
import info.boaventura.filescanner.search.Grep;
import info.boaventura.filescanner.search.JiraFormatFoundSnippet;
import info.boaventura.filescanner.search.MatchedTextFile;
import info.boaventura.filescanner.search.TextFormatFoundedSnippet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class FindFileText extends Observable implements Runnable {

	private String filePattern;
	
	private FindFile findFile;

	private File initialDir;
	
	private Charset charset;
	
	private List<File> fileList;

	private String textPattern;
	
	public enum EventType { COUNT_ENTRIES, COUNT_MATCHED_TEXT, TERMINATED, COUNT_FILE_SEARCH };
	
	private boolean hasChanged;

	private String directory;
	
	public class Event {
		
		private EventType type;
		
		private Object wrapper;

		public Event(EventType type, Object wrapper) {
			super();
			this.type = type;
			this.wrapper = wrapper;
		}

		public EventType getType() {
			return type;
		}

		public Object getWrapper() {
			return wrapper;
		}
		
	}
	
	public FindFileText() {
		
	}
	
	public FindFileText(String directory, String filePattern, String textPattern) throws FileNotFoundException {
		super();
		setup(directory, filePattern, textPattern);
	}
	
	public FindFileText(String directory, String filePattern) throws FileNotFoundException {
		setup(directory, filePattern, null);
	}
	
	public void setup(String directory, String filePattern, String textPattern) throws FileNotFoundException {
		if (directory == null || filePattern == null) {
			throw new RuntimeException("Neither directory or filePattern can be empty");
		}
		if (!filePattern.equals(this.filePattern) || !directory.equals(this.directory)) {
			this.filePattern = filePattern;
			this.findFile = new FindFile();
			this.initialDir = new File(directory);
			this.directory = directory;
			if (!initialDir.exists()) {
				throw new FileNotFoundException("Diretorio nao encontrado: " + initialDir);
			}
			this.hasChanged = true;
		}
		this.textPattern = textPattern;
	}

	private String commonFilePatterns(String filePattern) {
		if (filePattern.equals("*.*")) {
			return ".*";
		}
		else if (filePattern.endsWith(".*")) {
			return filePattern + "(\\.).*";
		}
		else if (filePattern.startsWith("*.")) {
			return ".*(\\.)" + filePattern;
		}
		return filePattern;
	}

	public void setupCharset(Charset charset) {
		this.charset = charset;
	}
	
	private synchronized void matchFiles() {
		if (!hasChanged && (this.initialDir == null || this.filePattern == null)) {
			throw new RuntimeException("You must set initial dir and file pattern to do a search");
		}
		if (hasChanged) {
			findFile.find(initialDir, commonFilePatterns(filePattern));
			fileList = findFile.getFilesFound();
		}
		setChanged();
		notifyObservers(new Event(EventType.COUNT_ENTRIES, fileList));
		hasChanged = false;
	}
	
	public int filesMatched() {
		matchFiles();
		return this.fileList.size();
	}
	
	public synchronized List<MatchedTextFile> findFileText(String textPattern) throws FileNotFoundException {
		Grep grepFile;
		List<MatchedTextFile> matchedList = new ArrayList<MatchedTextFile>();
		CharsetDetector charsetDetector = new CharsetDetector();
		
		// Match files specified in filePattern
		matchFiles();

		grepFile = new Grep(charset, textPattern);

		
		for (File fileFound: fileList) {
			try {
				List<MatchedTextFile> matchedEntries = grepFile.grep(fileFound);
				// If matched some entry, notify objects
				if (matchedEntries.size() > 0) {
					setChanged();
					notifyObservers(new Event(EventType.COUNT_MATCHED_TEXT, matchedEntries));
					matchedList.addAll(matchedEntries);
				}
			} catch (IOException e) {
				if (charset != null) {
					System.err.printf("Cannot handle file %s: %s\n", fileFound, e.getMessage());
				}
				else {
					try {
						String charsetDetected = charsetDetector.detect(fileFound.toString());
						System.out.println("Detected charset " + charsetDetected + " for file " + fileFound);
						grepFile.setupCharset(Charset.forName(charsetDetected));
						matchedList.addAll(grepFile.grep(fileFound));
					} catch (IOException e1) {
						System.err.printf("Cannot auto-detect codepage for file %s: %s\n", fileFound, e.getMessage());
					}
				}
			}
			setChanged();
			notifyObservers(new Event(EventType.COUNT_FILE_SEARCH, null));
		}
		
		return matchedList;
	}
	

	private String findFileTextGeneral(FormatFoundedSnippet formatter, String textPattern) throws FileNotFoundException {
		StringBuffer buff = new StringBuffer();
		for (MatchedTextFile matchedTextFile: findFileText(textPattern)) {
			buff.append(formatter.format(matchedTextFile));
		}
		return buff.toString();
	}
	
	public String findFileTextInJiraFormat(String textPattern) throws FileNotFoundException {
		return findFileTextGeneral(new JiraFormatFoundSnippet(), textPattern);
	}
	
	public String findFileTextInPlainFormat(String textPattern) throws FileNotFoundException {
		return findFileTextGeneral(new TextFormatFoundedSnippet(), textPattern);
	}
	
	
	public static final void main(String[] args) {
		try {
			FindFileText findFile = new FindFileText("D:/Documents and Settings/Y6E2/Meus documentos/projetos/servwli", "*.java");
			//Charset charsetToFind = Charset.forName("UTF-8");
			//System.out.println("Finding in charset: " + charsetToFind.displayName());
			//System.out.println(findFileTextInJiraFormat(Charset.defaultCharset(), "D:/Documents and Settings/Y6E2/Meus documentos/projetos/servwli", "*.java", "PerdaOleoService|PerdaGasService|PerdaInjecaoService|OportunidadeService|AmeacaService"));
			System.out.println(findFile.findFileTextInPlainFormat("AcompanhamentoDiarioConsumoGasService"));
			//System.out.println(findFileTextInPlainFormat(charsetToFind, "D:/Documents and Settings/Y6E2/Meus documentos/projetos/servwli", "*.java", "NO_MALHA.NOMA_IN_FISCAL"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			List<MatchedTextFile> list = findFileText(this.textPattern);
			setChanged();
			notifyObservers(new Event(EventType.TERMINATED, list));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
}
