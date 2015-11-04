package edu.hfut.fr.driver.run.impl;

import java.util.ArrayList;
import java.util.List;

public abstract class FeatureExtraction {

	List<Matrix> trainingSet;
	List<String> labels;
	int numOfComponents;
	Matrix meanMatrix;

	Matrix W;
	ArrayList<TrainingMatrix> projectedTrainingSet;

	public abstract Matrix getW();

	public abstract List<TrainingMatrix> getProjectedTrainingSet();

	public abstract Matrix getMeanMatrix();

}
