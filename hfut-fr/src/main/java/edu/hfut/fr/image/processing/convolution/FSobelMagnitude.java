package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandKernelProcessor;

/**
 * 对图像进行sobel操作
 *
 *@author wanghao
 */
public class FSobelMagnitude implements SinglebandKernelProcessor<Float, FImage> {

	/**
	 * sigma高斯偏导数3*3矩阵
	 */
	public static final FImage KERNEL_X = new FImage(new float[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } });

	public static final FImage KERNEL_Y = new FImage(new float[][] { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } });

	@Override
	public int getKernelHeight() {
		return 3;
	}

	@Override
	public int getKernelWidth() {
		return 3;
	}

	@Override
	public Float processKernel(FImage patch) {
		float sumx = 0, sumy = 0;

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				sumx += (KERNEL_X.pixels[2 - r][2 - c] * patch.pixels[r][c]);
				sumy += (KERNEL_Y.pixels[2 - r][2 - c] * patch.pixels[r][c]);
			}
		}

		return (float) Math.sqrt((sumx * sumx) + (sumy * sumy));
	}

}
