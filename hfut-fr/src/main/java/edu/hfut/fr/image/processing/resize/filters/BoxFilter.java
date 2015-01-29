package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 箱式滤波器
 *
 *@author jimbo
 */
public class BoxFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new BoxFilter();

	@Override
	public double getSupport() {
		return 0.5;
	}

	@Override
	public double filter(double t) {
		if ((t >= -0.5) && (t < 0.5))
			return (1.0);
		return (0.0);
	}

}
