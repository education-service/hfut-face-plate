package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 基础滤波器
 *
 *@author jimbo
 */
public class HermiteFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new HermiteFilter();

	@Override
	public double getSupport() {
		return 1;
	}

	@Override
	public double filter(double t) {
		if (t < 0.0)
			t = -t;
		if (t < 1.0)
			return (2.0 * t - 3.0) * t * t + 1.0;
		return (0.0);
	}

}
