package edu.hfut.fr.image.processing.resize.filters;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.processing.resize.ResizeFilterFunction;

/**
 * 汉明窗滤波器
 *
 *@author jimbo
 */
@Reference(author = { "R. B. Blackman", "J. W. Tukey" }, title = "Particular Pairs of Windows", type = ReferenceType.Inbook, year = "1959", booktitle = " In The Measurement of Power Spectra, From the Point of View of Communications Engineering", publisher = "Dover", pages = {
		"98", "99" })
public class HanningFilter implements ResizeFilterFunction {

	@Override
	public final double filter(final double t) {
		return 0.5 + 0.5 * Math.cos(Math.PI * t);
	}

	@Override
	public final double getSupport() {
		return 1.0;
	}

}
