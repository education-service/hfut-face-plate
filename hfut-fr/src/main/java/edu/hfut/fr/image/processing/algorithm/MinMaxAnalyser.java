package edu.hfut.fr.image.processing.algorithm;

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;

/**
 *计算相邻像素的最大和最小值
 *
 *@author wanghao
 */
public class MinMaxAnalyser implements ImageAnalyser<FImage> {

	private Set<Pixel> support;
	private int blockWidth = -1;
	private int blockHeight = -1;

	/**
	 * 最小值
	 */
	public FImage min;

	/**
	*  最大值
	 */
	public FImage max;

	/**
	 * 构造函数
	 */
	public MinMaxAnalyser(Set<Pixel> support) {
		this.support = support;

		if (FilterSupport.isBlockSupport(support)) {
			blockWidth = FilterSupport.getSupportWidth(support);
			blockHeight = FilterSupport.getSupportHeight(support);
		}
	}

	@Override
	public void analyseImage(FImage image) {
		min = new FImage(image.width, image.height);
		max = new FImage(image.width, image.height);

		if (blockHeight >= 1 && blockWidth >= 1) {
			processBlock(image, blockWidth, blockHeight);
		} else {
			for (int y = 0; y < image.height; y++) {
				for (int x = 0; x < image.width; x++) {
					float minv = Float.MAX_VALUE;
					float maxv = -Float.MAX_VALUE;

					for (final Pixel sp : support) {
						final int xx = x + sp.x;
						final int yy = y + sp.y;

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							minv = Math.min(minv, image.pixels[yy][xx]);
							maxv = Math.max(maxv, image.pixels[yy][xx]);
						}
					}

					min.pixels[y][x] = minv;
					max.pixels[y][x] = maxv;
				}
			}
		}
	}

	private void processBlock(FImage image, int width, int height) {
		final int halfWidth = width / 2;
		final float buffer[] = new float[image.width + width];

		for (int r = 0; r < image.height; r++) {
			for (int i = 0; i < halfWidth; i++)
				buffer[i] = image.pixels[r][0];
			for (int i = 0; i < image.width; i++)
				buffer[halfWidth + i] = image.pixels[r][i];
			for (int i = 0; i < halfWidth; i++)
				buffer[halfWidth + image.width + i] = image.pixels[r][image.width - 1];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float min = Float.MAX_VALUE;
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++) {
					min = Math.min(buffer[i + j], min);
					max = Math.max(buffer[i + j], max);
				}

				this.min.pixels[r][i] = min;
				this.max.pixels[r][i] = max;
			}
		}

		final int halfHeight = height / 2;

		final float minbuffer[] = new float[min.height + height];
		final float maxbuffer[] = new float[max.height + height];

		for (int c = 0; c < min.width; c++) {
			for (int i = 0; i < halfHeight; i++) {
				minbuffer[i] = min.pixels[0][c];
				maxbuffer[i] = max.pixels[0][c];
			}
			for (int i = 0; i < min.height; i++) {
				minbuffer[halfHeight + i] = min.pixels[i][c];
				maxbuffer[halfHeight + i] = max.pixels[i][c];
			}
			for (int i = 0; i < halfHeight; i++) {
				minbuffer[halfHeight + min.height + i] = min.pixels[min.height - 1][c];
				maxbuffer[halfHeight + min.height + i] = max.pixels[max.height - 1][c];
			}

			final int l = minbuffer.length - height;
			for (int i = 0; i < l; i++) {
				float minv = Float.MAX_VALUE;
				float maxv = -Float.MAX_VALUE;

				for (int j = 0; j < height; j++) {
					minv = Math.min(minbuffer[i + j], minv);
					maxv = Math.max(maxbuffer[i + j], maxv);
				}

				minbuffer[i] = minv;
				maxbuffer[i] = maxv;
			}

			for (int r = 0; r < min.height; r++) {
				min.pixels[r][c] = minbuffer[r];
				max.pixels[r][c] = maxbuffer[r];
			}
		}
	}

}
