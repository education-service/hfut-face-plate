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

public class FaceRecognition {

	public static void main(String[] args) throws IOException {

		MBFImage colorImage = ImageUtilities.readMBF(new File("faces_test/mayun.jpg"));

		/**********************************一、人脸检测、矫正、特征提取***************************************/

		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		AffineAligner faceAligner = new AffineAligner();
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

		/**********************************二、人脸数据库加载、识别***************************************/
		// 加载测试人脸数据集
		//		ATandTDataset faceDataset = new ATandTDataset(new File("att_faces"));
		// GroupedKFold  GroupedLeaveOneOut   KFold  LeaveOneOut   StratifiedGroupedKFold
		//		GroupedKFold<Integer, FImage> crossValidator = new GroupedKFold<Integer, FImage>(40);
		//		CrossValidationBenchmark benchmark = new CrossValidationBenchmark(crossValidator, faceDataset, faceDetector,
		//				null);
		//		benchmark.setup();
		//		benchmark.perform();
	}

	public static String recognize(FImage image) {
		return "";
	}

}
