package edu.hfut.fr.driver.run.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

public class FDMultiCore {

	/**
	 * 测试朱函数
	 */
	public static void main(String[] args) throws IOException {
		detect("multi-faces/test3.jpg");
	}

	public static void detect(String fileDir) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File(fileDir));
		FKEFaceDetector faceDetector = new FKEFaceDetector(20);
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
		for (KEDetectedFace face : faces) {
			colorImage.drawShape(face.getBounds(), RGBColour.RED);
		}
		DisplayUtilities.display(colorImage);
	}

}
