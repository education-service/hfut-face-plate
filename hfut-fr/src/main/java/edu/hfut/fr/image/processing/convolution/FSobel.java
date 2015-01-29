package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * sobel过滤器
 *
 * @author wanghao
 */
public class FSobel implements ImageAnalyser<FImage> {

	private float sigma;

	/**
	 * x方向梯度
	 */
	public FImage dx;

	/**
	 * Y方向梯度
	 */
	public FImage dy;

	public FSobel() {
		this(0);
	}

	/**
	 * 给定标准偏差进行构造
	 */
	public FSobel(float sigma) {
		this.sigma = sigma;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImage tmp = sigma == 0 ? image : image.process(new FGaussianConvolve(sigma));
		dx = tmp.process(new FSobelX());
		dy = tmp.process(new FSobelY());
	}

}
