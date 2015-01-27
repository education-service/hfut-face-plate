package edu.hfut.lpr.analysis;

import java.awt.image.BufferedImage;

/**
 * 图像统计计算类
 *
 * 注：这里将图像亮度值作为像素点的计算值
 *
 * @author wanggang
 *
 */
public class Statistics {

	// 亮度最小值
	public float maximum;
	// 亮度最大值
	public float minimum;
	// 亮度平均值
	public float average;
	// 亮度分散系或者离差
	public float dispersion;

	Statistics(BufferedImage bi) {
		this(new Photo(bi));
	}

	Statistics(Photo photo) {
		float sum = 0;
		float sum2 = 0;
		int w = photo.getWidth();
		int h = photo.getHeight();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float pixelValue = photo.getBrightness(x, y);
				this.maximum = Math.max(pixelValue, this.maximum);
				this.minimum = Math.min(pixelValue, this.minimum);
				sum += pixelValue;
				sum2 += (pixelValue * pixelValue);
			}
		}
		int count = (w * h);
		this.average = sum / count;
		// 像素平方和的均值 - 像素平均值的平方
		this.dispersion = (sum2 / count) - (this.average * this.average);
	}

	/**
	 * 根据域值系数对亮度值的取域值计算
	 */
	public float thresholdBrightness(float value, float coef) {
		float out;
		if (value > this.average) {
			out = coef + (((1 - coef) * (value - this.average)) / (this.maximum - this.average));
		} else {
			out = ((1 - coef) * (value - this.minimum)) / (this.average - this.minimum);
		}
		return out;
	}

}