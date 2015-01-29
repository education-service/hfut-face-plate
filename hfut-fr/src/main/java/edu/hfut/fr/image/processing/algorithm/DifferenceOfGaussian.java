package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

import edu.hfut.fr.image.processing.convolution.FGaussianConvolve;

/**
 * 实现高斯分类器
 *
 *@author wanghao
 */
public class DifferenceOfGaussian implements ImageProcessor<FImage> {

	FGaussianConvolve filter1;
	FGaussianConvolve filter2;

	/**
	 * 根据默认sigmas构造
	 */
	public DifferenceOfGaussian() {
		this(1, 2);
	}

	public DifferenceOfGaussian(float sigma1, float sigma2) {
		filter1 = new FGaussianConvolve(sigma1);
		filter2 = new FGaussianConvolve(sigma2);
	}

	@Override
	public void processImage(FImage image) {
		FImage blur1 = image.process(filter1);
		FImage blur2 = image.process(filter2);

		image.internalAssign(blur1.subtractInplace(blur2));
	}

}
