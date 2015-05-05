package info.boaventura.filescanner;

import static junit.framework.Assert.*;
import info.boaventura.filescanner.FindFileText;
import info.boaventura.filescanner.search.MatchedTextFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread.State;
import java.util.Observable;
import java.util.Observer;

import org.junit.Test;

public class FindFileTextTest {

	@Test
	public void testFindFileTextStringStringString() throws FileNotFoundException {
		String thisDir = getClass().getClassLoader().getResource(".").toString();
		System.out.println(thisDir);
		String projectBaseDir = new File(thisDir).getParentFile().getParentFile().getPath().replaceAll("file:\\\\", "");
		System.out.println(projectBaseDir);
		FindFileText findFileText = new FindFileText(projectBaseDir, "*.java");
		for (MatchedTextFile matchedTextFile: findFileText.findFileText("Screen")) {
			System.out.println(matchedTextFile);
		}
	}

	
	private int callBacks;
	
	@Test
	public void testFindFileTextThread() throws FileNotFoundException {
		String thisDir = getClass().getClassLoader().getResource(".").toString();
		System.out.println(thisDir);
		String projectBaseDir = new File(thisDir).getParentFile().getParentFile().getPath().replaceAll("file:\\\\", "");
		System.out.println(projectBaseDir);
		FindFileText findFileText = new FindFileText(projectBaseDir, "*.java", "Screen");
		
		Observer observador = new Observer() {
			
			@Override
			public void update(Observable paramObservable, Object paramObject) {
				System.out.println("Event received");
				FindFileText.Event event = (FindFileText.Event)paramObject;
				System.out.println(event.getType());
				callBacks++;
			}
			
		};
		
		
		findFileText.addObserver(observador);
		
		Thread thread = new Thread(findFileText);
		thread.start();
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(State.TERMINATED, thread.getState());
		assertTrue(callBacks > 0);
	}


	
}
