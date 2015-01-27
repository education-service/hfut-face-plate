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

import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

public class FKEFaceDetectorDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_test/multifaces.jpg")); // mayun/multifaces
		//		DisplayUtilities.display(colorImage);

		// FKEFaceDetector/AffineAligner/MeshWarpAligner/RotateScaleAligner
		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
		for (KEDetectedFace face : faces) {
			colorImage.drawShape(face.getBounds(), RGBColour.RED);
		}
		DisplayUtilities.display(colorImage);
	}

}
