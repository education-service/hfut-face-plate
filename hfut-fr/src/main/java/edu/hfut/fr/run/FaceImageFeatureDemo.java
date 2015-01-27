package edu.hfut.fr.run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;
import edu.hfut.fr.image.processing.face.feature.FaceImageFeature;
import edu.hfut.fr.image.processing.face.feature.FaceImageFeature.Extractor;

public class FaceImageFeatureDemo {

	public static void main(String[] args) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_test/mayun.jpg")); // mayun/multifaces
		//		DisplayUtilities.display(colorImage);

		// FKEFaceDetector/AffineAligner/MeshWarpAligner/RotateScaleAligner
		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		AffineAligner faceAligner = new AffineAligner(); // 矫正效果很好
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
		Extractor<KEDetectedFace> extractor = new FaceImageFeature.Extractor<KEDetectedFace>(faceAligner);
		for (KEDetectedFace face : faces) {
			FaceImageFeature faceImageFeature = extractor.extractFeature(face);
			FloatFV feature = faceImageFeature.getFeatureVector();
			float[] fv = feature.getVector();
			for (float f : fv) {
				System.out.println(f);
			}
		}
	}

}
