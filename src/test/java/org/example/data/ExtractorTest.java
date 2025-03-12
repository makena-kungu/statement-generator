package org.example.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtractorTest {

	@Test
	void testFileDisplay() {
		final var files = Extractor.files(Resources.inputFolder());
		files.forEach((key, value) -> System.out.println(key + "\t" + value));
	}

	@Test
	void testInputFileSelection() {

		System.setIn(new ByteArrayInputStream("1".getBytes()));
		String s = Extractor.inputFile();
		System.out.println(s);
		System.setIn(System.in);
	}
}