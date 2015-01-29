package edu.hfut.fr.image.processing.convolution.filterbank;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

import edu.hfut.fr.image.processing.convolution.FConvolution;
import edu.hfut.fr.image.processing.convolution.Gaussian2D;
import edu.hfut.fr.image.processing.convolution.LaplacianOfGaussian2D;

/**
 * LeungMalik过滤器组实现
 *
 * @author wanghao
 */
public class LeungMalikFilterBank extends FilterBank {

	/**
	 * 构造函数
	 */
	public LeungMalikFilterBank() {
		this(49);
	}

	public LeungMalikFilterBank(int size) {
		super(makeFilters(size));
	}

	protected static FConvolution[] makeFilters(int size) {
		final int nScales = 3;
		final int nOrientations = 6;

		final int NROTINV = 12;
		final int NBAR = nScales * nOrientations;
		final int NEDGE = nScales * nOrientations;
		final int NF = NBAR + NEDGE + NROTINV;

		final FConvolution F[] = new FConvolution[NF];

		int count = 0;
		for (int i = 1; i <= nScales; i++) {
			final float scale = (float) pow(sqrt(2), i);

			for (int orient = 0; orient < nOrientations; orient++) {
				final float angle = (float) (PI * orient / nOrientations);

				F[count] = new FConvolution(makeFilter(scale, 0, 1, angle, size));
				F[count + NEDGE] = new FConvolution(makeFilter(scale, 0, 2, angle, size));
				count++;
			}
		}

		count = NBAR + NEDGE;
		for (int i = 1; i <= 4; i++) {
			final float scale = (float) pow(sqrt(2), i);

			F[count] = new FConvolution(normalise(Gaussian2D.createKernelImage(size, scale)));
			F[count + 1] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, scale)));
			F[count + 2] = new FConvolution(normalise(LaplacianOfGaussian2D.createKernelImage(size, 3 * scale)));
			count += 3;
		}

		return F;
	}

	protected static FImage makeFilter(float scale, int phasex, int phasey, float angle, int size) {
		final int hs = (size - 1) / 2;

		final FImage filter = new FImage(size, size);
		for (int y = -hs, j = 0; y < hs; y++, j++) {
			for (int x = -hs, i = 0; x < hs; x++, i++) {
				final float cos = (float) cos(angle);
				final float sin = (float) sin(angle);

				final float rx = cos * x - sin * y;
				final float ry = sin * x + cos * y;

				final float gx = gaussian1D(3 * scale, 0, rx, phasex);
				final float gy = gaussian1D(scale, 0, ry, phasey);

				filter.pixels[j][i] = gx * gy;
			}
		}
		return normalise(filter);
	}

	protected static float gaussian1D(float sigma, float mean, float x, int order) {
		x = x - mean;
		final float num = x * x;

		final float variance = sigma * sigma;
		final float denom = 2 * variance;
		final float g = (float) (exp(-num / denom) / pow(PI * denom, 0.5));

		switch (order) {
		case 0:
			return g;
		case 1:
			return -g * (x / variance);
		case 2:
			return g * ((num - variance) / (variance * variance));
		default:
			throw new IllegalArgumentException("order must be 0, 1 or 2.");
		}
	}

	protected static FImage normalise(FImage f) {
		final float mean = FloatArrayStatsUtils.mean(f.pixels);
		f.subtractInplace(mean);
		final float sumabs = FloatArrayStatsUtils.sumAbs(f.pixels);
		return f.divideInplace(sumabs);
	}

}
