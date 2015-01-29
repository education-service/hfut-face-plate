package edu.hfut.fr.driver.run;

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

/**
 * 人脸识别整体过程
 *
 * @author wanghao
 */
public class FaceRecognition {

	@SuppressWarnings("unused")
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
				//System.out.println(f);
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
		FImage faceImage1 = ImageUtilities.readF(new File("faces_db/s1/2.pgm"));
		//		FImage faceImage2 = ImageUtilities.readF(new File("faces_db/s1/3.pgm"));
		///**
		//  另两种方法
		//		System.out.println(recognize(faceImage1, faceImage2));
		//		String filepath = "faces_db/s1/";
		//		String name = recognize(faceImage1, filepath);
		//		System.out.println(name);
		//		String result = recognizeDataset(faceImage1, faceImage2);
		//		System.out.println("the maxIndex and minIndex result is --->" + result);
		System.out.println(FaceRecognition.recognizeDataset("faces_db", faceImage1));
	}

	/**
	 * 给定两幅图像,给定判断得分值
	 */
	public static double recognize(FImage image1, FImage image2) throws IOException {
		FeatureExractor faceExractor = new FeatureExractor(image1, image2);
		double score = faceExractor.compareTwoImage();
		return score;
	}

	/**
	 * 给定一幅图像和一个类库,判断图像属于哪一个分类,进行面部识别
	 */
	public static String recognize(FImage image, String file) throws IOException {
		FeatureExractor faceExractor = new FeatureExractor(image, file);
		String minName = faceExractor.compareImageAndFileImage();
		/*
		 *  得到分数值,找到最接近的匹配,给出最近的图像文件
		 */
		return minName;
	}

	/**
	 * 通过给定的图像和多个库,人脸识别和人脸分类,分类到具体的类库
	 * @param return 返回对应的类别
	 */
	public static String recognizeDataset(String faceDb, FImage faceImage) throws IOException {
		return FeatureExractor.compareImageAndFolderName(faceDb, faceImage);
	}

	public static String recognizeDataset(FImage image1, FImage image2) throws IOException {
		FeatureExractor faceExractor = new FeatureExractor(image1, image2);
		String result = faceExractor.compareImageAndFileList();
		return result;
	}

}
