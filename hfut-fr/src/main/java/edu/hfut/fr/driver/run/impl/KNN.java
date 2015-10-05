package edu.hfut.fr.driver.run.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class KNN {

	public static String assignLabel(TrainingMatrix[] trainingSet, Matrix testFace, int K, Metric metric) {
		TrainingMatrix[] neighbors = findKNN(trainingSet, testFace, K, metric);
		return classify(neighbors);
	}

	static TrainingMatrix[] findKNN(TrainingMatrix[] trainingSet, Matrix testFace, int K, Metric metric) {
		int NumOfTrainingSet = trainingSet.length;
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";

		TrainingMatrix[] neighbors = new TrainingMatrix[K];
		int i;
		for (i = 0; i < K; i++) {
			trainingSet[i].distance = metric.getDistance(trainingSet[i].matrix, testFace);
			neighbors[i] = trainingSet[i];
		}

		for (i = K; i < NumOfTrainingSet; i++) {
			trainingSet[i].distance = metric.getDistance(trainingSet[i].matrix, testFace);

			int maxIndex = 0;
			for (int j = 0; j < K; j++) {
				if (neighbors[j].distance > neighbors[maxIndex].distance)
					maxIndex = j;
			}

			if (neighbors[maxIndex].distance > trainingSet[i].distance)
				neighbors[maxIndex] = trainingSet[i];
		}
		return neighbors;
	}

	static String classify(TrainingMatrix[] neighbors) {
		HashMap<String, Double> map = new HashMap<>();
		int num = neighbors.length;

		for (int index = 0; index < num; index++) {
			TrainingMatrix temp = neighbors[index];
			String key = temp.label;
			if (!map.containsKey(key))
				map.put(key, 1 / temp.distance);
			else {
				double value = map.get(key);
				value += 1 / temp.distance;
				map.put(key, value);
			}
		}

		double maxSimilarity = 0;
		String returnLabel = "";
		Set<String> labelSet = map.keySet();
		Iterator<String> it = labelSet.iterator();
		while (it.hasNext()) {
			String label = it.next();
			double value = map.get(label);
			if (value > maxSimilarity) {
				maxSimilarity = value;
				returnLabel = label;
			}
		}

		return returnLabel;
	}

}
