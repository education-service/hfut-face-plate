package edu.hfut.lpr.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.threshold.OtsuThreshold;

public class ChinesePlate {

	public static void main(String[] args) throws IOException {

		//		String spz = PlateRecognizer.recognizeResult("china-plates/test_001.jpg");
		MBFImage colorImage = ImageUtilities.readMBF(new File("china-plates/test_001.jpg"));
		FImage grayImage = colorImage.flatten();
		EqualisationProcessor equalisation = new EqualisationProcessor();
		equalisation.processImage(grayImage);
		OtsuThreshold threshold = new OtsuThreshold();
		//				AdaptiveLocalThresholdGaussian threshold = new AdaptiveLocalThresholdGaussian(2.0f, 0.1f); // test_100.jpg
		threshold.processImage(grayImage);
		DisplayUtilities.display(grayImage);
		List<Integer> hPeak = new ArrayList<>();
		for (int y = 0; y < grayImage.getHeight(); y++) {
			int counter = 0;
			for (int x = 0; x < grayImage.getWidth(); x++) {
				counter += grayImage.getPixelNative(x, y);
			}
			hPeak.add(counter);
		}
		for (int h : hPeak) {
			System.err.println(h);
		}
		//		BufferedImage bImage = ImageUtilities.createBufferedImage(grayImage);
		//		String spz = PlateRecognizer.recognizeResult(bImage);
		//		System.out.println(spz);
	}

}
