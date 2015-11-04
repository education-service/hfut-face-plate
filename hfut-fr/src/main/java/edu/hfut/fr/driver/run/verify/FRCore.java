package edu.hfut.fr.driver.run.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.driver.run.impl.CosineDissimilarity;
import edu.hfut.fr.driver.run.impl.EuclideanDistance;
import edu.hfut.fr.driver.run.impl.FeatureExtraction;
import edu.hfut.fr.driver.run.impl.FileManager;
import edu.hfut.fr.driver.run.impl.FileManagerHD;
import edu.hfut.fr.driver.run.impl.KNN;
import edu.hfut.fr.driver.run.impl.L1Distance;
import edu.hfut.fr.driver.run.impl.LDA;
import edu.hfut.fr.driver.run.impl.LPP;
import edu.hfut.fr.driver.run.impl.Matrix;
import edu.hfut.fr.driver.run.impl.Metric;
import edu.hfut.fr.driver.run.impl.PCA;
import edu.hfut.fr.driver.run.impl.TrainingMatrix;
import edu.hfut.fr.driver.run.impl.VerifyData;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 人脸识别主类：包括数据加载、特征提取、训练和识别
 *
 * @author wanggang
 *
 */
public class FRCore {

	// 距离计算函数：0: CosineDissimilarity, 1: L1Distance, 2: EuclideanDistance
	private int metricType;
	// 成分个数： PCA: components = samples * energyPercentage
	//         LDA: components = (c-1) *energyPercentage
	//         LLP: components = (c-1) *energyPercentage
	private int componentsRetained;
	// 特征提取模式：0: PCA, 1: LDA, 2: LPP
	private int featureExtractionMode;
	// KNN算法的K值
	private int knnK;
	// 训练集特征
	private FeatureExtraction fe = null;

	public FRCore(int metricType, int componentsRetained, int featureExtractionMode, int knnK) {
		super();
		this.metricType = metricType;
		this.componentsRetained = componentsRetained;
		this.featureExtractionMode = featureExtractionMode;
		this.knnK = knnK;
	}

	/**
	 * 测试函数
	 */
	public static void main(String[] args) {
		FRCore frCore = new FRCore(0, 100, 0, 3);
		// 训练数据
		long startTrain = System.currentTimeMillis();
		//		frCore.train("orlfaces", 40, 10);
		frCore.train("hdfaces", 28, 20);
		long endTrain = System.currentTimeMillis();
		System.err.println("样本训练时间为：" + (endTrain - startTrain) + " 毫秒");
		// 测试数据
		//		VerifyData testData = frCore.getTestData("orlfaces", 40, 10);
		VerifyData testData = frCore.getTestData("hdfaces", 28, 20);
		long startTest = System.currentTimeMillis();
		for (int i = 0; i < testData.getMatrixs().size(); i++) {
			String result = frCore.recognize(testData.getMatrixs().get(i));
			System.out.println("测试图片" + testData.getLabels().get(i) + "识别结果为：" + result);
		}
		long endTest = System.currentTimeMillis();
		System.err.println(testData.getMatrixs().size() + "张图片总测试时间为：" + (endTest - startTest) + " 毫秒");
	}

	public static void detect(String fileDir) throws IOException {
		MBFImage colorImage = ImageUtilities.readMBF(new File(fileDir));
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
		// 如果检测到则录入样本库
		//		AffineAligner faceAligner = new AffineAligner();
		//		if (maxFace != null) {
		//			FImage faceFA = faceAligner.align(maxFace);
		//			ImageUtilities.write(faceFA, "png", new File(""));
		//		}
	}

	public void train(String faceDir, int faceType, int faceCount) {
		// 训练数据
		VerifyData trainData = getTrainData(faceDir, faceType, faceCount);
		// 提取特征
		try {
			if (featureExtractionMode == 0)
				fe = new PCA(trainData.getMatrixs(), trainData.getLabels(), componentsRetained);
			else if (featureExtractionMode == 1)
				fe = new LDA(trainData.getMatrixs(), trainData.getLabels(), componentsRetained);
			else if (featureExtractionMode == 2)
				fe = new LPP(trainData.getMatrixs(), trainData.getLabels(), componentsRetained);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public String recognize(Matrix matrix) {
		// 距离度量标准
		Metric metric = null;
		if (metricType == 0)
			metric = new CosineDissimilarity();
		else if (metricType == 1)
			metric = new L1Distance();
		else if (metricType == 2)
			metric = new EuclideanDistance();

		assert metric != null : "metricType is wrong!";
		// 获取训练集特征集合
		List<TrainingMatrix> projectedTrainingSet = fe.getProjectedTrainingSet();
		Matrix testCase = fe.getW().transpose().times(matrix.minus(fe.getMeanMatrix()));
		String result = KNN.assignLabel(projectedTrainingSet.toArray(new TrainingMatrix[0]), testCase, knnK, metric);

		return result;
	}

	/**
	 * 训练集数据和标签
	 */
	public VerifyData getTrainData(String faceDir, int faceType, int faceCount) {
		List<Matrix> trainMatrix = new ArrayList<>();
		List<String> trainLabels = new ArrayList<>();
		for (int i = 1; i <= faceType; i++) {
			for (int j = 1; j < faceCount; j++) {
				try {
					Matrix temp = faceDir.equalsIgnoreCase("orlfaces") ? FileManager.convertPGMtoMatrix(faceDir + "/s"
							+ i + "/" + j + ".pgm") : FileManagerHD.convertPNGtoMatrix(faceDir + "/s" + i + "/" + j
							+ ".png");
					trainMatrix.add(vectorize(temp));
					trainLabels.add("s" + i);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return new VerifyData(trainMatrix, trainLabels);
	}

	/**
	 * 测试集数据和标签
	 */
	public VerifyData getTestData(String faceDir, int faceType, int faceCount) {
		List<Matrix> testMatrix = new ArrayList<>();
		List<String> testLabels = new ArrayList<>();
		for (int i = 1; i <= faceType; i++) {
			try {
				Matrix temp = faceDir.equalsIgnoreCase("orlfaces") ? FileManager.convertPGMtoMatrix(faceDir + "/s" + i
						+ "/" + faceCount + ".pgm") : FileManagerHD.convertPNGtoMatrix(faceDir + "/s" + i + "/"
						+ faceCount + ".png");
				testMatrix.add(vectorize(temp));
				testLabels.add("s" + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new VerifyData(testMatrix, testLabels);
	}

	private static Matrix vectorize(Matrix input) {
		int m = input.getRowDimension();
		int n = input.getColumnDimension();

		Matrix result = new Matrix(m * n, 1);
		for (int p = 0; p < n; p++) {
			for (int q = 0; q < m; q++) {
				result.set(p * m + q, 0, input.get(q, p));
			}
		}
		return result;
	}

}
