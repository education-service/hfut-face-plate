package edu.hfut.lpr.run;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 *
 * @author wanggang
 *
 */
public class PlateRecognizerTest {

	@Test
	public void testRecognizeResult() throws IOException {
		BufferedImage image = ImageIO.read(new File("src/test/resources/en-snapshots/test_041.jpg"));
		String spz = PlateRecognizer.recognizeResult(image);
		assertEquals("LM025BD", spz);
	}

}
