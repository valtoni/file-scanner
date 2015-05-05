package info.boaventura.filescanner;

import info.boaventura.filescanner.codepage.CharsetDetector;

import java.io.File;
import java.io.IOException;

public class FindFileCharset {
	
	public static final void main(String[] args) {
		FindFile files = new FindFile();
		files.find(new File("D:/Documents and Settings/Y6E2/Meus documentos/projetos/servwli"), ".*java");
		CharsetDetector charsetDetector = new CharsetDetector();
		for (File file: files.getFilesFound()) {
			try {
				System.out.println("[" + charsetDetector.detect(file.getAbsolutePath()) + "] " + file.getAbsolutePath());
			} catch (IOException e) {
				System.out.println("[???] " + file.getAbsolutePath());
			}
		}
	}
	
}