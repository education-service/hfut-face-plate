package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * B-Spline 滤波器
 *
 *@author jimbo
 */
public class BSplineFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new BSplineFilter();

	@Override
	public double getSupport() {
		return 2;
	}

	@Override
	public double filter(double t) {
		double tt;

		if (t < 0)
			t = -t;
		if (t < 1) {
			tt = t * t;
			return ((.5 * tt * t) - tt + (2.0 / 3.0));
		} else if (t < 2) {
			t = 2 - t;
			return ((1.0 / 6.0) * (t * t * t));
		}
		return (0.0);
	}

}
