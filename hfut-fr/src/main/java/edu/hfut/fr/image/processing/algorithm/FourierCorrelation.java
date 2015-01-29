package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 *傅里叶相关性
 *
 *@author wanghao
 */
public class FourierCorrelation implements SinglebandImageProcessor<Float, FImage> {

	public FImage template;

	/**
	 * 傅里叶相关性构造函数
	 */
	public FourierCorrelation(FImage template) {
		this.template = template;
	}

	@Override
	public void processImage(FImage image) {
		correlate(image, template, true);
	}

	/**
	 * 分析图像相关性
	 */
	public static FImage correlate(FImage image, FImage template, boolean inplace) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		float[][] preparedKernel = FourierTransform.prepareData(template.pixels, rows, cols, false);
		fft.complexForward(preparedKernel);

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				float reImage = preparedImage[y][x * 2];
				float imImage = preparedImage[y][1 + x * 2];

				float reKernel = preparedKernel[y][x * 2];
				float imKernelConj = -1 * preparedKernel[y][1 + x * 2];

				float re = reImage * reKernel - imImage * imKernelConj;
				float im = reImage * imKernelConj + imImage * reKernel;

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

}
