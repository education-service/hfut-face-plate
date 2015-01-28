package edu.hfut.lpr.run;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openimaj.image.DisplayUtilities;

public class ImageCopy {

	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File("china-plates/test_001.jpg"));
		DisplayUtilities.display(image);
		BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageCopy.setData(image.getData());
		DisplayUtilities.display(image);
	}

}
