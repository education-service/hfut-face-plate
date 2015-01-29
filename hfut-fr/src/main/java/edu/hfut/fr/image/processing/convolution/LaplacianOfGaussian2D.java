package edu.hfut.fr.image.processing.convolution;

import static java.lang.Math.PI;
import static java.lang.Math.exp;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * 高斯滤波器二位拉普拉斯
 *
 * @author wanghao
 */
public class LaplacianOfGaussian2D extends FConvolution {

	/**
	 * 构造函数
	 */
	public LaplacianOfGaussian2D(int width, int height, float sigma) {
		super(createKernelImage(width, height, sigma));
	}

	public LaplacianOfGaussian2D(int size, float sigma) {
		super(createKernelImage(size, size, sigma));
	}

	/**
	 * 返回内核图像
	 */
	public static FImage createKernelImage(int size, float sigma) {
		return createKernelImage(size, size, sigma);
	}

	public static FImage createKernelImage(int width, int height, float sigma) {
		FImage f = new FImage(width, height);
		int hw = (width - 1) / 2;
		int hh = (height - 1) / 2;
		float sigmasq = sigma * sigma;
		float sigma4 = sigmasq * sigmasq;

		for (int y = -hh, j = 0; y < hh; y++, j++) {
			for (int x = -hw, i = 0; x < hw; x++, i++) {
				int radsqrd = x * x + y * y;
				f.pixels[j][i] = (float) (-1 / (PI * sigma4) * (1 - radsqrd / (2 * sigmasq)) * exp(-radsqrd
						/ (2 * sigmasq)));
			}
		}
		return f.subtractInplace(FloatArrayStatsUtils.mean(f.pixels));
	}

}
