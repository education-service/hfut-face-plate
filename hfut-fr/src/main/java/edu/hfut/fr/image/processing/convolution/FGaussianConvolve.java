package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 高斯卷积图像处理器
 *
 * @author wanghao
 */
public class FGaussianConvolve implements SinglebandImageProcessor<Float, FImage> {

	/**
	 * 默认sigma
	 */
	public static final float DEFAULT_GAUSS_TRUNCATE = 4.0f;

	protected float[] kernel;

	/**
	 * 标准偏差位sigma的高斯
	 */
	public FGaussianConvolve(float sigma) {
		this(sigma, DEFAULT_GAUSS_TRUNCATE);
	}

	public FGaussianConvolve(float sigma, float truncate) {
		kernel = makeKernel(sigma, truncate);
	}

	/**
	 * 构造0均值高斯
	 */
	public static float[] makeKernel(float sigma) {
		return makeKernel(sigma, DEFAULT_GAUSS_TRUNCATE);
	}

	public static float[] makeKernel(float sigma, float truncate) {
		if (sigma == 0)
			return new float[] { 1f };
		int ksize = (int) (2.0f * truncate * sigma + 1.0f);
		if (ksize % 2 == 0)
			ksize++;

		final float[] kernel = new float[ksize];

		float sum = 0.0f;
		for (int i = 0; i < ksize; i++) {
			final float x = i - ksize / 2;
			kernel[i] = (float) Math.exp(-x * x / (2.0 * sigma * sigma));
			sum += kernel[i];
		}

		for (int i = 0; i < ksize; i++) {
			kernel[i] /= sum;
		}

		return kernel;
	}

	@Override
	public void processImage(FImage image) {
		FImageConvolveSeparable.convolveHorizontal(image, kernel);
		FImageConvolveSeparable.convolveVertical(image, kernel);
	}

}
