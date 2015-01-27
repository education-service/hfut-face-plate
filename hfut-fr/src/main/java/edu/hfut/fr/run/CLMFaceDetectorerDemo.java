package edu.hfut.fr.run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.image.processing.face.detection.CLMDetectedFace;
import edu.hfut.fr.image.processing.face.detection.CLMFaceDetector;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;

public class CLMFaceDetectorerDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_test/multifaces.jpg")); // mayun/multifaces
		//		DisplayUtilities.display(colorImage);

		// CLMFaceDetectorer/CLMAligner  矫正效果不好
		FaceDetector<CLMDetectedFace, FImage> faceDetector = new CLMFaceDetector();
		//		CLMAligner faceAligner = new CLMAligner(200);
		List<CLMDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensityNTSC(colorImage));
		for (CLMDetectedFace face : faces) {
			//			FImage faceFA = faceAligner.align(face);
			//			DisplayUtilities.display(faceFA);
			colorImage.drawShape(face.getBounds(), RGBColour.RED);
		}

		DisplayUtilities.display(colorImage);

	}

}
