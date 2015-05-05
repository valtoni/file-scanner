package info.boaventura.filescanner.search;

import info.boaventura.filescanner.codepage.CharsetDetector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class GrepFinder implements Runnable {

	private Grep grep;
	
	private List<String> files;
	
	private String pattern;
	
	private CharsetDetector charsetDetector = new CharsetDetector();
	
	private List<MatchedTextFile> matchedList;
	
	Logger log = Logger.getLogger(GrepFinder.class);
	
	private Charset findCharset(String file) throws IOException {
		String charsetDetected = charsetDetector.detect(file);
		log.debug("Detected charset " + charsetDetected + " for file " + file);
		return Charset.forName(charsetDetected);
	}
	
	public GrepFinder(List<String> files) {
		this.files = files;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public List<MatchedTextFile> getMatchedTexts() {
		return matchedList;
	}
	
	public void find() throws IOException {
		grep = new Grep(pattern);
		matchedList = new ArrayList<MatchedTextFile>();
		for (String file: files) {
			try {
				grep.setupCharset(findCharset(file));
				matchedList.addAll(grep.grep(new File(file)));
			} catch (IOException e) {
				log.error("File " + file + " cannot be listed: codepage was not found");
			}
		}
	}
	
	
	@Override
  public void run() {
		try {
	    find();
    } catch (IOException e) {
	    e.printStackTrace();
    }
  }

	
	
}
