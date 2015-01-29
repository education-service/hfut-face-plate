package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 实现完整图像的构造，包含了计算总和数值的实现
 *
 * @author wanggang
 */
public class SummedSqAreaTable implements ImageAnalyser<FImage> {

	public FImage sum;

	public FImage sqSum;

	public SummedSqAreaTable() {
	}

	/**
	 */
	public SummedSqAreaTable(FImage image) {
		computeTable(image);
	}

	protected void computeTable(FImage image) {
		sum = new FImage(image.getWidth() + 1, image.getHeight() + 1);
		sqSum = new FImage(image.getWidth() + 1, image.getHeight() + 1);

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float p = image.pixels[y][x];

				sum.pixels[y + 1][x + 1] = p + sum.pixels[y + 1][x] + sum.pixels[y][x + 1] - sum.pixels[y][x];

				sqSum.pixels[y + 1][x + 1] = p * p + sqSum.pixels[y + 1][x] + sqSum.pixels[y][x + 1]
						- sqSum.pixels[y][x];
			}
		}
	}

	/**
	 * 计算图像中给定区域中的总像素值
	 */
	public float calculateSumArea(int x1, int y1, int x2, int y2) {
		final float A = sum.pixels[y1][x1];
		final float B = sum.pixels[y1][x2];
		final float C = sum.pixels[y2][x2];
		final float D = sum.pixels[y2][x1];

		return A + C - B - D;
	}

	public float calculateSumArea(Rectangle r) {
		return calculateSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width), Math.round(r.y + r.height));
	}

	public float calculateSqSumArea(int x1, int y1, int x2, int y2) {
		final float A = sqSum.pixels[y1][x1];
		final float B = sqSum.pixels[y1][x2];
		final float C = sqSum.pixels[y2][x2];
		final float D = sqSum.pixels[y2][x1];

		return A + C - B - D;
	}

	public float calculateSqSumArea(Rectangle r) {
		return calculateSqSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width),
				Math.round(r.y + r.height));
	}

	@Override
	public void analyseImage(FImage image) {
		computeTable(image);
	}

}
