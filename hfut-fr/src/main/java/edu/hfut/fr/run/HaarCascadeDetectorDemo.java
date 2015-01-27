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

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;

public class HaarCascadeDetectorDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_test/multifaces.jpg")); // mayun/multifaces
		//		DisplayUtilities.display(colorImage);

		FaceDetector<DetectedFace, FImage> faceDetector = new HaarCascadeDetector(20);
		List<DetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
		for (DetectedFace face : faces) {
			colorImage.drawShape(face.getBounds(), RGBColour.RED);
		}
		DisplayUtilities.display(colorImage);
	}

}
