package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * 生成窗口显示直方图
 *
 * @author wanggang
 */
public class BinnedWindowedExtractor implements ImageAnalyser<FImage>, WindowedHistogramExtractor {

	protected int[][] binMap;
	protected int nbins;
	protected float min = 0;
	protected float max = 1;

	/**
	 * 设置直方图柱体的个数
	 */
	public BinnedWindowedExtractor(int nbins) {
		this.nbins = nbins;
	}

	public BinnedWindowedExtractor(int nbins, float min, float max) {
		this.nbins = nbins;
		this.min = min;
		this.max = max;
	}

	@Override
	public int getNumBins() {
		return nbins;
	}

	/**
	 * 设置个数
	 */
	public void setNbins(int nbins) {
		this.nbins = nbins;
	}

	public float getMin() {
		return min;
	}

	/**
	 * 设置最小个数
	 */
	public void setMin(float min) {
		this.min = min;
	}

	/**
	 * 得到最大的个数.
	 *
	 */
	public float getMax() {
		return max;
	}

	/**
	 * 设置最大值和最小值
	 *
	 */
	public void setMax(float max) {
		this.max = max;
	}

	/**
	 * 计算图片的位图
	 */
	@Override
	public void analyseImage(FImage image) {
		final int height = image.height;
		final int width = image.width;

		binMap = new int[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int bin = (int) (((image.pixels[y][x] - min) / (max - min)) * nbins);

				if (bin > (nbins - 1))
					bin = nbins - 1;

				binMap[y][x] = bin;
			}
		}
	}

	/**
	 * 得到创建的位图
	 * .
	 *
	 */
	public int[][] getBinMap() {
		return binMap;
	}

	@Override
	public Histogram computeHistogram(Rectangle roi) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
	}

	@Override
	public Histogram computeHistogram(int x, int y, int w, int h) {
		final Histogram hist = new Histogram(nbins);

		computeHistogram(x, y, w, h, hist);

		return hist;
	}

	/**
	 *通过给定的图像，计算直方图
	 */
	public Histogram computeHistogram(Rectangle roi, FImage weights) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, weights);
	}

	public Histogram computeHistogram(int x, int y, int w, int h, FImage weights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				hist.values[binMap[r][c]] += weights.pixels[r][c];
			}
		}

		return hist;
	}

	public Histogram computeHistogram(int x, int y, FImage weights, FImage windowWeights) {
		final Histogram hist = new Histogram(nbins);

		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + windowWeights.height);
		final int stopx = Math.min(binMap[0].length, x + windowWeights.width);

		final int startwr = y < 0 ? -y : y;
		final int startwc = x < 0 ? -x : x;

		for (int r = starty, wr = startwr; r < stopy; r++, wr++) {
			for (int c = startx, wc = startwc; c < stopx; c++, wc++) {
				hist.values[binMap[r][c]] += (weights.pixels[r][c] * windowWeights.pixels[wr][wc]);
			}
		}

		return hist;
	}

	@Override
	public void computeHistogram(Rectangle roi, Histogram histogram) {
		computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, histogram);
	}

	@Override
	public void computeHistogram(int x, int y, int w, int h, Histogram histogram) {
		final int starty = Math.max(0, y);
		final int startx = Math.max(0, x);
		final int stopy = Math.min(binMap.length, y + h);
		final int stopx = Math.min(binMap[0].length, x + w);

		for (int r = starty; r < stopy; r++) {
			for (int c = startx; c < stopx; c++) {
				histogram.values[binMap[r][c]]++;
			}
		}
	}

}
