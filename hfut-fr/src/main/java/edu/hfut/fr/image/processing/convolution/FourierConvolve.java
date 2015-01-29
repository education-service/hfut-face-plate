package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import edu.hfut.fr.image.processing.algorithm.FourierTransform;

/**
 * 傅里叶域中卷积计算
 *
 *@author wanghao
 */
public class FourierConvolve implements SinglebandImageProcessor<Float, FImage> {

	private float[][] kernel;

	/**
	 * 卷积计算
	 */
	public FourierConvolve(float[][] kernel) {
		this.kernel = kernel;
	}

	/**
	 * 构造函数
	 */
	public FourierConvolve(FImage kernel) {
		this.kernel = kernel.pixels;
	}

	@Override
	public void processImage(FImage image) {
		convolve(image, kernel, true);
	}

	public static FImage convolve(FImage image, float[][] kernel, boolean inplace) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		final FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		final float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		final float[][] preparedKernel = FourierTransform.prepareData(kernel, rows, cols, false);
		fft.complexForward(preparedKernel);

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedKernel[y][x * 2];
				final float imKernel = preparedKernel[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				preparedImage[y][x * 2] = re;
				preparedImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		FImage out = image;
		if (!inplace)
			out = new FImage(cols, rows);

		FourierTransform.unprepareData(preparedImage, out, false);

		return out;
	}

	public static FImage convolvePrepared(FImage image, FImage filter, boolean centered) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		final FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		final float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, centered);
		fft.complexForward(preparedImage);

		final float[][] preparedKernel = filter.pixels;

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedKernel[y][x * 2];
				final float imKernel = preparedKernel[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				preparedImage[y][x * 2] = re;
				preparedImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		final FImage out = new FImage(cols, rows);
		FourierTransform.unprepareData(preparedImage, out, centered);
		return out;
	}

}
