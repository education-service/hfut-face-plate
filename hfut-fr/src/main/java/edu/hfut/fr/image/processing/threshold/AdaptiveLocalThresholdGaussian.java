package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.processing.convolution.FGaussianConvolve;

/**
 * 使用高斯算法来调整局部阈值
 *
 * @author  jimbo
 */
public class AdaptiveLocalThresholdGaussian implements SinglebandImageProcessor<Float, FImage> {

	private float offset;
	private float sigma;

	public AdaptiveLocalThresholdGaussian(float sigma, float offset) {
		this.sigma = sigma;
		this.offset = offset;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmp = image.process(new FGaussianConvolve(sigma));

		final float[][] tpix = tmp.pixels;
		final float[][] ipix = image.pixels;
		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				tpix[y][x] = ipix[y][x] < (tpix[y][x] - offset) ? 0f : 1f;

		image.internalAssign(tmp);
	}

}
