package edu.hfut.fr.driver.run.impl;

public class CosineDissimilarity implements Metric {

	@Override
	public double getDistance(Matrix a, Matrix b) {
		assert a.getRowDimension() == b.getRowDimension();
		int size = a.getRowDimension();
		double cosine, sNorm, eNorm, se;
		int i;

		se = 0;
		for (i = 0; i < size; i++) {
			se += a.get(i, 0) * b.get(i, 0);
		}

		sNorm = 0;
		for (i = 0; i < size; i++) {
			sNorm += Math.pow(a.get(i, 0), 2);
		}
		sNorm = Math.sqrt(sNorm);

		eNorm = 0;
		for (i = 0; i < size; i++) {
			eNorm += Math.pow(b.get(i, 0), 2);
		}
		eNorm = Math.sqrt(eNorm);

		if (se < 0)
			se = 0 - se;

		cosine = se / (eNorm * sNorm);

		if (cosine == 0.0)
			return Double.MAX_VALUE;
		return 1 / cosine;
	}

}
