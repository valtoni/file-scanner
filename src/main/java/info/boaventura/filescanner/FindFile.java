package info.boaventura.filescanner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FindFile {

	// This filter only returns directories
	FileFilter directoryFilter = new FileFilter() {
	    public boolean accept(File file) {
	        return file.isDirectory();
	    }
	};
	
	FileFilter mainFilter;
	
	List<File> founded; 
	
	public FindFile() {

	}

	public void find(File directory, final String... matches) {
		founded = new ArrayList<File>();
		mainFilter = new FileFilter() {
			public boolean accept(File pathname) {
				boolean accept = !pathname.isDirectory();
				for (String match: matches) {
					accept &= pathname.getName().matches(match);
				}
				return accept;
			}
		};
		directoriesFind(directory);
	}

	private void directoriesFind(File directory) {
		for (File actualDir: directory.listFiles(directoryFilter)) {
			directoriesFind(actualDir);
			filesFind(actualDir);
		}
	}
	
	
	private void filesFind(File directory) {
		founded.addAll(Arrays.asList(directory.listFiles(mainFilter)));
	}

	public List<File> getFilesFound() {
		return founded;
	}
	
	public static final void main(String[] args) {
		FindFile todos = new FindFile();
		todos.find(new File("."), ".*java");
		for (File file: todos.getFilesFound()) {
			System.out.println(file.getAbsolutePath());
		}
	}
	
}


