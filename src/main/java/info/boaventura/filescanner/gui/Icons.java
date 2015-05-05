package info.boaventura.filescanner.gui;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class Icons {

	private static final String ICON_DIR = "images/icons/";
	
	private static Image create(Display display, String file) {
		InputStream imageStream = Icons.class.getClassLoader().getResourceAsStream(ICON_DIR + file);
		return new Image(display, imageStream);
	}
	
	public static final void load(Shell shell) {
		Display display = shell.getDisplay();
		Image icon_32 = create(display, "file_search_32.png");
		Image icon_48 = create(display, "file_search_48.png");
		Image icon_64 = create(display, "file_search_64.png");
		Image icon_128 = create(display, "file_search_128.png");
		Image[] images = { icon_32, icon_48, icon_64, icon_128 }; 
		shell.setImages(images);
	}
	
	
}
