package edu.hfut.fr.image.analysis.algorithm.histogram.binning;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.WindowedHistogramExtractor;

/**
 * 根据区域的直方图来构建直方图方法的接口
 *
 * @author wanggang
 */
public interface SpatialBinningStrategy {

	/**
	 * 返回在指定区域提取的直方图
	 */
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output);

}
