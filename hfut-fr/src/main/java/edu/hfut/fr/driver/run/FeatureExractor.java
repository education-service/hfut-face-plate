package edu.hfut.fr.driver.run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;

/**
 * 人脸识别/人脸分类分类;给定人脸分类的库,和相似度得分
 *
 * @author wanghao
 */
public class FeatureExractor {

	/**
	 * 构造函数
	 */
	public FeatureExractor(FImage image1, FImage image2) {
		this.image1 = image1;
		this.image2 = image2;
	}

	public FeatureExractor(FImage image1, String file) {
		this.image1 = image1;
		this.filename = file;
	}

	public FeatureExractor(FImage image1, List<String> filelist) {
		this.image1 = image1;
		this.filelist = filelist;
	}

	public double compareTwoImage() throws IOException {
		DoubleFV faceImage1 = FImage2DoubleFV.INSTANCE.extractFeature(this.image1);
		DoubleFV faceImage2 = FImage2DoubleFV.INSTANCE.extractFeature(this.image2);
		double score = 0.0;
		score = distance(faceImage1.getVector(), faceImage2.getVector(), faceImage1.getVector().length);
		return score;
	}

	/**
	 * 比较一张图片和一个文件夹中所有的图片
	 */
	public String compareImageAndFileImage() throws IOException {

		File tmp = new File(this.filename);
		File[] tmpList = tmp.listFiles();
		/*
		 * 对照文件中所有的图片
		 */
		DoubleFV facefeature1 = FImage2DoubleFV.INSTANCE.extractFeature(this.image1);
		double[] distances = new double[tmpList.length];
		double minDistance = 10000;
		int minIndex = 0;
		for (int i = 0; i < tmpList.length; i++) {
			FImage faceImage2 = ImageUtilities.readF(tmpList[i]);
			DoubleFV facefeature2 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage2);
			distances[i] = distance(facefeature1.getVector(), facefeature2.getVector(), facefeature1.length());
			if (distances[i] < minDistance) {
				minDistance = distances[i];
				minIndex = i;
			}
		}
		String minFileName = tmpList[minIndex].getName();
		System.out
				.println("The min distance is --->" + minDistance + "-----------the file name is =--->" + minFileName);
		// 显示最匹配的图片
		//		DisplayUtilities.display(ImageUtilities.readMBF(new File(minFileName)));
		return minFileName;
	}

	/**
	 * 比较一张图片和文件夹列表中的图片,找到最佳的匹配的文件夹
	 */
	public String compareImageAndFileList() throws IOException {

		FImage faceImage1 = ImageUtilities.readF(new File("faces_db/s1/2.pgm"));
		DoubleFV feature1 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage1);

		double[] distances = new double[40];
		for (int s = 1; s < 41; s++) {
			for (int ii = 1; ii < 11; ii++) {
				FImage faceImage2 = ImageUtilities.readF(new File("faces_db/s" + s + "/" + ii + ".pgm"));
				DoubleFV feature2 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage2);
				distances[s - 1] += distance(feature1.getVector(), feature2.getVector(), feature2.getVector().length);
			}
		}

		double maxDistance = -1.0, minDistance = 10000.0;
		int cursor = 0, maxIndex = 0, minIndex = 0;
		for (double distance : distances) {
			cursor++;
			if (distance > maxDistance) {
				maxDistance = distance;
				maxIndex = cursor;
			}
			if (distance < minDistance) {
				minDistance = distance;
				minIndex = cursor;
			}
			//	System.err.println(distance);
		}

		//	System.err.println("Max Distance Num-------->" + maxIndex);
		//	System.err.println("Min Distance Num-------->" + minIndex);

		//		DoubleFV facefeature1 = FImage2DoubleFV.INSTANCE.extractFeature(this.image1);
		//		double[] distances = new double[this.filelist.size()];
		//
		//		for (int i = 0; i < this.filelist.size(); i++) {
		//			File imageFile = new File(filelist.get(i));
		//			File[] imageList = imageFile.listFiles();
		//			for (int j = 0; j < imageList.length; j++) {
		//				FImage faceImage2 = ImageUtilities.readF(imageList[j]);
		//				DoubleFV facefeature2 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage2);
		//				distances[i] += distance(facefeature1.getVector(), facefeature2.getVector(), facefeature1.length());
		//			}
		//		}
		//
		//		double maxDistance = -1.0, minDistance = 10000.0;
		//		int cursor = 0, maxIndex = 0, minIndex = 0;
		//		for (double distance : distances) {
		//			cursor++;
		//			if (distance > maxDistance) {
		//				maxDistance = distance;
		//				maxIndex = cursor;
		//			}
		//			if (distance < minDistance) {
		//				minDistance = distance;
		//				minIndex = cursor;
		//			}
		//			System.err.println(distance);
		//		}

		return maxIndex + "," + minIndex;
	}

	/*
	public static void main(String[] args) throws IOException {

		FImage faceImage1 = ImageUtilities.readF(new File("faces_db/s1/2.pgm"));
		DoubleFV feature1 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage1);

		double[] distances = new double[40];
		for (int s = 1; s < 41; s++) {
			for (int ii = 1; ii < 11; ii++) {
				FImage faceImage2 = ImageUtilities.readF(new File("faces_db/s" + s + "/" + ii + ".pgm"));
				DoubleFV feature2 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage2);
				distances[s - 1] += distance(feature1.getVector(), feature2.getVector(), feature2.getVector().length);
			}
		}

		double maxDistance = -1.0, minDistance = 10000.0;
		int cursor = 0, maxIndex = 0, minIndex = 0;
		for (double distance : distances) {
			cursor++;
			if (distance > maxDistance) {
				maxDistance = distance;
				maxIndex = cursor;
			}
			if (distance < minDistance) {
				minDistance = distance;
				minIndex = cursor;
			}
			System.err.println(distance);
		}

		System.ershir.println("Max Distance Num-------->" + maxIndex);
		System.err.println("Min Distance Num-------->" + minIndex);
	}
	*/

	/**
	 * 使用平方差和衡量距离
	 */
	public static double distance(double[] v1, double[] v2, int dim) {
		float dis = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += Math.pow(Math.abs(v1[i] - v2[i]), 2);
		}
		return Math.sqrt(dis);
	}

	/**
	 * 使用余弦计算距离
	 */
	public static double distance1(float[] v1, float[] v2, int dim) {
		float dis = 0.0f;
		float v1norm = 0.0f;
		float v2norm = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += v1[i] * v2[i];
			v1norm += Math.sqrt(Math.pow(v1[i], 2));
			v2norm += Math.sqrt(Math.pow(v2[i], 2));
		}
		return dis / (v1norm * v2norm);
	}

	public FImage getImage1() {
		return image1;
	}

	public void setImage1(FImage image1) {
		this.image1 = image1;
	}

	public FImage getImage2() {
		return image2;
	}

	public void setImage2(FImage image2) {
		this.image2 = image2;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<String> getFilelist() {
		return filelist;
	}

	public void setFilelist(List<String> filelist) {
		this.filelist = filelist;
	}

	private FImage image1 = null;
	private FImage image2 = null;
	private String filename = null;
	private List<String> filelist = null;

}
