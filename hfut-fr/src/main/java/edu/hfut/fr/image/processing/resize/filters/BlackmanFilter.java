package edu.hfut.fr.image.processing.resize.filters;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 布莱克曼窗滤波器
 *
 *@author jimbo
 */
@Reference(author = { "R. B. Blackman", "J. W. Tukey" }, title = "Particular Pairs of Windows", type = ReferenceType.Inbook, year = "1959", booktitle = " In The Measurement of Power Spectra, From the Point of View of Communications Engineering", publisher = "Dover", pages = {
		"98", "99" })
public class BlackmanFilter implements ResizeFilterFunction {

	private static double blackman(final double t) {
		return 0.42 + 0.50 * Math.cos(Math.PI * t) + 0.08 * Math.cos(2.0 * Math.PI * t);
	}

	@Override
	public final double filter(final double t) {
		return blackman(t);
	}

	@Override
	public final double getSupport() {
		return 1.0;
	}

}
