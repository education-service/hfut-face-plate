package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * 差值直方图
 *
 * @author  wanggang
 */
public class InterpolatedBinnedWindowedExtractor extends BinnedWindowedExtractor {

	/**
	 * 权重
	 */
	float[][] weights;

	/**
	 * 直方图是否循环
	 */
	boolean wrap = false;

	/**
	 *非循环直方图的构造.
	 *
	 * @param nbins
	 *            number of bins
	 */
	public InterpolatedBinnedWindowedExtractor(int nbins) {
		super(nbins);
	}

	/**
	 * 插值直方图的构建
	 */
	public InterpolatedBinnedWindowedExtractor(int nbins, boolean wrap) {
		super(nbins);
		this.wrap = true;
	}

	public InterpolatedBinnedWindowedExtractor(int nbins, float min, float max) {
		super(nbins, min, max);
	}

	public InterpolatedBinnedWindowedExtractor(int nbins, float min, float max, boolean wrap) {
		super(nbins, min, max);
		this.wrap = wrap;
	}

	/**
	 * 计算图片的位图
	 */
	@Override
	public void analyseImage(FImage image) {
		final int height = image.height;
		final int width = image.width;

		binMap = new int[height][width];
		weights = new float[height][width];

		if (wrap) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final float val = ((image.pixels[y][x] - min) / (max - min)) * nbins;
					final int bin = (int) Math.floor(val);
					final float delta = val - bin;

					final int lbin = bin % nbins;
					final float lweight = 1f - delta;

					binMap[y][x] = lbin;
					weights[y][x] = lweight;
				}
			}
		} else {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					final float val = ((image.pixels[y][x] - min) / (max - min)) * nbins;
					final int bin = (int) Math.floor(val);
					final float delta = val - bin;

					int lbin;
					float lweight;
					if (delta < 0.5) {
						lbin = bin - 1;
						lweight = 0.5f + (delta);
					} else {
						lbin = bin;
						lweight = 1.5f - (delta);
					}

					if (lbin < 0) {
						lbin = 0;
						lweight = 1;
					} else if (bin >= nbins) {
						lbin = nbins - 1;
						lweight = 1;
					}

					binMap[y][x] = lbin;
					weights[y][x] = lweight;
				}
			}
		}
	}

	@Override
	public Histogram computeHistogram(int x, int y, int w, int h) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				final int bin = binMap[r][c];
				hist.values[bin] += weights[r][c];

				if (wrap && bin + 1 == nbins) {
					hist.values[0] += (1 - weights[r][c]);
				}

				if (bin + 1 < nbins) {
					hist.values[bin + 1] += (1 - weights[r][c]);
				}
			}
		}

		return hist;
	}

	@Override
	public Histogram computeHistogram(int x, int y, int w, int h, FImage extWeights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				final int bin = binMap[r][c];
				hist.values[bin] += (extWeights.pixels[r][c] * weights[r][c]);

				if (wrap && bin + 1 == nbins) {
					hist.values[0] += (extWeights.pixels[r][c] * (1 - weights[r][c]));
				}

				if (bin + 1 < nbins) {
					hist.values[bin + 1] += (extWeights.pixels[r][c] * (1 - weights[r][c]));
				}
			}
		}

		return hist;
	}

	@Override
	public Histogram computeHistogram(int x, int y, FImage extWeights, FImage windowWeights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + windowWeights.height);
		final int stopx = Math.min(binMap[0].length, x + windowWeights.width);

		final int startwr = y < 0 ? -y : y;
		final int startwc = x < 0 ? -x : x;

		for (int r = starty, wr = startwr; r < stopy; r++, wr++) {
			for (int c = startx, wc = startwc; c < stopx; c++, wc++) {
				final int bin = binMap[r][c];
				hist.values[bin] += (extWeights.pixels[r][c] * weights[r][c] * windowWeights.pixels[wr][wc]);

				if (wrap && bin + 1 == nbins) {
					hist.values[0] += (extWeights.pixels[r][c] * (1 - weights[r][c]) * windowWeights.pixels[wr][wc]);
				}

				if (bin + 1 < nbins) {
					hist.values[bin + 1] += (extWeights.pixels[r][c] * (1 - weights[r][c]) * windowWeights.pixels[wr][wc]);
				}
			}
		}

		return hist;
	}

	/**
	 * 得到权重
	 *
	 */
	public float[][] getWeightsMap() {
		return weights;
	}

}
