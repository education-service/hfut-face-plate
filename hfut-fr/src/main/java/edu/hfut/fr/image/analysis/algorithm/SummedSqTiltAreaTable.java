package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 计算图像中矩阵区域像素点的总和
 *
 * @author wanggang
 */
public class SummedSqTiltAreaTable extends SummedSqAreaTable {

	public FImage tiltSum;

	public SummedSqTiltAreaTable() {
	}

	public SummedSqTiltAreaTable(FImage image) {
		this(image, true);
	}

	public SummedSqTiltAreaTable(FImage image, boolean computeTilted) {
		computeTable(image, computeTilted);
	}

	private void computeTable(FImage image, boolean computeTilted) {
		if (computeTilted) {
			computeRotSqSumIntegralImages(image);
		} else {
			computeSqSumIntegralImages(image);
		}
	}

	protected void computeSqSumIntegralImages(FImage img) {
		final int width = img.width;
		final int height = img.height;

		sum = new FImage(width + 1, height + 1);
		sqSum = new FImage(width + 1, height + 1);

		final float[][] sumData = sum.pixels;
		final float[][] sqSumData = sqSum.pixels;

		for (int y = 1; y <= height; y++) {
			float rowSum = 0;
			float rowSumSQ = 0;

			final float[] row = img.pixels[y - 1];
			for (int x = 1; x <= width; x++) {
				final float pix = row[x - 1];

				rowSum += pix;
				rowSumSQ += pix * pix;

				sumData[y][x] = sumData[y - 1][x] + rowSum;
				sqSumData[y][x] = sqSumData[y - 1][x] + rowSumSQ;
			}
		}
	}

	protected final void computeRotSqSumIntegralImages(FImage image) {
		final int width = image.width;
		final int height = image.height;

		sum = new FImage(width + 1, height + 1);
		sqSum = new FImage(width + 1, height + 1);
		tiltSum = new FImage(width + 2, height + 2);

		final float[] buffer = new float[width];

		if (height > 0) {
			final float[] row = image.pixels[0];

			float rowSum = 0;
			float sqRowSum = 0;

			for (int x = 1; x <= width; x++) {
				final float gray = (row[x - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[1][x] = rowSum;
				buffer[x - 1] = tiltSum.pixels[1][x] = gray;
				sqSum.pixels[1][x] = sqRowSum;
			}
		}

		if (height > 1) {
			final float[] row = image.pixels[1];

			float rowSum = 0;
			float sqRowSum = 0;

			for (int x = 1; x < width; x++) {
				final float gray = (row[x - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[2][x] = sum.pixels[1][x] + rowSum;
				sqSum.pixels[2][x] = sqSum.pixels[1][x] + sqRowSum;
				tiltSum.pixels[2][x] = tiltSum.pixels[1][x - 1] + buffer[x - 1] + tiltSum.pixels[1][x + 1] + gray;
				buffer[x - 1] = gray;
			}

			if (width > 0) {
				final float gray = (row[width - 1]);

				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[2][width] = sum.pixels[1][width] + rowSum;
				sqSum.pixels[2][width] = sqSum.pixels[1][width] + sqRowSum;
				tiltSum.pixels[2][width] = tiltSum.pixels[1][width - 1] + buffer[width - 1] + gray;
				buffer[width - 1] = gray;
			}
		}

		for (int y = 3; y <= height; y++) {
			final float[] row = image.pixels[y - 1];

			float rowSum = 0;
			float sqRowSum = 0;

			if (width > 0) {
				final float gray = row[0];
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][1] = sum.pixels[y - 1][1] + rowSum;
				sqSum.pixels[y][1] = sqSum.pixels[y - 1][1] + sqRowSum;
				tiltSum.pixels[y][1] = tiltSum.pixels[y - 1][2] + buffer[0] + gray;
				buffer[0] = gray;
			}

			for (int x = 2; x < width; x++) {
				final float gray = row[x - 1];
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][x] = sum.pixels[y - 1][x] + rowSum;
				sqSum.pixels[y][x] = sqSum.pixels[y - 1][x] + sqRowSum;
				tiltSum.pixels[y][x] = tiltSum.pixels[y - 1][x - 1] + buffer[x - 1] + tiltSum.pixels[y - 1][x + 1]
						- tiltSum.pixels[y - 2][x] + gray;
				buffer[x - 1] = gray;
			}

			if (width > 0) {
				final float gray = row[width - 1];
				rowSum += gray;
				sqRowSum += gray * gray;

				sum.pixels[y][width] = sum.pixels[y - 1][width] + rowSum;
				sqSum.pixels[y][width] = sqSum.pixels[y - 1][width] + sqRowSum;
				tiltSum.pixels[y][width] = tiltSum.pixels[y - 1][width - 1] + buffer[width - 1] + gray;
				buffer[width - 1] = gray;
			}
		}
	}

	/**
	 *返回给定矩阵中总像素点
	 */
	public float calculateTiltedSumArea(int x, int y, int width, int height) {
		final float p0 = tiltSum.pixels[y][x];
		final float p1 = tiltSum.pixels[y + height][x - height];
		final float p2 = tiltSum.pixels[y + width][x + width];
		final float p3 = tiltSum.pixels[y + width + height][x + width - height];

		return p0 - p1 - p2 + p3;
	}

	public float calculateTiltedSumArea(Rectangle r) {
		return calculateTiltedSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
	}

	@Override
	public void analyseImage(FImage image) {
		computeTable(image, true);
	}

}
