package edu.hfut.mapred.images.run.fr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.image.FImage;

import edu.hfut.fr.driver.run.impl.CosineDissimilarity;
import edu.hfut.fr.driver.run.impl.EuclideanDistance;
import edu.hfut.fr.driver.run.impl.FeatureExtraction;
import edu.hfut.fr.driver.run.impl.KNN;
import edu.hfut.fr.driver.run.impl.L1Distance;
import edu.hfut.fr.driver.run.impl.LDA;
import edu.hfut.fr.driver.run.impl.LPP;
import edu.hfut.fr.driver.run.impl.Matrix;
import edu.hfut.fr.driver.run.impl.Metric;
import edu.hfut.fr.driver.run.impl.PCA;
import edu.hfut.fr.driver.run.impl.TrainingMatrix;
import edu.hfut.fr.driver.run.impl.VerifyData;

/**
 * 人脸识别主类：包括数据加载、特征提取、训练和识别
 *
 * @author wanggang
 *
 */
public class FrHdfsCore {

	private int metricType;
	private int componentsRetained;
	private int featureExtractionMode;
	private int knnK;
	private FeatureExtraction fe = null;

	// 人脸类别，固定洪都的28人
	final int faceType = 28;
	// 每类人脸数，默认洪都20张
	final int faceCount = 20;

	public FrHdfsCore(int metricType, int componentsRetained, int featureExtractionMode, int knnK) {
		super();
		this.metricType = metricType;
		this.componentsRetained = componentsRetained;
		this.featureExtractionMode = featureExtractionMode;
		this.knnK = knnK;
	}

	public void train(HashMap<String, List<FImage>> faceSamples) {
		// 训练数据
		VerifyData trainData = getTrainData(faceSamples);
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

	public String recognize(FImage image) {
		// FImage转换成Matrix
		Matrix matrix = convertFImagetoMatrix(image);
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
	public VerifyData getTrainData(HashMap<String, List<FImage>> faceSamples) {
		List<Matrix> trainMatrix = new ArrayList<>();
		List<String> trainLabels = new ArrayList<>();
		for (Entry<String, List<FImage>> childFold : faceSamples.entrySet()) {
			for (FImage imagec : childFold.getValue()) {
				Matrix temp = convertFImagetoMatrix(imagec);
				trainMatrix.add(vectorize(temp));
				trainLabels.add(childFold.getKey());
			}
		}

		return new VerifyData(trainMatrix, trainLabels);
	}

	public static Matrix vectorize(Matrix input) {
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

	private static Matrix convertFImagetoMatrix(FImage image) {
		int picWidth = image.getWidth();
		int picHeight = image.getHeight();
		// 提取特征后，以特征数据计算
		//		double[] feature = FImage2DoubleFV.INSTANCE.extractFeature(image).getVector();

		double[][] data2D = new double[picHeight][picWidth];
		for (int row = 0; row < picHeight; row++) {
			for (int col = 0; col < picWidth; col++) {
				//				data2D[row][col] = feature[row * 80 + col];
				data2D[row][col] = image.getPixel(row, col);
			}
		}

		return new Matrix(data2D);
	}

}
