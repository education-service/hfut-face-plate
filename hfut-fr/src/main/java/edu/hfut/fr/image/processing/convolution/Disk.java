package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * 磁盘--圆形平均过滤器
 *
 *@author wanghao
 */
public class Disk extends FConvolution {

	/**
	 * 设置半径
	 */
	public Disk(int radius) {
		super(createKernelImage(radius));
	}

	public static FImage createKernelImage(int radius) {
		int sze = 2 * radius + 1;
		FImage f = new FImage(sze, sze);
		int hsz = (sze - 1) / 2;

		for (int y = -hsz, j = 0; y < hsz; y++, j++) {
			for (int x = -hsz, i = 0; x < hsz; x++, i++) {
				double rad = Math.sqrt(x * x + y * y);
				f.pixels[j][i] = rad < radius ? 1 : 0;
			}
		}
		float sum = FloatArrayStatsUtils.sum(f.pixels);
		return f.divideInplace(sum);
	}

}
