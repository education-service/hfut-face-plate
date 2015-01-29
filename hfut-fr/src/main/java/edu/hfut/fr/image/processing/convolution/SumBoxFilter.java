package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.algorithm.SummedAreaTable;

/**
 * 矩阵卷积
 *
 * @author wanghao
 */
public class SumBoxFilter implements SinglebandImageProcessor<Float, FImage> {

	private int width;
	private int height;

	/**
	 * 通过给定的维度构造
	 */
	public SumBoxFilter(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public SumBoxFilter(int dim) {
		this(dim, dim);
	}

	@Override
	public void processImage(FImage image) {
		if (this.height == 1 && this.width == 1)
			return;

		final SummedAreaTable sat = new SummedAreaTable();
		sat.analyseImage(image);

		final int hw = width / 2;
		final int hh = height / 2;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final int sx = Math.max(0, x - hw);
				final int sy = Math.max(0, y - hh);
				final int ex = Math.min(image.width, x + hw + 1);
				final int ey = Math.min(image.height, y + hh + 1);

				final float mean = sat.calculateArea(sx, sy, ex, ey);
				image.pixels[y][x] = mean;
			}
		}
	}

}
