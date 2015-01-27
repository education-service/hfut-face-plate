package edu.hfut.lpr.run;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ChinesePlate {

	public static void main(String[] args) throws IOException {

		BufferedImage colorImage = ImageIO.read(new File("china-plates/test_002.jpg"));
		String spz = PlateRecognizer.recognizeResult(colorImage);
		System.out.println(spz);
		//		MBFImage colorImage = ImageUtilities.readMBF(new File("china-plates/test_001.jpg"));
		//		DisplayUtilities.display(colorImage);
		//		FImage grayImage = colorImage.flatten();
		//		DisplayUtilities.display(colorImage);
	}

}
