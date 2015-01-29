package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.convolution.AverageBoxFilter;

/**
 * 调整局部阈值
 *
 *@author jimbo
 */

public class AdaptiveLocalThresholdMean extends AbstractLocalThreshold {

	float offset;

	public AdaptiveLocalThresholdMean(int size) {
		super(size);
	}

	public AdaptiveLocalThresholdMean(int size_x, int size_y) {
		super(size_x, size_y);
	}

	public AdaptiveLocalThresholdMean(int size, float offset) {
		this(size, size, offset);
	}

	public AdaptiveLocalThresholdMean(int size_x, int size_y, float offset) {
		super(size_x, size_y);
		this.offset = offset;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmp = image.process(new AverageBoxFilter(sizeX, sizeY));

		final float[][] tpix = tmp.pixels;
		final float[][] ipix = image.pixels;
		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				tpix[y][x] = ipix[y][x] < (tpix[y][x] - offset) ? 0f : 1f;

		image.internalAssign(tmp);
	}

}
