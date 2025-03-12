package org.example.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {
	private static final String PROJECT_DIR = System.getProperty("user.dir");

	public static File inputFolder() {
		var resourcesDir = PROJECT_DIR + "\\src\\main\\resources\\in";
		return new File(resourcesDir);
	}

	public static File outputFolder() throws IOException {
		var resourcesDir = PROJECT_DIR + "\\src\\main\\resources\\out";
		Path path = Paths.get(resourcesDir);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		return new File(resourcesDir);
	}
}
