package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.SingularValueDecomposition;

/**
 * 卷积操作基本类
 *
 * @author wanghao
 */
public class FConvolution implements SinglebandImageProcessor<Float, FImage> {

	/** 内核 */
	public FImage kernel;

	private ConvolveMode mode;

	interface ConvolveMode {
		public void convolve(FImage f);

		class OneD implements ConvolveMode {
			private float[] kernel;
			private boolean rowMode;

			OneD(FImage image) {
				if (image.height == 1) {
					this.rowMode = true;
					this.kernel = image.pixels[0];

				} else {
					this.rowMode = false;
					this.kernel = new float[image.height];
					for (int i = 0; i < image.height; i++)
						this.kernel[i] = image.pixels[i][0];
				}
			}

			@Override
			public void convolve(FImage f) {
				if (this.rowMode)
					FImageConvolveSeparable.convolveHorizontal(f, kernel);
				else
					FImageConvolveSeparable.convolveVertical(f, kernel);
			}

		}

		class Separable implements ConvolveMode {
			private float[] row;
			private float[] col;

			Separable(SingularValueDecomposition svd) {

				final int nrows = svd.getU().getRowDimension();

				this.row = new float[nrows];
				this.col = new float[nrows];

				final float factor = (float) Math.sqrt(svd.getS().get(0, 0));
				for (int i = 0; i < nrows; i++) {
					this.row[i] = (float) svd.getU().get(i, 0) * factor;
					this.col[i] = (float) svd.getV().get(i, 0) * factor;
				}
			}

			@Override
			public void convolve(FImage f) {
				FImageConvolveSeparable.convolveHorizontal(f, row);
				FImageConvolveSeparable.convolveVertical(f, col);
			}
		}

		class BruteForce implements ConvolveMode {
			protected FImage kernel;

			BruteForce(FImage kernel) {
				this.kernel = kernel;
			}

			@Override
			public void convolve(FImage image) {
				final int kh = kernel.height;
				final int kw = kernel.width;
				final int hh = kh / 2;
				final int hw = kw / 2;
				final FImage clone = image.newInstance(image.width, image.height);
				for (int y = hh; y < image.height - (kh - hh); y++) {
					for (int x = hw; x < image.width - (kw - hw); x++) {
						float sum = 0;
						for (int j = 0, jj = kh - 1; j < kh; j++, jj--) {
							for (int i = 0, ii = kw - 1; i < kw; i++, ii--) {
								final int rx = x + i - hw;
								final int ry = y + j - hh;

								sum += image.pixels[ry][rx] * kernel.pixels[jj][ii];
							}
						}
						clone.pixels[y][x] = sum;
					}
				}
				image.internalAssign(clone);
			}
		}
	}

	public FConvolution(FImage kernel) {
		this.kernel = kernel;
		setup(false);
	}

	/**
	 * 给定内核来构造卷积
	 */
	public FConvolution(float[][] kernel) {
		this.kernel = new FImage(kernel);
		setup(false);
	}

	/**
	 * 设置暴力卷积
	 */
	public void setBruteForce(boolean brute) {
		setup(brute);
	}

	private void setup(boolean brute) {
		if (brute) {
			this.mode = new ConvolveMode.BruteForce(this.kernel);
			return;
		}
		if (this.kernel.width == 1 || this.kernel.height == 1) {
			this.mode = new ConvolveMode.OneD(kernel);
		} else {
			MatrixUtils.matrixFromFloat(this.kernel.pixels);
			final SingularValueDecomposition svd = new SingularValueDecomposition(
					MatrixUtils.matrixFromFloat(this.kernel.pixels));
			if (svd.rank() == 1)
				this.mode = new ConvolveMode.Separable(svd);
			else
				this.mode = new ConvolveMode.BruteForce(this.kernel);
		}
	}

	@Override
	public void processImage(FImage image) {
		mode.convolve(image);
	}

	/**
	 * 在给定x,y点返回内核
	 */
	public float responseAt(int x, int y, FImage image) {
		float sum = 0;
		final int kh = kernel.height;
		final int kw = kernel.width;
		final int hh = kh / 2;
		final int hw = kw / 2;

		for (int j = 0, jj = kh - 1; j < kh; j++, jj--) {
			for (int i = 0, ii = kw - 1; i < kw; i++, ii--) {
				final int rx = x + i - hw;
				final int ry = y + j - hh;

				sum += image.pixels[ry][rx] * kernel.pixels[jj][ii];
			}
		}
		return sum;
	}

}
