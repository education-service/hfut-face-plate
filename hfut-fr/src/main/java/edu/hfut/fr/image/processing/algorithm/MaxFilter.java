package edu.hfut.fr.image.processing.algorithm;

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 *最大分类器
 *
 */
public class MaxFilter implements SinglebandImageProcessor<Float, FImage> {
	private Set<Pixel> support;
	private int blockWidth;
	private int blockHeight;

	/**
	 * 构造函数
	 * 
	 */
	public MaxFilter(Set<Pixel> support) {
		this.support = support;

		if (FilterSupport.isBlockSupport(support)) {
			blockWidth = FilterSupport.getSupportWidth(support);
			blockHeight = FilterSupport.getSupportHeight(support);
		}
	}

	@Override
	public void processImage(FImage image) {
		if (blockWidth >= 1 && blockHeight >= 1) {
			maxHorizontalSym(image, blockWidth);
			maxVerticalSym(image, blockWidth);
		} else {
			final FImage tmpImage = new FImage(image.width, image.height);

			for (int y = 0; y < image.height; y++) {
				for (int x = 0; x < image.width; x++) {
					float max = -Float.MAX_VALUE;

					for (final Pixel sp : support) {
						final int xx = x + sp.x;
						final int yy = y + sp.y;

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							max = Math.max(max, image.pixels[yy][xx]);
						}
					}

					tmpImage.pixels[y][x] = max;
				}
			}
			image.internalAssign(tmpImage);
		}
	}

	private static void maxHorizontalSym(FImage image, int width) {
		final int halfsize = width / 2;
		final float buffer[] = new float[image.width + width];

		for (int r = 0; r < image.height; r++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[r][0];
			for (int i = 0; i < image.width; i++)
				buffer[halfsize + i] = image.pixels[r][i];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.width + i] = image.pixels[r][image.width - 1];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++)
					max = Math.max(buffer[i + j], max);

				image.pixels[r][i] = max;
			}
		}
	}

	private static void maxVerticalSym(FImage image, int width) {
		final int halfsize = width / 2;

		final float buffer[] = new float[image.height + width];

		for (int c = 0; c < image.width; c++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[0][c];
			for (int i = 0; i < image.height; i++)
				buffer[halfsize + i] = image.pixels[i][c];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.height + i] = image.pixels[image.height - 1][c];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++)
					max = Math.max(buffer[i + j], max);

				buffer[i] = max;
			}

			for (int r = 0; r < image.height; r++)
				image.pixels[r][c] = buffer[r];
		}
	}

	/**
	 *实现分类
	 */
	public static void filter(FImage img, int times) {
		final MaxFilter d = new MaxFilter(FilterSupport.BLOCK_3x3);
		for (int i = 0; i < times; i++)
			img.processInplace(d);
	}
}
