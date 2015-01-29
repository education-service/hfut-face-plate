package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * bell 滤波器
 *
 *@author jimbo
 */
public class BellFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new BellFilter();

	@Override
	public double getSupport() {
		return 1.5;
	}

	@Override
	public double filter(double t) {
		if (t < 0)
			t = -t;
		if (t < .5)
			return (.75 - (t * t));
		if (t < 1.5) {
			t = (t - 1.5);
			return (.5 * (t * t));
		}
		return (0.0);
	}

}
