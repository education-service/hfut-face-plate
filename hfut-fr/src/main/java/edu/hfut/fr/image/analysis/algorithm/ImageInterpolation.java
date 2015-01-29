package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.util.Interpolation;

/**
 * 提供预测像素值的差值方法
 *
 * @author  wanggang
 */
public class ImageInterpolation implements ImageAnalyser<FImage> {

	public static interface Interpolator {
		/**
		 *  插入一个像素值
		 *
		 */
		public float interpolate(float x, float y, FImage image, Object workingSpace);

		public Object createWorkingSpace();
	}

	/**
	 * 标准的插入类型
	 *
	 */
	public static enum InterpolationType implements Interpolator {
		/**
		 * 最邻近插值法
		 */
		NEAREST_NEIGHBOUR {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				x = Math.round(x);
				y = Math.round(y);

				if (x < 0 || x >= image.width || y < 0 || y >= image.height)
					return 0;

				return image.pixels[(int) y][(int) x];
			}

			@Override
			public Object createWorkingSpace() {
				return null;
			}
		},
		/**
		 * 双线性插值法
		 */
		BILINEAR {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				return image.getPixelInterpNative(x, y, 0);
			}

			@Override
			public Object createWorkingSpace() {
				return null;
			}
		},
		/**
		 * 双三次插值法
		 */
		BICUBIC {
			@Override
			public float interpolate(float x, float y, FImage image, Object workingSpace) {
				final float[][] working = (float[][]) workingSpace;

				final int sx = (int) Math.floor(x) - 1;
				final int sy = (int) Math.floor(y) - 1;
				final int ex = sx + 3;
				final int ey = sy + 3;

				for (int yy = sy, i = 0; yy <= ey; yy++, i++) {
					for (int xx = sx, j = 0; xx <= ex; xx++, j++) {
						final int px = xx < 0 ? 0 : xx >= image.width ? image.width - 1 : xx;
						final int py = yy < 0 ? 0 : yy >= image.height ? image.height - 1 : yy;

						working[i][j] = image.pixels[py][px];
					}
				}

				final float dx = (float) (x - Math.floor(x));
				final float dy = (float) (y - Math.floor(y));
				return Interpolation.bicubicInterp(dx, dy, working);
			}

			@Override
			public Object createWorkingSpace() {
				return new float[4][4];
			}
		};
	}

	protected Interpolator interpolator;
	protected Object workingSpace;
	protected FImage image;

	/**
	 * 默认构造函数
	 *
	 */
	public ImageInterpolation(Interpolator interpolator) {
		this.interpolator = interpolator;
		this.workingSpace = interpolator.createWorkingSpace();
	}

	@Override
	public void analyseImage(FImage image) {
		this.image = image;
	}

	/**
	 * Get the interpolated pixel value of the previously analysed image
	 * 根据先前的图像得到差值数据
	 */
	public float getPixelInterpolated(float x, float y) {
		return interpolator.interpolate(x, y, image, workingSpace);
	}

}
