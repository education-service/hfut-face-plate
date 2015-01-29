package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * 创建窗口直方图的接口
 *
 * @author wanghao
 */
public interface WindowedHistogramExtractor {

	/**
	 * 得到直方图柱状的个数
	 *
	 */
	public abstract int getNumBins();

	public abstract Histogram computeHistogram(Rectangle roi);

	/**
	 * 通过给定的窗口显示直方图
	 */
	public abstract Histogram computeHistogram(int x, int y, int w, int h);

	public abstract void computeHistogram(Rectangle roi, Histogram histogram);

	public abstract void computeHistogram(int x, int y, int w, int h, Histogram histogram);

}
