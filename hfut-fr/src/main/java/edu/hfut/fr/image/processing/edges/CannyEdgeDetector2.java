package edu.hfut.fr.image.processing.edges;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

@Deprecated
public class CannyEdgeDetector2 implements SinglebandImageProcessor<Float, FImage> {

	private boolean complete;

	private int threshold = 128;

	private int hystThresh1 = 50;

	private int hystThresh2 = 230;

	private int kernelSize = 15;

	final float ORIENT_SCALE = 40F;
	private int height;
	private int width;
	private int picsize;
	private float[] data;
	private int derivative_mag[];
	private float magnitude[];
	private float orientation[];
	private FImage sourceImage;
	private FImage edgeImage;

	public CannyEdgeDetector2() {
		complete = false;
	}

	/**
	 * 返回结果是否完成
	 */
	public boolean isImageReady() {
		return complete;
	}

	@Override
	public void processImage(FImage image) {
		complete = false;

		final int widGaussianKernel = kernelSize;
		final int threshold = this.threshold;
		final int threshold1 = hystThresh1;
		final int threshold2 = hystThresh2;

		if (threshold < 0 || threshold > 255) {
			throw new IllegalArgumentException("The value of the threshold " + "is out of its valid range.");
		}

		if (widGaussianKernel < 3 || widGaussianKernel > 40) {
			throw new IllegalArgumentException("The value of the widGaussianKernel " + "is out of its valid range.");
		}

		width = image.getWidth();
		height = image.getHeight();
		picsize = width * height;
		sourceImage = image;

		data = new float[picsize];
		magnitude = new float[picsize];
		orientation = new float[picsize];

		final float f = 1.0F;
		canny_core(f, widGaussianKernel);
		thresholding_tracker(threshold1, threshold2);

		for (int i = 0; i < picsize; i++)
			if (data[i] > threshold)
				data[i] = 1;
			else
				data[i] = -1;

		edgeImage = new FImage(data, width, height).normalise();
		data = null;

		complete = true;

		image.internalAssign(edgeImage);
	}

	/**
	 * 显示图像
	 */
	protected void display(int[] data) {
		final FImage tmp = new FImage(width, height);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				tmp.pixels[r][c] = data[c + r * width] / 255f;
		DisplayUtilities.display(tmp);
	}

	protected void display(float[] data) {
		final FImage tmp = new FImage(width, height);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				tmp.pixels[r][c] = data[c + r * width] / 255f;
		DisplayUtilities.display(tmp);
	}

