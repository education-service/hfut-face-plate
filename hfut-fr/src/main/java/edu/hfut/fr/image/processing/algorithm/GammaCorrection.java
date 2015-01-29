package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.processor.PixelProcessor;

/**
 *gamma关联度实现类
 *
 *@author wanghao
 */
public class GammaCorrection implements PixelProcessor<Float> {

	protected double gamma;

	/**
	 * 构造函数
	 */
	public GammaCorrection() {
		this.gamma = 0.2;
	}

	public GammaCorrection(double gamma) {
		this.gamma = gamma;
	}

	@Override
	public Float processPixel(Float pixel) {
		if (gamma == 0) {
			return (float) Math.log(pixel);
		}
		return (float) Math.pow(pixel, gamma);
	}

}
