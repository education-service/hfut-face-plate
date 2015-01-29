package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * Lanczos3 滤波器
 *
 *@author jimbo
 */
public class Lanczos3Filter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new Lanczos3Filter();

	@Override
	public double getSupport() {
		return 3;
	}

	private double sinc(double x) {
		x *= Math.PI;
		if (x != 0)
			return (Math.sin(x) / x);
		return (1.0);
	}

	@Override
	public double filter(double t) {
		if (t < 0)
			t = -t;
		if (t < 3.0)
			return (sinc(t) * sinc(t / 3.0));
		return (0.0);
	}

}
