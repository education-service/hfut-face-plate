package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.processor.PixelProcessor;

/**
 * 像素的指数实现
 *
 *@author wanghao
 */
public class Pow implements PixelProcessor<Float> {

	double power;

	/**
	 * 构造函数
	 */
	public Pow(double power) {
		this.power = power;
	}

	@Override
	public Float processPixel(Float pixel) {
		return (float) Math.pow(pixel, power);
	}

}
