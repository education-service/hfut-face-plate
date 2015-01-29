package edu.hfut.fr.image.processing.convolution.filterbank;

import static java.lang.Math.PI;
import edu.hfut.fr.image.processing.convolution.FConvolution;
import edu.hfut.fr.image.processing.convolution.Gaussian2D;
import edu.hfut.fr.image.processing.convolution.LaplacianOfGaussian2D;

/**
 * 滤波器组设置
 *
 * @author wanghao
 */

public class RootFilterSetFilterBank extends FilterBank {

	protected final static float[] SCALES = { 1, 2, 4 };
	protected final static int NUM_ORIENTATIONS = 6;

	/**
	 * 默认构造
	 */
	public RootFilterSetFilterBank() {
		this(49);
	}

	public RootFilterSetFilterBank(int size) {
		super(makeFilters(size));
	}

	protected static FConvolution[] makeFilters(int size) {
		final int numRotInvariants = 2;
		final int numBar = SCALES.length * NUM_ORIENTATIONS;
		final int numEdge = SCALES.length * NUM_ORIENTATIONS;
		final int numFilters = numBar + numEdge + numRotInvariants;
		final FConvolution[] F = new FConvolution[numFilters];

		int count = 0;
		for (final float scale : SCALES) {
			for (int orient = 0; orient < NUM_ORIENTATIONS; orient++) {
				final float angle = (float) (PI * orient / NUM_ORIENTATIONS);

				F[count] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 1, angle, size));
				F[count + numEdge] = new FConvolution(LeungMalikFilterBank.makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}

		F[numBar + numEdge] = new FConvolution(Gaussian2D.createKernelImage(size, 10));
		F[numBar + numEdge + 1] = new FConvolution(LeungMalikFilterBank.normalise(LaplacianOfGaussian2D
				.createKernelImage(size, 10)));

		return F;
	}

}
