package edu.hfut.fr.no;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.FImage2DoubleFV;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

public class VerifyRecognition {

	public static void main(String[] args) throws IOException {

		/******************** 加载训练集 ********************/
		System.out.println("加载训练集开始...");
		long t1 = System.currentTimeMillis();
		HashMap<String, List<double[]>> corpus = new HashMap<>();
		File[] DbFiles = new File("Face_DB").listFiles();
		File[] dbfiles = null;
		for (File fdb1 : DbFiles) {
			List<double[]> list = new ArrayList<>();
			dbfiles = fdb1.listFiles();
			for (File fdb2 : dbfiles) {
				list.add(FImage2DoubleFV.INSTANCE.extractFeature(ImageUtilities.readF(fdb2)).getVector());
			}
			corpus.put(fdb1.getName(), list);
		}
		long t2 = System.currentTimeMillis();
		System.out.println("加载训练集完成...");
		System.out.println("训练集加载时间为：" + (t2 - t1) + "ms");
		/******************* 识别率测试 ********************/
		System.out.println("开始测试...");
		long t3 = System.currentTimeMillis();
		String Face_TestUrl = "Face_Test/";
		File[] TestFiles = new File(Face_TestUrl).listFiles();
		FImage fimage = null;
		MBFImage mbimage;
		DoubleFV fv1;
		for (File f : TestFiles) {
			String rightname = "";
			mbimage = ImageUtilities.readMBF(new File(Face_TestUrl + f.getName()));
			FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
			List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(mbimage));
			AffineAligner faceAligner = new AffineAligner();
			// 找出检测中的最大图像块即为人脸
			int max = Integer.MIN_VALUE;
			KEDetectedFace maxFace = null;
			for (KEDetectedFace face : faces) {
				if (face.getFacePatch().getHeight() > max) {
					max = face.getFacePatch().getHeight();
					maxFace = face;
				}
			}
			if (maxFace == null) {
				System.out.println("the " + f.getName() + "*******->identity is null");
				continue;
			}
			fimage = faceAligner.align(maxFace);
			fv1 = FImage2DoubleFV.INSTANCE.extractFeature(fimage);
			double MinDistances = Double.MAX_VALUE;
			for (Entry<String, List<double[]>> tmp : corpus.entrySet()) {
				double distances = 0;
				for (double[] d : tmp.getValue()) {
					distances = distances + cos(fv1.getVector(), d, fv1.getVector().length);
				}
				// 求均值，注意原先是求累加和，这里求cos相似度均值的意义为：
				// 1、兼容原先的识别算法
				// 2、当样本库不均匀时，可以保证平均的cos相似度
				distances = distances / (tmp.getValue().size());
				if (distances < MinDistances) {
					MinDistances = distances;
					rightname = tmp.getKey();
				}
			}
			System.out.println("the " + f.getName() + "*******->identity is " + rightname + "*");
		}
		long t4 = System.currentTimeMillis();
		System.out.println("测试完成...");
		System.out.println("测试时间为：" + (t4 - t3) + "ms");
	}

	public static String recognizeFaceName(HashMap<String, List<double[]>> corpus, BufferedImage bi) {
		String result = "error";
		MBFImage image = ImageUtilities.createMBFImage(bi, Boolean.TRUE);
		FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
		List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(image));
		AffineAligner faceAligner = new AffineAligner();
		// 找出检测中的最大图像块即为人脸
		int max = Integer.MIN_VALUE;
		KEDetectedFace maxFace = null;
		for (KEDetectedFace face : faces) {
			if (face.getFacePatch().getHeight() > max) {
				max = face.getFacePatch().getHeight();
				maxFace = face;
			}
		}
		if (maxFace == null) {
			return "error";
		}
		FImage fimage = faceAligner.align(maxFace);
		DoubleFV fv1 = FImage2DoubleFV.INSTANCE.extractFeature(fimage);
		double MinDistances = Double.MAX_VALUE;
		for (Entry<String, List<double[]>> tmp : corpus.entrySet()) {
			double distances = 0;
			for (double[] d : tmp.getValue()) {
				distances = distances + cos(fv1.getVector(), d, fv1.getVector().length);
			}
			distances = distances / (tmp.getValue().size());
			if (distances < MinDistances) {
				MinDistances = distances;
				result = tmp.getKey();
			}
		}

		return result;
	}

	/**
	 * 使用平方差和衡量距离
	 */
	public static double distance(double[] v1, double[] v2, int dim) {
		double dis = 0.0;
		for (int i = 0; i < dim; i++) {
			dis += Math.pow(Math.abs(v1[i] - v2[i]), 2);
		}
		return Math.sqrt(dis);
	}

	/**
	 * 余弦负值，使用余弦定理求两个向量的相似度，值越大相似度越大，
	 * 为了兼容距离相似度，所以取负值，值越小相似度越大。
	 */
	public static double cos(double[] v1, double[] v2, int dim) {
		return (-1.0) * d(v1, v2, dim) / (q(v1) * q(v2));
	}

	/**
	 * 求两个向量的乘积
	 */
	private static double d(double[] v1, double[] v2, int dim) {
		double result = 0.0;
		for (int i = 0; i < dim; i++) {
			result += v1[i] * v2[i];
		}
		return result;
	}

	/**
	 * 求一组向量的平方根
	 */
	private static double q(double[] v) {
		double result = 0.0;
		for (double t : v) {
			result += t * t;
		}
		return Math.sqrt(result);
	}

	/**
	 * 求方差
	 */
	public static double var(double[] d) {
		double result = 0.0;
		double average = 0.0;
		for (double t : d) {
			average += t;
		}
		average = average / d.length;
		for (int i = 0; i < d.length; i++) {
			result += (d[i] - average) * (d[i] - average);
		}
		return Math.sqrt(result);
	}

}
