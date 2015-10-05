package edu.hfut.fr.no.demo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.DisplayUtilities;
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
 * 提取特征向量
 *
 * @author wanghao
 */
public class FaceImageFeatureExtractor implements Display {

	/**
	 * 默认构造函数
	 */
	@SuppressWarnings("unused")
	public FaceImageFeatureExtractor() {

		try {
			this.images = ImageUtilities.readMBF(new File("faces_test/mayun.jpg"));
			DisplayUtilities.display(this.images);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		AffineAligner faceAligner = new AffineAligner(); // 采用最好的矫正方法
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.images));
		// 创建特征类解释器
		Extractor<KEDetectedFace> extractor = new FaceImageFeature.Extractor<KEDetectedFace>(faceAligner);
		for (KEDetectedFace face : faces) {
			// 通过解释器抽取图像特征
			FaceImageFeature faceImageFeature = extractor.extractFeature(face);
			FloatFV feature = faceImageFeature.getFeatureVector();
			// 获取特征向量
			float[] fv = feature.getVector();
			for (float f : fv) {
				//System.out.println(f);
			}
		}

	}

	/**
	 * 指定文件
	 */
	@SuppressWarnings("unused")
	public FaceImageFeatureExtractor(String file) {
		try {
			this.images = ImageUtilities.readMBF(new File(file));
			DisplayUtilities.display(this.images);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		AffineAligner faceAligner = new AffineAligner(); // 采用最好的矫正方法
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.images));
		// 创建特征类解释器
		Extractor<KEDetectedFace> extractor = new FaceImageFeature.Extractor<KEDetectedFace>(faceAligner);
		for (KEDetectedFace face : faces) {
			// 通过解释器抽取图像特征
			FaceImageFeature faceImageFeature = extractor.extractFeature(face);
			FloatFV feature = faceImageFeature.getFeatureVector();
			// 获取特征向量
			float[] fv = feature.getVector();
			for (float f : fv) {
				//	System.out.println(f);
			}
		}

	}

	/**
	 * 返回特征向量
	 */
	public float[] getFeatureVector() {
		return this.fv.getVector();
	}

	@Override
	public void displayMBF(MBFImage image) {
		DisplayUtilities.display(image);
	}

	@Override
	public void displayF(FImage image) {
		DisplayUtilities.display(image);
	}

	// 特征向量的GET/SET方法
	public FloatFV getFv() {
		return fv;
	}

	public void setFv(FloatFV fv) {
		this.fv = fv;
	}

	public MBFImage getImages() {
		return images;
	}

	public void setImages(MBFImage images) {
		this.images = images;
	}

	// 特征向量
	private FloatFV fv = null;
	private MBFImage images = null;

}
