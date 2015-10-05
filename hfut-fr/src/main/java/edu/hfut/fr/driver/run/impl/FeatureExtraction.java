package edu.hfut.fr.driver.run.impl;

import java.util.ArrayList;

public abstract class FeatureExtraction {

	ArrayList<Matrix> trainingSet;
	ArrayList<String> labels;
	int numOfComponents;
	Matrix meanMatrix;

	Matrix W;
	ArrayList<TrainingMatrix> projectedTrainingSet;

	public abstract Matrix getW();

	public abstract ArrayList<TrainingMatrix> getProjectedTrainingSet();

	public abstract Matrix getMeanMatrix();

}
