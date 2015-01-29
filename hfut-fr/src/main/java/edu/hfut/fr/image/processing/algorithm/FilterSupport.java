package edu.hfut.fr.image.processing.algorithm;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;

/**
 *图像过滤器
 *
 *@author wanghao
 */
public class FilterSupport {

	public static final Set<Pixel> CROSS_3x3 = new HashSet<Pixel>();
	static {
		CROSS_3x3.add(new Pixel(0, -1));
		CROSS_3x3.add(new Pixel(-1, 0));
		CROSS_3x3.add(new Pixel(0, 0));
		CROSS_3x3.add(new Pixel(1, 0));
		CROSS_3x3.add(new Pixel(0, 1));
	}

	public static final Set<Pixel> BLOCK_3x3 = createBlockSupport(3, 3);

	public static final Set<Pixel> createBlockSupport(final int width, final int height) {
		final HashSet<Pixel> indices = new HashSet<Pixel>(width * height);

		final int startX = -width / 2;
		final int startY = -height / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				indices.add(new Pixel(startX + x, startY + y));
			}
		}

		return indices;
	}

	/**
	 *测试是否支持块支持
	 */
	public final static boolean isBlockSupport(final Set<Pixel> support) {
		final int sw = getSupportWidth(support);
		final int sh = getSupportHeight(support);

		return sw * sh == support.size() && isCentred(support);
	}

	private static boolean isCentred(Set<Pixel> support) {
		final ConnectedComponent cc = new ConnectedComponent(support);
		final Pixel cp = cc.calculateCentroidPixel();
		return cp.x == 0 && cp.y == 0;
	}

	/**
	 *获得支持区域的宽度
	 */
	public final static int getSupportWidth(final Set<Pixel> support) {
		int min = Integer.MAX_VALUE;
		int max = -Integer.MAX_VALUE;

		for (final Pixel p : support) {
			min = Math.min(min, p.x);
			max = Math.max(max, p.x);
		}

		return max - min + 1;
	}

	/**
	 *获得高度
	 */
	public final static int getSupportHeight(final Set<Pixel> support) {
		int min = Integer.MAX_VALUE;
		int max = -Integer.MAX_VALUE;

		for (final Pixel p : support) {
			min = Math.min(min, p.y);
			max = Math.max(max, p.y);
		}

		return max - min + 1;
	}

}
