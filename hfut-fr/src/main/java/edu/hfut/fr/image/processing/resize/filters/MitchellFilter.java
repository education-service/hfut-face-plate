package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * Mitchell 滤波器
 *
 *@author jimbo
 */
public class MitchellFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new MitchellFilter();

	@Override
	public double getSupport() {
		return 2;
	}

	@Override
	public double filter(double t) {
		double B, C;
		B = C = 1d / 3d;

		double tt;

		tt = t * t;
		if (t < 0)
			t = -t;
		if (t < 1.0) {
			t = (((12.0 - 9.0 * B - 6.0 * C) * (t * tt)) + ((-18.0 + 12.0 * B + 6.0 * C) * tt) + (6.0 - 2 * B));
			return (t / 6.0);
		} else if (t < 2.0) {
			t = (((-1.0 * B - 6.0 * C) * (t * tt)) + ((6.0 * B + 30.0 * C) * tt) + ((-12.0 * B - 48.0 * C) * t) + (8.0 * B + 24 * C));
			return (t / 6.0);
		}
		return (0.0);
	}

}
