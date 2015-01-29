package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 完整图像和完整区域方法的实现
 *
 * @author wanggang
 */
public class SummedAreaTable implements ImageAnalyser<FImage> {

	public FImage data;

	/**
	 *   简单的构造函数
	 */
	public SummedAreaTable() {
	}

	/**
	 *通过给定的图像就行构建
	 *
	 */
	public SummedAreaTable(FImage image) {
		computeTable(image);
	}

	protected void computeTable(FImage image) {
		data = new FImage(image.width + 1, image.height + 1);

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				data.pixels[y + 1][x + 1] = image.pixels[y][x] + data.pixels[y + 1][x] + data.pixels[y][x + 1]
						- data.pixels[y][x];
			}
		}
	}

	/**
	 *  计算图像总共的像素点的和
	 */
	public float calculateArea(int x1, int y1, int x2, int y2) {
		final float A = data.pixels[y1][x1];
		final float B = data.pixels[y1][x2];
		final float C = data.pixels[y2][x2];
		final float D = data.pixels[y2][x1];

		return A + C - B - D;
	}

	/**
	 *通过给定的矩阵的区域返回矩阵内的像素点的个数

	 */
	public float calculateArea(Rectangle r) {
		return calculateArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width), Math.round(r.y + r.height));
	}

	@Override
	public void analyseImage(FImage image) {
		computeTable(image);
	}

}
