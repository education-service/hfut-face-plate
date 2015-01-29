package edu.hfut.fr.image.processing.algorithm;

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 *中间分类器
 *
 *@author wanghao
 */
public class MedianFilter implements SinglebandImageProcessor<Float, FImage> {

	private Set<Pixel> support;

	/**
	 *构造函数
	 */
	public MedianFilter(Set<Pixel> support) {
		this.support = support;
	}

	@Override
	public void processImage(FImage image) {
		final float[] tmp = new float[support.size()];
		final FImage tmpImage = new FImage(image.width, image.height);

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				int count = 0;

				for (final Pixel sp : support) {
					final int xx = x + sp.x;
					final int yy = y + sp.y;

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
						tmp[count++] = image.pixels[yy][xx];
					}
				}

				tmpImage.pixels[y][x] = FloatArrayStatsUtils.median(tmp, 0, count);
			}
		}
		image.internalAssign(tmpImage);
	}

}
