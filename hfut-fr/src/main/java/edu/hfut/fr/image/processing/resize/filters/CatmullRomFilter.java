package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * Catmull-Rom滤波器
 *
 * @author jimbo
 */
public class CatmullRomFilter implements ResizeFilterFunction {

	public static ResizeFilterFunction INSTANCE = new CatmullRomFilter();

	@Override
	public final double filter(double t) {
		if (t < 0) {
			t = -t;
		}
		if (t < 1.0) {
			return 0.5 * (2.0 + t * t * (-5.0 + t * 3.0));
		}
		if (t < 2.0) {
			return 0.5 * (4.0 + t * (-8.0 + t * (5.0 - t)));
		}
		return 0.0;
	}

	@Override
	public final double getSupport() {
		return 2.0;
	}

}
