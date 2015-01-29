package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 三角滤波器
 *
 * @author jimbo
 */
public class TriangleFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new TriangleFilter();

	@Override
	public double getSupport() {
		return 1;
	}

	@Override
	public double filter(double t) {
		if (t < 0.0)
			t = -t;
		if (t < 1.0)
			return (1.0 - t);
		return (0.0);
	}

}
