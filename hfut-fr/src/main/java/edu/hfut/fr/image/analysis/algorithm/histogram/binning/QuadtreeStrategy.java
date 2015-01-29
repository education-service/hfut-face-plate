package edu.hfut.fr.image.analysis.algorithm.histogram.binning;

import java.util.List;

import org.openimaj.image.pixel.sampling.QuadtreeSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.WindowedHistogramExtractor;

/**
 * 采集直方图由一个固定的的四叉树深度采样区域最后生成
 *
 * @author  wanggang
 */
public class QuadtreeStrategy implements SpatialBinningStrategy {

	int nlevels;

	/**
	 *通过给定深度的四叉树来构建
	 */
	public QuadtreeStrategy(int nlevels) {
		this.nlevels = nlevels;
	}

	@Override
	/**
	 * 提取直方图
	 */
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final QuadtreeSampler sampler = new QuadtreeSampler(region, nlevels);
		final int blockSize = binnedData.getNumBins();
		final List<Rectangle> rects = sampler.allRectangles();

		if (output == null || output.values.length != blockSize * rects.size())
			output = new Histogram(blockSize * rects.size());

		final Histogram tmp = new Histogram(blockSize);
		for (int i = 0; i < rects.size(); i++) {
			final Rectangle r = rects.get(i);

			binnedData.computeHistogram(r, tmp);

			System.arraycopy(tmp.values, 0, output.values, blockSize * i, blockSize);
		}

		return output;
	}

}
