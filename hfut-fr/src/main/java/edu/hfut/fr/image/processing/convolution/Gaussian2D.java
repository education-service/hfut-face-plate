package edu.hfut.fr.image.processing.convolution;

import static java.lang.Math.exp;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * 加单高斯二维卷积
 *
 *@author wanghao
 */
public class Gaussian2D extends FConvolution {

	/**
	 * 构造函数
	 */
	public Gaussian2D(int width, int height, float sigma) {
		super(createKernelImage(width, height, sigma));
	}

	public Gaussian2D(int size, float sigma) {
		super(createKernelImage(size, size, sigma));
	}

	/**
	 * 返回内核图片
	 */
	public static FImage createKernelImage(int size, float sigma) {
		return createKernelImage(size, size, sigma);
	}

	public static FImage createKernelImage(int width, int height, float sigma) {
		final FImage f = new FImage(width, height);
		final int hw = (width - 1) / 2;
		final int hh = (height - 1) / 2;
		final float sigmasq = sigma * sigma;

		for (int y = -hh, j = 0; y <= hh; y++, j++) {
			for (int x = -hw, i = 0; x <= hw; x++, i++) {
				final int radsqrd = x * x + y * y;
				f.pixels[j][i] = (float) exp(-radsqrd / (2 * sigmasq));
			}
		}
		final float sum = FloatArrayStatsUtils.sum(f.pixels);
		return f.divideInplace(sum);
	}

}
