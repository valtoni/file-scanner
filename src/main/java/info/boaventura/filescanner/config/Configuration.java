package info.boaventura.filescanner.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Configuration {

	public static final Path USER_HOME = Paths.get(System.getProperty("user.home"));
	
	public static final Path APP_HOME = Paths.get(USER_HOME.toString(), ".filescanner");
	
}
