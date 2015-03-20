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

/**
 * 用于人脸检测测试
 *
 * @author wanggang
 *
 */
public class DetectorDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("Face_Test/yl2.bmp"));
		FKEFaceDetector faceDetector = new FKEFaceDetector(20);
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
		KEDetectedFace maxFace = null;
		int max = Integer.MIN_VALUE;
		// 找出检测中的最大图像块即为人脸
		for (KEDetectedFace face : faces) {
			if (face.getFacePatch().getHeight() > max) {
				max = face.getFacePatch().getHeight();
				maxFace = face;
			}
		}
		// 在原图上标出人脸部分
		if (maxFace == null) {
			System.err.println("检测不到人脸图片");
		}
		colorImage.drawShape(maxFace.getBounds(), RGBColour.RED);
		DisplayUtilities.display(colorImage);
	}

}
