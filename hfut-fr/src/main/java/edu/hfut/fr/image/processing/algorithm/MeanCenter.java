package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

/**
 *像素点的均值
 *
 *@author wanghao
 */
public class MeanCenter implements ImageProcessor<FImage> {

	@Override
	public void processImage(FImage image) {
		final int width = image.width;
		final int height = image.height;
		final float[][] data = image.pixels;

		image.subtractInplace(patchMean(data, 0, 0, width, height));
	}

	public static final float patchMean(final float[][] data) {
		return patchMean(data, 0, 0, data.length > 0 && data[0] != null ? data[0].length : 0, data.length);
	}

	public static final float patchMean(final float[][] data, final int x, final int y, final int width,
			final int height) {
		float accum = 0;

		final int endX = width + x;
		final int endY = height + y;

		for (int yy = y; yy < endY; yy++) {
			for (int xx = x; xx < endX; xx++) {
				accum += data[yy][xx];
			}
		}

		float mean = accum / (width * height);
		return mean;
	}

}
