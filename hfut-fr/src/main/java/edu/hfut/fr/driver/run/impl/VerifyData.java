package edu.hfut.fr.driver.run.impl;

import java.util.List;

public class VerifyData {

	private List<Matrix> matrixs;
	private List<String> labels;

	public VerifyData() {
		super();
	}

	public VerifyData(List<Matrix> matrixs, List<String> labels) {
		super();
		this.matrixs = matrixs;
		this.labels = labels;
	}

	public List<Matrix> getMatrixs() {
		return matrixs;
	}

	public void setMatrixs(List<Matrix> matrixs) {
		this.matrixs = matrixs;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

}
