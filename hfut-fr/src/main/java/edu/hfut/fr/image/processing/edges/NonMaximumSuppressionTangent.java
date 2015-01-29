package edu.hfut.fr.image.processing.edges;

import org.openimaj.image.FImage;
import org.openimaj.image.combiner.ImageCombiner;

/**
 * 使用x,y梯度的非最大抑制
 *
 * @author wanghao
 */
public class NonMaximumSuppressionTangent implements ImageCombiner<FImage, FImage, FImage> {

	public static FImage computeSuppressed(FImage dxImage, FImage dyImage) {
		return computeSuppressed(dxImage, dyImage, null);
	}

	public static FImage computeSuppressed(FImage dxImage, FImage dyImage, FImage magsOut) {
		final float[][] diffx = dxImage.pixels;
		final float[][] diffy = dyImage.pixels;
		final int width = dxImage.width;
		final int height = dxImage.height;

		final float[][] mag = magsOut == null ? new float[height][width] : magsOut.pixels;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				mag[y][x] = (float) Math.sqrt(diffx[y][x] * diffx[y][x] + diffy[y][x] * diffy[y][x]);

		final FImage outimg = new FImage(width, height);
		final float[][] output = outimg.pixels;

		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				int dx, dy;

				if (diffx[y][x] > 0)
					dx = 1;
				else
					dx = -1;

				if (diffy[y][x] > 0)
					dy = -1;
				else
					dy = 1;

				float a1, a2, b1, b2, A, B, point, val;
				if (Math.abs(diffx[y][x]) > Math.abs(diffy[y][x])) {
					a1 = mag[y][x + dx];
					a2 = mag[y - dy][x + dx];
					b1 = mag[y][x - dx];
					b2 = mag[y + dy][x - dx];
					A = (Math.abs(diffx[y][x]) - Math.abs(diffy[y][x])) * a1 + Math.abs(diffy[y][x]) * a2;
					B = (Math.abs(diffx[y][x]) - Math.abs(diffy[y][x])) * b1 + Math.abs(diffy[y][x]) * b2;
					point = mag[y][x] * Math.abs(diffx[y][x]);
					if (point >= A && point > B) {
						val = Math.abs(diffx[y][x]);
						output[y][x] = val;
					} else {
						val = 0;
						output[y][x] = val;
					}
				} else {
					a1 = mag[y - dy][x];
					a2 = mag[y - dy][x + dx];
					b1 = mag[y + dy][x];
					b2 = mag[y + dy][x - dx];
					A = (Math.abs(diffy[y][x]) - Math.abs(diffx[y][x])) * a1 + Math.abs(diffx[y][x]) * a2;
					B = (Math.abs(diffy[y][x]) - Math.abs(diffx[y][x])) * b1 + Math.abs(diffx[y][x]) * b2;
					point = mag[y][x] * Math.abs(diffy[y][x]);
					if (point >= A && point > B) {
						val = Math.abs(diffy[y][x]);
						output[y][x] = val;
					} else {
						val = 0;
						output[y][x] = val;
					}
				}
			}
		}

		return outimg;
	}

	/**
	 * 实现非最大抑制
	 */
	@Override
	public FImage combine(FImage dxImage, FImage dyImage) {
		return computeSuppressed(dxImage, dyImage);
	}

}
