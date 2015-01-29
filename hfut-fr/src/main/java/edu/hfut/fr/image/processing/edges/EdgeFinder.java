package edu.hfut.fr.image.processing.edges;

import static java.lang.Math.atan;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * 寻找图像边界
 *
 *@author wanghao
 */
public class EdgeFinder implements ImageAnalyser<FImage> {

	protected float[][] kx;
	protected float[][] ky;

	public FImage magnitude;
	public FImage angle;

	/**
	 * 使用边缘内核找到图像边缘
	 */
	public EdgeFinder(float[][] kx, float[][] ky) {
		this.kx = kx;
		this.ky = ky;
	}

	public EdgeFinder() {
		kx = new float[][] { { +1, +1, 0, -1, -1 }, { +2, +2, 0, -2, -2 }, { +2, +2, 0, -2, -2 },
				{ +2, +2, 0, -2, -2 }, { +1, +1, 0, -1, -1 } };

		ky = new float[][] { { +1, +2, +2, +2, +1 }, { +1, +2, +2, +2, +1 }, { 0, 0, 0, 0, 0 }, { -1, -2, -2, -2, -1 },
				{ -1, -2, -2, -2, -1 } };
	}

	@Override
	public void analyseImage(FImage image) {
		int height = image.getHeight();
		int width = image.getWidth();

		magnitude = new FImage(width, height);
		angle = new FImage(width, height);

		int w = (kx.length - 1) / 2;

		for (int y = w + 1; y < height - w; y++) {
			for (int x = w + 1; x < width - w; x++) {
				double gx = 0;
				double gy = 0;
				for (int j = 0; j < kx.length; j++) {
					for (int i = 0; i < kx.length; i++) {
						gx += image.pixels[y - w + j][x - w + i] * kx[j][i];
						gy += image.pixels[y - w + j][x - w + i] * ky[j][i];
					}
				}

				magnitude.pixels[y][x] = (float) sqrt(gx * gx + gy * gy);

				if (gy != 0)
					angle.pixels[y][x] = (float) atan(gx / -gy);
				else
					angle.pixels[y][x] = 1.57f;
			}
		}
	}

}