	private void canny_core(float f, int i) {
		derivative_mag = new int[picsize];
		final float af4[] = new float[i];
		final float af5[] = new float[i];
		final float af6[] = new float[i];
		data = sourceImage.clone().multiply(255.0f).getFloatPixelVector();
		int k4 = 0;
		do {
			if (k4 >= i)
				break;
			final float f1 = gaussian(k4, f);
			if (f1 <= 0.005F && k4 >= 2)
				break;
			final float f2 = gaussian(k4 - 0.5F, f);
			final float f3 = gaussian(k4 + 0.5F, f);
			final float f4 = gaussian(k4, f * 0.5F);
			af4[k4] = (f1 + f2 + f3) / 3F / (6.283185F * f * f);
			af5[k4] = f3 - f2;
			af6[k4] = 1.6F * f4 - f1;
			k4++;
		} while (true);

		final int j = k4;
		float af[] = new float[picsize];
		float af1[] = new float[picsize];
		int j1 = width - (j - 1);
		int l = width * (j - 1);
		int i1 = width * (height - (j - 1));
		for (int l4 = j - 1; l4 < j1; l4++) {
			for (int l5 = l; l5 < i1; l5 += width) {
				final int k1 = l4 + l5;
				float f8 = data[k1] * af4[0];
				float f10 = f8;
				int l6 = 1;
				int k7 = k1 - width;
				for (int i8 = k1 + width; l6 < j; i8 += width) {
					f8 += af4[l6] * (data[k7] + data[i8]);
					f10 += af4[l6] * (data[k1 - l6] + data[k1 + l6]);
					l6++;
					k7 -= width;
				}

				af[k1] = f8;
				af1[k1] = f10;
			}

		}

		float af2[] = new float[picsize];
		for (int i5 = j - 1; i5 < j1; i5++) {
			for (int i6 = l; i6 < i1; i6 += width) {
				float f9 = 0.0F;
				final int l1 = i5 + i6;
				for (int i7 = 1; i7 < j; i7++)
					f9 += af5[i7] * (af[l1 - i7] - af[l1 + i7]);

				af2[l1] = f9;
			}

		}

		af = null;
		float af3[] = new float[picsize];
		for (int j5 = k4; j5 < width - k4; j5++) {
			for (int j6 = l; j6 < i1; j6 += width) {
				float f11 = 0.0F;
				final int i2 = j5 + j6;
				int j7 = 1;
				for (int l7 = width; j7 < j; l7 += width) {
					f11 += af5[j7] * (af1[i2 - l7] - af1[i2 + l7]);
					j7++;
				}

				af3[i2] = f11;
			}

		}

		af1 = null;
		j1 = width - j;
		l = width * j;
		i1 = width * (height - j);
		for (int k5 = j; k5 < j1; k5++) {
			for (int k6 = l; k6 < i1; k6 += width) {
				final int j2 = k5 + k6;
				final int k2 = j2 - width;
				final int l2 = j2 + width;
				final int i3 = j2 - 1;
				final int j3 = j2 + 1;
				final int k3 = k2 - 1;
				final int l3 = k2 + 1;
				final int i4 = l2 - 1;
				final int j4 = l2 + 1;
				final float f6 = af2[j2];
				final float f7 = af3[j2];
				final float f12 = hypotenuse(f6, f7);
				final int k = (int) (f12 * 20D);
				derivative_mag[j2] = k >= 256 ? 255 : k;
				final float f13 = hypotenuse(af2[k2], af3[k2]);
				final float f14 = hypotenuse(af2[l2], af3[l2]);
				final float f15 = hypotenuse(af2[i3], af3[i3]);
				final float f16 = hypotenuse(af2[j3], af3[j3]);
				final float f18 = hypotenuse(af2[l3], af3[l3]);
				final float f20 = hypotenuse(af2[j4], af3[j4]);
				final float f19 = hypotenuse(af2[i4], af3[i4]);
				final float f17 = hypotenuse(af2[k3], af3[k3]);
				float f5;
				if (f6 * f7 <= 0 ? Math.abs(f6) >= Math.abs(f7) ? (f5 = Math.abs(f6 * f12)) >= Math.abs(f7 * f18
						- (f6 + f7) * f16)
						&& f5 > Math.abs(f7 * f19 - (f6 + f7) * f15) : (f5 = Math.abs(f7 * f12)) >= Math.abs(f6 * f18
						- (f7 + f6) * f13)
						&& f5 > Math.abs(f6 * f19 - (f7 + f6) * f14) : Math.abs(f6) >= Math.abs(f7) ? (f5 = Math.abs(f6
						* f12)) >= Math.abs(f7 * f20 + (f6 - f7) * f16)
						&& f5 > Math.abs(f7 * f17 + (f6 - f7) * f15) : (f5 = Math.abs(f7 * f12)) >= Math.abs(f6 * f20
						+ (f7 - f6) * f14)
						&& f5 > Math.abs(f6 * f17 + (f7 - f6) * f13)) {
					magnitude[j2] = derivative_mag[j2];
					orientation[j2] = (float) Math.toDegrees(Math.atan2(f7, f6));
				}
			}

		}

		derivative_mag = null;
		af2 = null;
		af3 = null;
	}

	/**
	 * 返回斜边的长度
	 */
	private float hypotenuse(float f, float f1) {
		if (f == 0.0F && f1 == 0.0F)
			return 0.0F;
		else
			return (float) Math.sqrt(f * f + f1 * f1);
	}

	private float gaussian(float f, float f1) {
		return (float) Math.exp((-f * f) / (2 * f1 * f1));
	}

	private void thresholding_tracker(int i, int j) {
		for (int k = 0; k < picsize; k++)
			data[k] = 0;

		for (int l = 0; l < width; l++) {
			for (int i1 = 0; i1 < height; i1++)
				if (magnitude[l + width * i1] >= i)
					follow(l, i1, j);

		}

	}

	private boolean follow(int i, int j, int k) {
		int j1 = i + 1;
		int k1 = i - 1;
		int l1 = j + 1;
		int i2 = j - 1;
		final int j2 = i + j * width;
		if (l1 >= height)
			l1 = height - 1;
		if (i2 < 0)
			i2 = 0;
		if (j1 >= width)
			j1 = width - 1;
		if (k1 < 0)
			k1 = 0;
		if (data[j2] == 0) {
			data[j2] = magnitude[j2];
			boolean flag = false;
			int l = k1;
			do {
				if (l > j1)
					break;
				int i1 = i2;
				do {
					if (i1 > l1)
						break;
					final int k2 = l + i1 * width;
					if ((i1 != j || l != i) && magnitude[k2] >= k && follow(l, i1, k)) {
						flag = true;
						break;
					}
					i1++;
				} while (true);
				if (!flag)
					break;
				l++;
			} while (true);
			return true;
		} else {
			return false;
		}
	}

	public void setSourceImage(FImage image) {
		sourceImage = image;
	}

	/**
	 * 返回边缘图像
	 */
	public FImage getEdgeImage() {
		return edgeImage;
	}

	public float[] getMagnitude() {
		return magnitude;
	}

	public float[] getOrientation() {
		return orientation;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getHystThresh1() {
		return hystThresh1;
	}

	public void setHystThresh1(int hystThresh1) {
		this.hystThresh1 = hystThresh1;
	}

	public int getHystThresh2() {
		return hystThresh2;
	}

	public void setHystThresh2(int hystThresh2) {
		this.hystThresh2 = hystThresh2;
	}

	public int getKernelSize() {
		return kernelSize;
	}

	public void setKernelSize(int kernelSize) {
		this.kernelSize = kernelSize;
	}

}
