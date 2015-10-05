package edu.hfut.fr.driver.run.impl;

public class TrainingMatrix {

	Matrix matrix;
	String label;
	double distance = 0;

	public TrainingMatrix(Matrix m, String l) {
		this.matrix = m;
		this.label = l;
	}

}
