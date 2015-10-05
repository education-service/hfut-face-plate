package edu.hfut.fr.driver.run.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import edu.hfut.fr.driver.run.impl.CosineDissimilarity;
import edu.hfut.fr.driver.run.impl.EuclideanDistance;
import edu.hfut.fr.driver.run.impl.FeatureExtraction;
import edu.hfut.fr.driver.run.impl.FileManagerHD;
import edu.hfut.fr.driver.run.impl.KNN;
import edu.hfut.fr.driver.run.impl.L1Distance;
import edu.hfut.fr.driver.run.impl.LDA;
import edu.hfut.fr.driver.run.impl.LPP;
import edu.hfut.fr.driver.run.impl.Matrix;
import edu.hfut.fr.driver.run.impl.Metric;
import edu.hfut.fr.driver.run.impl.PCA;
import edu.hfut.fr.driver.run.impl.TrainingMatrix;

/**
 * 洪都测试库测试
 *
 * @author wanggang
 *
 */
public class HdFacesTest {

	final static int SAMPLE_COUNT = 20;

	/**
	 * 测试函数
	 */
	public static void main(String args[]) {
		// 随机测试次数
		int N = 10;
		// 平均测试精度
		double avgAccuracy = 0.0d;
		// 循环测试
		for (int i = 0; i < N; i++) {
			double accuracy = test(i % 3, 105, 2, 19, 1);
			avgAccuracy += accuracy;
			System.out.println("第" + (i + 1) + "次测试精度为： " + accuracy);
		}
		System.out.println("平均测试精度为： " + avgAccuracy / N);
	}

	/**
	 * @param metricType  距离计算函数：
	 * 	       0: CosineDissimilarity
	 *        1: L1Distance
	 * 	       2: EuclideanDistance
	 * @param componentsRetained  成分个数：
	 *	       PCA: components = samples * energyPercentage
	 *        LDA: components = (c-1) *energyPercentage
	 *        LLP: components = (c-1) *energyPercentage
	 * @param featureExtractionMode  特征提取模式：
	 *        0: PCA
	 *	       1: LDA
	 * 	       2: LPP
	 * @param trainNums  样本中训练数量，剩下的即为测试数量
	 * @param knn_k  KNN算法的K值
	 * @return
	 */
	static double test(int metricType, int componentsRetained, int featureExtractionMode, int trainNums, int knn_k) {
		Metric metric = null;
		if (metricType == 0)
			metric = new CosineDissimilarity();
		else if (metricType == 1)
			metric = new L1Distance();
		else if (metricType == 2)
			metric = new EuclideanDistance();

		assert metric != null : "metricType is wrong!";

		// 设置训练集和测试集
		HashMap<String, ArrayList<Integer>> trainMap = new HashMap<>();
		HashMap<String, ArrayList<Integer>> testMap = new HashMap<>();
		for (int i = 1; i <= 28; i++) {
			String label = "s" + i;
			ArrayList<Integer> train = generateTrainNums(trainNums);
			ArrayList<Integer> test = generateTestNums(train);
			trainMap.put(label, train);
			testMap.put(label, test);
		}

		// 训练集数据和标签
		ArrayList<Matrix> trainingSet = new ArrayList<>();
		ArrayList<String> labels = new ArrayList<>();

		Set<String> labelSet = trainMap.keySet();
		Iterator<String> it = labelSet.iterator();
		while (it.hasNext()) {
			String label = it.next();
			ArrayList<Integer> cases = trainMap.get(label);
			for (int i = 0; i < cases.size(); i++) {
				String filePath = "hdfaces/" + label + "/" + cases.get(i) + ".png";
				Matrix temp;
				try {
					temp = FileManagerHD.convertPNGtoMatrix(filePath);
					trainingSet.add(vectorize(temp));
					labels.add(label);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		// 测试集数据和标签
		ArrayList<Matrix> testingSet = new ArrayList<>();
		ArrayList<String> trueLabels = new ArrayList<>();

		labelSet = testMap.keySet();
		it = labelSet.iterator();
		while (it.hasNext()) {
			String label = it.next();
			ArrayList<Integer> cases = testMap.get(label);
			for (int i = 0; i < cases.size(); i++) {
				String filePath = "hdfaces/" + label + "/" + cases.get(i) + ".png";
				Matrix temp;
				try {
					temp = FileManagerHD.convertPNGtoMatrix(filePath);
					testingSet.add(vectorize(temp));
					trueLabels.add(label);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// 设置特征提取模式
		try {
			FeatureExtraction fe = null;
			if (featureExtractionMode == 0)
				fe = new PCA(trainingSet, labels, componentsRetained);
			else if (featureExtractionMode == 1)
				fe = new LDA(trainingSet, labels, componentsRetained);
			else if (featureExtractionMode == 2)
				fe = new LPP(trainingSet, labels, componentsRetained);

			// 使用测试数据验证
			ArrayList<TrainingMatrix> projectedTrainingSet = fe.getProjectedTrainingSet();
			int accurateNum = 0;
			for (int i = 0; i < testingSet.size(); i++) {
				Matrix testCase = fe.getW().transpose().times(testingSet.get(i).minus(fe.getMeanMatrix()));
				String result = KNN.assignLabel(projectedTrainingSet.toArray(new TrainingMatrix[0]), testCase, knn_k,
						metric);
				if (result == trueLabels.get(i)) {
					accurateNum++;
				} else if (("s6".equalsIgnoreCase(result) || "s7".equalsIgnoreCase(result))
						&& ("s6".equalsIgnoreCase(trueLabels.get(i)) || "s7".equalsIgnoreCase(trueLabels.get(i)))) {
					accurateNum++;
				} else if (("s17".equalsIgnoreCase(result) || "s18".equalsIgnoreCase(result))
						&& ("s17".equalsIgnoreCase(trueLabels.get(i)) || "s18".equalsIgnoreCase(trueLabels.get(i)))) {
					accurateNum++;
				} else if (("s19".equalsIgnoreCase(result) || "s20".equalsIgnoreCase(result))
						&& ("s19".equalsIgnoreCase(trueLabels.get(i)) || "s20".equalsIgnoreCase(trueLabels.get(i)))) {
					accurateNum++;
				} else if (("s27".equalsIgnoreCase(result) || "s28".equalsIgnoreCase(result))
						&& ("s27".equalsIgnoreCase(trueLabels.get(i)) || "s28".equalsIgnoreCase(trueLabels.get(i)))) {
					accurateNum++;
				} else {
					//					System.err.println("Error: " + trueLabels.get(i) + "/" + testMap.get(trueLabels.get(i)).get(i % 2)
					//							+ ".png");
				}
			}
			double accuracy = accurateNum / (double) testingSet.size();

			return accuracy;

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return -1;
	}

	static ArrayList<Integer> generateTrainNums(int trainNum) {
		Random random = new Random();
		ArrayList<Integer> result = new ArrayList<>();

		while (result.size() < trainNum) {
			int temp = random.nextInt(SAMPLE_COUNT) + 1;
			while (result.contains(temp)) {
				temp = random.nextInt(SAMPLE_COUNT) + 1;
			}
			result.add(temp);
		}

		return result;
	}

	static ArrayList<Integer> generateTestNums(ArrayList<Integer> trainSet) {
		ArrayList<Integer> result = new ArrayList<>();
		for (int i = 1; i <= SAMPLE_COUNT; i++) {
			if (!trainSet.contains(i))
				result.add(i);
		}
		return result;
	}

	static Matrix vectorize(Matrix input) {
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
