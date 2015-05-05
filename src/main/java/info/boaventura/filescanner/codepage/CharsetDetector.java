package info.boaventura.filescanner.codepage;

import java.io.FileInputStream;

import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetector {

	private final int BUFFER_CHARS_READED = 4096; 
	
	public static final String ASCII_ENCODING = "ASCII";
	
	private UniversalDetector detector = new UniversalDetector(null);
	
	public CharsetDetector() {
		detector = new UniversalDetector(null);
	}
	
	public CharsetDetector(CharsetListener charsetListener) {
		detector = new UniversalDetector(charsetListener);
	}
	
	public String detect(String fileName) throws java.io.IOException {
		// Declarations
		byte[] buf = new byte[BUFFER_CHARS_READED];
		int nread;
		try (FileInputStream fis = new FileInputStream(fileName)) {
			String encoding;
	
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();
	
			encoding = detector.getDetectedCharset();
			detector.reset();
			if (encoding != null) {
				return encoding;
			} 
			else {
				return ASCII_ENCODING;
			}
		}
		
	}

}
