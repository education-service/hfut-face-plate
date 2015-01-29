package edu.hfut.fr.image.processing.convolution;

import odk.lang.FastMath;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * 图像梯度方向处理器
 *
 *@author wanghao
 */
public class FImageGradients implements ImageAnalyser<FImage> {

	public enum Mode {

		Unsigned(-PI_OVER_TWO_FLOAT, PI_OVER_TWO_FLOAT) {
			@Override
			void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations) {
				FImageGradients.gradientMagnitudesAndUnsignedOrientations(image, magnitudes, orientations);
			}
		},

		Signed(-PI_FLOAT, PI_FLOAT) {
			@Override
			void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations) {
				FImageGradients.gradientMagnitudesAndOrientations(image, magnitudes, orientations);
			}
		};

		private float min;
		private float max;

		private Mode(float min, float max) {
			this.min = min;
			this.max = max;
		}

		abstract void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations);

		/**
		 * 获得最小角度
		 */
		public float minAngle() {
			return min;
		}

		/**
		 * 获得最大角度
		 */
		public float maxAngle() {
			return max;
		}
	}

	private final static float PI_FLOAT = (float) Math.PI;
	private final static float PI_OVER_TWO_FLOAT = (float) Math.PI / 2f;
	private final static float TWO_PI_FLOAT = (float) (Math.PI * 2);

	/**
	 * 图像梯度幅度
	 */
	public FImage magnitudes;

	public FImage orientations;

	public Mode mode;

	public FImageGradients() {
		this.mode = Mode.Signed;
	}

	/**
	 * 给定模型进行构造
	 */
	public FImageGradients(Mode mode) {
		this.mode = mode;
	}

	@Override
	public void analyseImage(FImage image) {
		if (magnitudes == null || magnitudes.height != image.height || magnitudes.width != image.width) {
			magnitudes = new FImage(image.width, image.height);
			orientations = new FImage(image.width, image.height);
		}

		mode.gradientMagnitudesAndOrientations(image, magnitudes, orientations);
	}

	public static FImageGradients getGradientMagnitudesAndOrientations(FImage image) {
		final FImageGradients go = new FImageGradients();
		go.analyseImage(image);

		return go;
	}

	public static FImageGradients getGradientMagnitudesAndOrientations(FImage image, Mode mode) {
		final FImageGradients go = new FImageGradients(mode);
		go.analyseImage(image);

		return go;
	}

	/**
	 * 估计梯度的幅度和方向
	 */
	public static void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations) {
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				magnitudes.pixels[r][c] = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				orientations.pixels[r][c] = (float) FastMath.atan2(ygrad, xgrad);
			}
		}
	}

	public static void gradientMagnitudesAndUnsignedOrientations(FImage image, FImage magnitudes, FImage orientations) {
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				magnitudes.pixels[r][c] = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				if (magnitudes.pixels[r][c] == 0)
					orientations.pixels[r][c] = 0;
				else
					orientations.pixels[r][c] = (float) FastMath.atan(ygrad / xgrad);
			}
		}
	}

	public static void gradientMagnitudesAndQuantisedOrientations(FImage image, FImage[] magnitudes) {
		final int numOriBins = magnitudes.length;

		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				final float mag = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				float ori = (float) FastMath.atan2(ygrad, xgrad);

				ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

				final float po = numOriBins * ori / TWO_PI_FLOAT; // po is now
																	// 0<=po<oriSize

				final int oi = (int) Math.floor(po);
				final float of = po - oi;

				for (int i = 0; i < magnitudes.length; i++)
					magnitudes[i].pixels[r][c] = 0;

				magnitudes[oi % numOriBins].pixels[r][c] = (1f - of) * mag;
				magnitudes[(oi + 1) % numOriBins].pixels[r][c] = of * mag;
			}
		}
	}

	public static void gradientMagnitudesAndQuantisedOrientations(FImage image, FImage[] magnitudes, boolean interp,
			Mode mode) {
		final int numOriBins = magnitudes.length;
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];
				final float mag = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);

				float po;
				if (mode == Mode.Unsigned) {
					final float ori = mag == 0 ? PI_OVER_TWO_FLOAT : (float) FastMath.atan(ygrad / xgrad)
							+ PI_OVER_TWO_FLOAT;

					po = numOriBins * ori / PI_FLOAT;
				} else {
					float ori = (float) FastMath.atan2(ygrad, xgrad);

					ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

					po = numOriBins * ori / TWO_PI_FLOAT;
				}

				for (int i = 0; i < magnitudes.length; i++)
					magnitudes[i].pixels[r][c] = 0;

				int oi = (int) Math.floor(po);
				final float of = po - oi;

				if (interp) {
					magnitudes[oi % numOriBins].pixels[r][c] = (1f - of) * mag;
					magnitudes[(oi + 1) % numOriBins].pixels[r][c] = of * mag;
				} else {
					if (oi > numOriBins - 1)
						oi = numOriBins - 1;
					magnitudes[oi].pixels[r][c] = mag;
				}
			}
		}
	}

}
