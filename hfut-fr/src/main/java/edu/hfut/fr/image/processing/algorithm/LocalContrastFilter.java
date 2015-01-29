package edu.hfut.fr.image.processing.algorithm;

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 *局域对比度过滤器
 *
 *@author wanghao
 */
public class LocalContrastFilter implements SinglebandImageProcessor<Float, FImage> {

	private Set<Pixel> support;

	/**
	 *构造函数
	 */
	public LocalContrastFilter(Set<Pixel> support) {
		this.support = support;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmpImage = new FImage(image.width, image.height);
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				for (final Pixel sp : support) {
					final int xx = x + sp.x;
					final int yy = y + sp.y;

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
						min = Math.min(min, image.pixels[yy][xx]);
						max = Math.min(min, image.pixels[yy][xx]);
					}
				}

				tmpImage.pixels[y][x] = max - min;
			}
		}
		image.internalAssign(tmpImage);
	}

}
