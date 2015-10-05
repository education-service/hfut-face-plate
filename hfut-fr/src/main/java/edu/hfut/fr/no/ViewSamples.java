package edu.hfut.fr.no;

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
		MBFImage colorImage = ImageUtilities.readMBF(new File("Face_DB/s1/1.png"));
		DisplayUtilities.display(colorImage);
	}

}
