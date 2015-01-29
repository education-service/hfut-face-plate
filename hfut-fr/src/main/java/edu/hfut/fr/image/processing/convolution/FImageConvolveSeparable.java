package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 可分离的卷积图像处理器
 *
 * @author wanghao
 */
public class FImageConvolveSeparable implements SinglebandImageProcessor<Float, FImage> {

	float[] hkernel;
	float[] vkernel;

	/**
	 * 分开定义水平和竖直内核
	 *
	 */
	public FImageConvolveSeparable(float[] hkernel, float[] vkernel) {
		this.hkernel = hkernel;
		this.vkernel = vkernel;
	}

	/**
	 * 定义内核
	 *
	 */
	public FImageConvolveSeparable(float[] kernel) {
		this.hkernel = kernel;
		this.vkernel = kernel;
	}

	@Override
	public void processImage(FImage image) {
		if (hkernel != null)
			convolveHorizontal(image, hkernel);
		if (vkernel != null)
			convolveVertical(image, vkernel);
	}

	/*
	 * 计算内核数组卷积
	 */
	protected static void convolveBuffer(float[] buffer, float[] kernel) {
		final int l = buffer.length - kernel.length;
		for (int i = 0; i < l; i++) {
			float sum = 0.0f;

			for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
				sum += buffer[i + j] * kernel[jj];

			buffer[i] = sum;
		}
	}

	/**
	 * 计算图像水平方向卷积
	 */
	public static void convolveHorizontal(FImage image, float[] kernel) {
		final int halfsize = kernel.length / 2;

		final float buffer[] = new float[image.width + kernel.length];

		for (int r = 0; r < image.height; r++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[r][0];
			for (int i = 0; i < image.width; i++)
				buffer[halfsize + i] = image.pixels[r][i];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.width + i] = image.pixels[r][image.width - 1];

			final int l = buffer.length - kernel.length;
			for (int i = 0; i < l; i++) {
				float sum = 0.0f;

				for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
					sum += buffer[i + j] * kernel[jj];

				buffer[i] = sum;
			}

			for (int c = 0; c < image.width; c++)
				image.pixels[r][c] = buffer[c];
		}
	}

	/**
	 * 计算图像竖直方向卷积
	 *
	 */
	public static void convolveVertical(FImage image, float[] kernel) {
		final int halfsize = kernel.length / 2;

		final float buffer[] = new float[image.height + kernel.length];

		for (int c = 0; c < image.width; c++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[0][c];
			for (int i = 0; i < image.height; i++)
				buffer[halfsize + i] = image.pixels[i][c];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.height + i] = image.pixels[image.height - 1][c];

			final int l = buffer.length - kernel.length;
			for (int i = 0; i < l; i++) {
				float sum = 0.0f;

				for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
					sum += buffer[i + j] * kernel[jj];

				buffer[i] = sum;
			}

			for (int r = 0; r < image.height; r++)
				image.pixels[r][c] = buffer[r];
		}
	}

	/**
	 * 快速计算3*3内核的卷积
	 *
	 */
	public static void fastConvolve3(FImage source, FImage dest, float[] kx, float[] ky, float[] buffer) {
		final int dst_width = source.width - 2;

		if (kx == null)
			kx = new float[] { 0, 1, 0 };
		if (ky == null)
			ky = new float[] { 0, 1, 0 };

		if (buffer == null || buffer.length < source.width)
			buffer = new float[source.width];

		for (int y = 0; y <= source.height - 3; y++) {
			final float[] src = source.pixels[y];
			final float[] src2 = source.pixels[y + 1];
			final float[] src3 = source.pixels[y + 2];

			for (int x = 0; x < source.width; x++) {
				buffer[x] = ky[0] * src[x] + ky[1] * src2[x] + ky[2] * src3[x];
			}

			for (int x = 0; x < dst_width; x++) {
				dest.pixels[y][x] = kx[0] * buffer[x] + kx[1] * buffer[x + 1] + kx[2] * buffer[x + 2];
			}
		}
	}

}
