package info.boaventura.filescanner.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Grep {

    // Charset and decoder for ISO-8859-15
    private Charset charset;
    private CharsetDecoder decoder;

    // Pattern used to parse lines
    private Pattern linePattern = Pattern.compile(".*\r?\n");

    // The input pattern that we're looking for
    private Pattern pattern;

    public Grep() {
    	setupCharset(Charset.defaultCharset());
    }
    
    public Grep(Charset charset) {
    	setupCharset(charset);
    }
    
    public Grep(Charset charset, String pattern) {
    	if (charset == null) {
    		setupCharset(Charset.defaultCharset());
    	}
    	else {
    		setupCharset(charset);
    	}
    	compile(pattern);
    }

    public Grep(String pattern) {
    	setupCharset(Charset.defaultCharset());
    	compile(pattern);
    }
    
    public void setupCharset(Charset charset) {
    	this.charset = charset;
    	decoder = this.charset.newDecoder();
    }
    
    // Compile the pattern
    public void compile(String pattern) throws PatternSyntaxException {
	    this.pattern = Pattern.compile(pattern);
    }

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    private List<MatchedTextFile> grep(File f, CharBuffer cb) {
    	List<MatchedTextFile> matches = new ArrayList<MatchedTextFile>();
		Matcher lm = linePattern.matcher(cb);	// Line matcher
		Matcher pm = null;			// Pattern matcher
		int lines = 0;
		MatchedTextFile matchedTextFile = new MatchedTextFile(f);
		while (lm.find()) {
		    lines++;
		    CharSequence cs = lm.group(); 	// The current line
		    if (pm == null) {
		    	pm = pattern.matcher(cs);
		    }
		    else {
		    	pm.reset(cs);
		    }
		    if (pm.find()) {
		    	matchedTextFile.addMatch(new MatchedTextFileSnippet(lines, cs.toString()));
		    }
		    if (lm.end() == cb.limit()) {
		    	break;
		    }
		}
		if (matchedTextFile.getMatches().size() > 0) {
			matches.add(matchedTextFile);
		}
		return matches;
    }

    // Search for occurrences of the input pattern in the given file
    public List<MatchedTextFile> grep(File f) throws IOException {
    	List<MatchedTextFile> matches;
    	// Open the file and then get a channel from the stream
    	FileInputStream fis = new FileInputStream(f);
    	try {
			FileChannel fc = fis.getChannel();
		
			// Get the file's size and then map it into memory
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
		
			// Decode the file into a char buffer
			CharBuffer cb = decoder.decode(bb);
	
			// Perform the search
			matches = grep(f, cb);
			
			// Close the channel and the stream
			fc.close();
	
			return matches;
    	} finally {
			fis.close();
		}
    }

}