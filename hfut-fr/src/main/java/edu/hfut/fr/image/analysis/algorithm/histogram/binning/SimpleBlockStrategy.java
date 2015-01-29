package edu.hfut.fr.image.analysis.algorithm.histogram.binning;

import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.WindowedHistogramExtractor;

/**
 * 每个直方图是由一系列子的直方图构成
 *
 * @author wanggang
 */
public class SimpleBlockStrategy implements SpatialBinningStrategy {

	int numBlocksX;
	int numBlocksY;

	/**
	 *通过给定的x,y方向区域数来构造直方图
	 */
	public SimpleBlockStrategy(int numBlocks) {
		this(numBlocks, numBlocks);
	}

	public SimpleBlockStrategy(int numBlocksX, int numBlocksY) {
		this.numBlocksX = numBlocksX;
		this.numBlocksY = numBlocksY;
	}

	/**
	 * 提取直方图
	 */
	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final float dx = region.width / numBlocksX;
		final float dy = region.height / numBlocksY;
		final int blockSize = binnedData.getNumBins();

		if (output == null || output.values.length != blockSize * numBlocksX * numBlocksY)
			output = new Histogram(blockSize * numBlocksX * numBlocksY);

		final RectangleSampler rs = new RectangleSampler(region, dx, dy, dx, dy);
		int block = 0;
		final Histogram tmp = new Histogram(blockSize);

		for (final Rectangle r : rs) {
			binnedData.computeHistogram(r, tmp);
			tmp.normaliseL2();

			System.arraycopy(tmp.values, 0, output.values, blockSize * block, blockSize);
			block++;
		}

		return output;
	}

}
