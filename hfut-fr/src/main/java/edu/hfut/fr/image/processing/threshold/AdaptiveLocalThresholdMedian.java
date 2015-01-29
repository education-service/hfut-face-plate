package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.algorithm.FilterSupport;
import edu.hfut.fr.image.processing.algorithm.MedianFilter;

/**
 * 使用中值和偏移量调整局部阈值
 *
 *@author jimbo
 */
public class AdaptiveLocalThresholdMedian extends AbstractLocalThreshold {

	float offset = 0;

	/**
	 * 构造函数
	 */
	public AdaptiveLocalThresholdMedian(int size) {
		super(size);
	}

	/**
	 * 构造函数
	 */
	public AdaptiveLocalThresholdMedian(int size_x, int size_y) {
		super(size_x, size_y);
	}

	/**
	 * 构造函数
	 */
	public AdaptiveLocalThresholdMedian(int size, float offset) {
		this(size, size, offset);
	}

	/**
	 * 构造函数
	 */
	public AdaptiveLocalThresholdMedian(int size_x, int size_y, float offset) {
		super(size_x, size_y);
		this.offset = offset;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmp = image.process(new MedianFilter(FilterSupport.createBlockSupport(sizeX, sizeY)));

		final float[][] tpix = tmp.pixels;
		final float[][] ipix = image.pixels;
		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				tpix[y][x] = ipix[y][x] < (tpix[y][x] - offset) ? 0f : 1f;

		image.internalAssign(tmp);
	}

}
