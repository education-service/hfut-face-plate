package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

/**
 * 三角过滤图像卷积
 *
 *@author wanghao
 */
public class FTriangleFilter implements ImageProcessor<FImage> {

	private boolean zeropad;
	private int filterHeight;
	private int filterWidth;

	/**
	 * 指定维度构造
	 */
	public FTriangleFilter(int filterWidth, int filterHeight, boolean zeropad) {
		super();
		this.filterWidth = filterWidth;
		this.filterHeight = filterHeight;
		this.zeropad = zeropad;
	}

	public FTriangleFilter(int filterWidth, int filterHeight) {
		this(filterWidth, filterHeight, false);
	}

	@Override
	public void processImage(FImage image) {
		convolve(image, filterWidth, filterHeight, zeropad);
	}

	/**
	 * 构建三角内核
	 */
	public static float[] createKernel1D(int width) {
		final float[] kernel = new float[width * 2 - 1];
		final float invNorm = 1f / (width * width);

		kernel[width - 1] = width * invNorm;
		for (int i = 0; i < width - 1; i++) {
			kernel[i] = (i + 1) * invNorm;
			kernel[kernel.length - i - 1] = kernel[i];
		}
		return kernel;
	}

	static void convolve(FImage image, int filterWidth, int filterHeight, boolean zeropad) {
		convolveVertical(image, image, filterHeight, zeropad);
		convolveHorizontal(image, image, filterWidth, zeropad);
	}

	static void convolveVertical(FImage dest, FImage image, int filterSize, boolean zeropad) {
		if (image.height == 0) {
			return;
		}

		final float scale = (float) (1.0 / ((double) filterSize * (double) filterSize));
		final float[] buffer = new float[image.height + filterSize];
		final int bufferOffset = filterSize;

		for (int x = 0; x < image.width; x++) {
			buffer[bufferOffset + image.height - 1] = image.pixels[image.height - 1][x];
			int y;
			for (y = image.height - 2; y >= 0; --y) {
				buffer[bufferOffset + y] = buffer[bufferOffset + y + 1] + image.pixels[y][x];
			}
			if (zeropad) {
				for (; y >= -filterSize; y--) {
					buffer[bufferOffset + y] = buffer[bufferOffset + y + 1];
				}
			} else {
				for (; y >= -filterSize; y--) {
					buffer[bufferOffset + y] = buffer[bufferOffset + y + 1] + image.pixels[0][x];
				}
			}

			for (y = -filterSize; y < image.height - filterSize; y++) {
				buffer[bufferOffset + y] = buffer[bufferOffset + y] - buffer[bufferOffset + y + filterSize];
			}
			if (!zeropad) {
				for (y = image.height - filterSize; y < image.height; ++y) {
					buffer[bufferOffset + y] = buffer[bufferOffset + y] - buffer[bufferOffset + image.height - 1]
							* (image.height - filterSize - y);
				}
			}

			for (y = -filterSize + 1; y < image.height; y++) {
				buffer[bufferOffset + y] += buffer[bufferOffset + y - 1];
			}

			for (y = dest.height - 1; y >= 0; y--) {
				dest.pixels[y][x] = scale * (buffer[bufferOffset + y] - buffer[bufferOffset + y - filterSize]);
			}
		}
	}

	static void convolveHorizontal(FImage dest, FImage image, int filterSize, boolean zeropad) {
		if (image.width == 0) {
			return;
		}

		final float scale = (float) (1.0 / ((double) filterSize * (double) filterSize));
		final float[] buffer = new float[image.width + filterSize];
		final int bufferOffset = filterSize;

		for (int y = 0; y < image.height; y++) {
			buffer[bufferOffset + image.width - 1] = image.pixels[y][image.width - 1];
			int x;
			for (x = image.width - 2; x >= 0; --x) {
				buffer[bufferOffset + x] = buffer[bufferOffset + x + 1] + image.pixels[y][x];
			}
			if (zeropad) {
				for (; x >= -filterSize; x--) {
					buffer[bufferOffset + x] = buffer[bufferOffset + x + 1];
				}
			} else {
				for (; x >= -filterSize; x--) {
					buffer[bufferOffset + x] = buffer[bufferOffset + x + 1] + image.pixels[y][0];
				}
			}

			for (x = -filterSize; x < image.width - filterSize; x++) {
				buffer[bufferOffset + x] = buffer[bufferOffset + x] - buffer[bufferOffset + x + filterSize];
			}
			if (!zeropad) {
				for (x = image.width - filterSize; x < image.width; ++x) {
					buffer[bufferOffset + x] = buffer[bufferOffset + x] - buffer[bufferOffset + image.width - 1]
							* (image.width - filterSize - x);
				}
			}

			for (x = -filterSize + 1; x < image.width; x++) {
				buffer[bufferOffset + x] += buffer[bufferOffset + x - 1];
			}

			for (x = dest.width - 1; x >= 0; x--) {
				dest.pixels[y][x] = scale * (buffer[bufferOffset + x] - buffer[bufferOffset + x - filterSize]);
			}
		}
	}

}
