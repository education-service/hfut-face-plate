package edu.hfut.fr.no.demo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;

import edu.hfut.fr.image.processing.face.detection.CCDetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.SandeepFaceDetector;

/**
 * Sandeep人脸检测示例
 *
 * @author wanggang
 *
 */
@Deprecated
public class SandeepFaceDetectorDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("Face_Sample_Tackle/s1/1.bmp")); // mayun
		//		DisplayUtilities.display(colorImage);

		FaceDetector<CCDetectedFace, MBFImage> faceDetector = new SandeepFaceDetector(null); // 需要传入皮肤模型才行
		List<CCDetectedFace> faces = faceDetector.detectFaces(colorImage);
		for (CCDetectedFace face : faces) {
			colorImage.drawShape(face.getBounds(), RGBColour.RED);
		}
		DisplayUtilities.display(colorImage);
	}

}
