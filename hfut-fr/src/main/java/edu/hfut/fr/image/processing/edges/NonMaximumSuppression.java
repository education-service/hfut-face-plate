package edu.hfut.fr.image.processing.edges;

import org.openimaj.image.FImage;
import org.openimaj.image.combiner.ImageCombiner;

/**
 * 使用大小和方向进行非最大抑制
 *
 * @author wanghao
 */
public class NonMaximumSuppression implements ImageCombiner<FImage, FImage, FImage> {

	public static FImage computeSuppressed(FImage mag, FImage ori) {
		int height = mag.getHeight(), width = mag.getWidth();

		FImage suppressed = new FImage(width, height);

		float p8 = (float) (Math.PI / 8.0);

		// 计算最大抑制
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				if (mag.pixels[y][x] > 0) {
					float t = (float) (ori.pixels[y][x] - Math.PI / 2);

					if ((t >= -p8 && t <= p8) || (t <= -7 * p8 || t >= 7 * p8)) {
						if (mag.pixels[y][x] > mag.pixels[y + 1][x] && mag.pixels[y][x] >= mag.pixels[y - 1][x]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else if ((t >= 3 * p8 && t <= 5 * p8) || (t >= -5 * p8 && t <= -3 * p8)) { // +/-90
																									// degrees
						if (mag.pixels[y][x] >= mag.pixels[y][x + 1] && mag.pixels[y][x] > mag.pixels[y][x - 1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else if ((t >= p8 && t <= 3 * p8) || (t >= -7 * p8 && t <= -5 * p8)) {
						if (mag.pixels[y][x] > mag.pixels[y + 1][x - 1] && mag.pixels[y][x] >= mag.pixels[y - 1][x + 1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					} else {
						if (mag.pixels[y][x] > mag.pixels[y - 1][x - 1] && mag.pixels[y][x] >= mag.pixels[y + 1][x + 1]) {
							suppressed.pixels[y][x] = mag.pixels[y][x];
						}
					}
				}
			}
		}

		return suppressed;
	}

	/**
	 * 实现非最大抑制计算
	 *
	 */
	@Override
	public FImage combine(FImage mag, FImage ori) {
		return computeSuppressed(mag, ori);
	}

}
