package edu.hfut.fr.image.processing.resize.filters;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 点阵滤波器
 *
 *@author jimbo
 */
public class PointFilter extends BoxFilter {

	public static ResizeFilterFunction INSTANCE = new PointFilter();

	@Override
	public double getSupport() {
		return 0;
	}

}
