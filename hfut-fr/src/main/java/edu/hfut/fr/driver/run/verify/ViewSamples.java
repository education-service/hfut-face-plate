package edu.hfut.fr.driver.run.verify;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * 用于查看标准样本库
 *
 * @author wanggang
 *
 */
public class ViewSamples {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_db/s1/3.pgm"));
		DisplayUtilities.display(colorImage);
	}

}
