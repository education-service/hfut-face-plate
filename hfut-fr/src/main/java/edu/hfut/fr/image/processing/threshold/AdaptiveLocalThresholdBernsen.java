package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.algorithm.FilterSupport;
import edu.hfut.fr.image.processing.algorithm.LocalContrastFilter;
import edu.hfut.fr.image.processing.convolution.AverageBoxFilter;

/**
 * Bernsen 调整局部阈值
 *
 *@author jimbo
 */
public class AdaptiveLocalThresholdBernsen extends AbstractLocalThreshold {

	private float threshold;

	public AdaptiveLocalThresholdBernsen(float threshold, int size) {
		super(size);
		this.threshold = threshold;
	}

	public AdaptiveLocalThresholdBernsen(float threshold, int size_x, int size_y) {
		super(size_x, size_y);
		this.threshold = threshold;
	}

	@Override
	public void processImage(FImage image) {
		final FImage contrast = image.process(new LocalContrastFilter(FilterSupport.createBlockSupport(sizeX, sizeY)));
		final FImage avg = image.process(new AverageBoxFilter(sizeX, sizeY));

		final float[][] cpix = contrast.pixels;
		final float[][] mpix = avg.pixels;
		final float[][] ipix = image.pixels;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (cpix[y][x] < threshold)
					ipix[y][x] = (mpix[y][x] >= 128) ? 1 : 0;
				else
					ipix[y][x] = (ipix[y][x] >= mpix[y][x]) ? 1 : 0;
			}
		}
	}

}
