package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Gabor过滤器类
 *
 * @author wanghao
 */
public class GaborFilters {

	private GaborFilters() {
	};

	public static FImage[] createGaborJets(int width, int height, int... orientationsPerScale) {
		final int nscales = orientationsPerScale.length;
		final int nfilters = (int) ArrayUtils.sumValues(orientationsPerScale);

		final FImage[] filters = new FImage[nfilters];

		final double[][] param = new double[nfilters][];
		for (int i = 0, l = 0; i < nscales; i++) {
			for (int j = 0; j < orientationsPerScale[i]; j++) {
				param[l++] = new double[] { .35, .3 / Math.pow(1.85, i),
						16.0 * orientationsPerScale[i] * orientationsPerScale[i] / (32.0 * 32.0),
						Math.PI / (orientationsPerScale[i]) * j };
			}
		}

		final double[][] freq = new double[height][width];
		final double[][] phase = new double[height][width];

		final float hw = width / 2f;
		final float hh = height / 2f;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float fx = x - hw;
				final float fy = y - hh;
				freq[y][x] = Math.sqrt(fx * fx + fy * fy);
				phase[y][x] = Math.atan2(fy, fx);
			}
		}

		for (int i = 0; i < nfilters; i++) {
			filters[i] = new FImage(width * 2, height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double tr = phase[y][x] + param[i][3];

					if (tr > Math.PI)
						tr -= 2 * Math.PI;
					else if (tr < -Math.PI)
						tr += 2 * Math.PI;

					filters[i].pixels[y][x * 2] = (float) Math.exp(-10 * param[i][0]
							* (freq[y][x] / width / param[i][1] - 1) * (freq[y][x] / width / param[i][1] - 1) - 2
							* param[i][2] * Math.PI * tr * tr);
				}
			}
		}

		return filters;
	}

}
