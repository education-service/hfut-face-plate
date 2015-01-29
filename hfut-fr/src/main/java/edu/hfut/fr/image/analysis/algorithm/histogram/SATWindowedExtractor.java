package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.SummedAreaTable;

/**
 * 创建任意数量的窗口直方图
 *
 * @author wanggang
 */
public class SATWindowedExtractor implements WindowedHistogramExtractor {

	protected final SummedAreaTable[] sats;
	protected final int nbins;

	protected SATWindowedExtractor(int nbins) {
		this.nbins = nbins;
		this.sats = new SummedAreaTable[nbins];
	}

	/**
	 * 通过给定的空间直方图进行构造
	 */
	public SATWindowedExtractor(FImage[] magnitudeMaps) {
		this.nbins = magnitudeMaps.length;

		sats = new SummedAreaTable[nbins];
		computeSATs(magnitudeMaps);
	}

	protected void computeSATs(FImage[] magnitudeMaps) {
		for (int i = 0; i < nbins; i++) {
			sats[i] = new SummedAreaTable(magnitudeMaps[i]);
		}
	}

	@Override
	public int getNumBins() {
		return nbins;
	}

	@Override
	public Histogram computeHistogram(Rectangle roi) {
		return computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height);
	}

	@Override
	public Histogram computeHistogram(final int x, final int y, final int w, final int h) {
		final Histogram hist = new Histogram(nbins);
		final int x2 = x + w;
		final int y2 = y + h;

		for (int i = 0; i < nbins; i++) {
			final float val = sats[i].calculateArea(x, y, x2, y2);
			hist.values[i] = Math.max(0, val);
		}

		return hist;
	}

	@Override
	public void computeHistogram(final int x, final int y, final int w, final int h, final Histogram hist) {
		final int x2 = x + w;
		final int y2 = y + h;
		final double[] values = hist.values;

		for (int i = 0; i < values.length; i++) {
			final float val = sats[i].calculateArea(x, y, x2, y2);
			values[i] = Math.max(0, val);
		}
	}

	@Override
	public void computeHistogram(Rectangle roi, Histogram histogram) {
		computeHistogram((int) roi.x, (int) roi.y, (int) roi.width, (int) roi.height, histogram);
	}

}
