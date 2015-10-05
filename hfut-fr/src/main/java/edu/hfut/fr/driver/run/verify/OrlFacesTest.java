package edu.hfut.fr.driver.run.verify;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import edu.hfut.fr.driver.run.impl.CosineDissimilarity;
import edu.hfut.fr.driver.run.impl.EuclideanDistance;
import edu.hfut.fr.driver.run.impl.FeatureExtraction;
import edu.hfut.fr.driver.run.impl.FileManager;
import edu.hfut.fr.driver.run.impl.KNN;
import edu.hfut.fr.driver.run.impl.L1Distance;
import edu.hfut.fr.driver.run.impl.LDA;
import edu.hfut.fr.driver.run.impl.LPP;
import edu.hfut.fr.driver.run.impl.Matrix;
import edu.hfut.fr.driver.run.impl.Metric;
import edu.hfut.fr.driver.run.impl.PCA;
import edu.hfut.fr.driver.run.impl.TrainingMatrix;

/**
 * ORl标准人脸库测试
 *
 * @author wanggang
 *
 */
public class OrlFacesTest {

	/**
	 * 测试函数
	 */
	public static void main(String[] args) {
		// PCA
		testOrl(0, 100, 0, 9, 3);
		// LDA
		//		testOrl(2, 60, 1, 9, 3);
		// LPP
		//		testOrl(2, 100, 2, 9, 3);
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
	static double testOrl(int metricType, int componentsRetained, int featureExtractionMode, int trainNums, int knn_k) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Metric metric = null;
		if (metricType == 0)
			metric = new CosineDissimilarity();
		else if (metricType == 1)
			metric = new L1Distance();
		else if (metricType == 2)
			metric = new EuclideanDistance();

		assert metric != null : "metricType is wrong!";

		Calendar cal1 = Calendar.getInstance();
		System.out.println("Start set Training Set :" + dateFormat.format(cal1.getTime()));
		// 设置训练集和测试集
		HashMap<String, ArrayList<Integer>> trainMap = new HashMap<>();
		HashMap<String, ArrayList<Integer>> testMap = new HashMap<>();
		for (int i = 1; i <= 40; i++) {
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
				String filePath = "orlfaces/" + label + "/" + cases.get(i) + ".pgm";
				Matrix temp;
				try {
					temp = FileManager.convertPGMtoMatrix(filePath);
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
				String filePath = "orlfaces/" + label + "/" + cases.get(i) + ".pgm";
				Matrix temp;
				try {
					temp = FileManager.convertPGMtoMatrix(filePath);
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

				if (result == trueLabels.get(i))
					accurateNum++;
			}
			double accuracy = accurateNum / (double) testingSet.size();
			System.out.println("测试精度为： " + accuracy);
			Calendar cal2 = Calendar.getInstance();
			System.out.println("End :" + dateFormat.format(cal2.getTime()));
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
			int temp = random.nextInt(10) + 1;
			while (result.contains(temp)) {
				temp = random.nextInt(10) + 1;
			}
			result.add(temp);
		}

		return result;
	}

	static ArrayList<Integer> generateTestNums(ArrayList<Integer> trainSet) {
		ArrayList<Integer> result = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
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
