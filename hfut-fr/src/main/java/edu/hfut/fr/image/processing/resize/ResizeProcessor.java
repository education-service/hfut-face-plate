package edu.hfut.fr.image.processing.resize;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import edu.hfut.fr.image.processing.resize.filters.TriangleFilter;

/**
 * 图像处理中调整图片大小的方法
 *
 * @author wanggang
 */
@Reference(type = ReferenceType.Incollection, author = { "Schumacher, Dale" }, title = "Graphics Gems III", year = "1992", pages = {
		"8", "", "16" }, chapter = "General Filtered Image Rescaling", url = "http://dl.acm.org/citation.cfm?id=130745.130747", editor = { "Kirk, David" }, publisher = "Academic Press Professional, Inc.", customData = {
		"isbn", "0-12-409671-9", "numpages", "9", "acmid", "130747", "address", "San Diego, CA, USA" })
public class ResizeProcessor implements SinglebandImageProcessor<Float, FImage> {

	/**
	 * 调整大小的模式
	 */
	public static enum Mode {
		DOUBLE, HALF, SCALE, ASPECT_RATIO, FIT, MAX, MAX_AREA, NONE,
	}

	private Mode mode = null;

	private float amount = 0;

	private float newX;

	private float newY;

	private ResizeFilterFunction filterFunction;

	public static final ResizeFilterFunction DEFAULT_FILTER = TriangleFilter.INSTANCE;

	public ResizeProcessor(Mode mode) {
		this.mode = mode;
		this.filterFunction = DEFAULT_FILTER;
	}

	public ResizeProcessor(float amount, ResizeFilterFunction ff) {
		this.mode = Mode.SCALE;
		this.amount = amount;
		this.filterFunction = ff;
	}

	public ResizeProcessor(float newX, float newY, ResizeFilterFunction ff) {
		this.mode = Mode.ASPECT_RATIO;
		this.newX = newX;
		this.newY = newY;
		this.filterFunction = ff;
	}

	public ResizeProcessor(float amount) {
		this(amount, DEFAULT_FILTER);
	}

	public ResizeProcessor(float newX, float newY) {
		this(newX, newY, DEFAULT_FILTER);
	}

	public ResizeProcessor(int maxSize) {
		this.mode = Mode.MAX;
		this.newX = maxSize;
		this.newY = maxSize;
		this.filterFunction = DEFAULT_FILTER;
	}

	public ResizeProcessor(int maxSizeArea, boolean area) {
		this.mode = area ? Mode.MAX_AREA : Mode.MAX;
		this.newX = maxSizeArea;
		this.newY = maxSizeArea;
	}

	public ResizeProcessor(int newX, int newY, boolean aspectRatio) {
		this(newX, newY, DEFAULT_FILTER);

		if (aspectRatio)
			this.mode = Mode.ASPECT_RATIO;
		else
			this.mode = Mode.FIT;
	}

	public ResizeProcessor(int newX, int newY, boolean aspectRatio, ResizeFilterFunction filterf) {
		this(newX, newY, filterf);

		if (aspectRatio)
			this.mode = Mode.ASPECT_RATIO;
		else
			this.mode = Mode.FIT;
	}

	@Override
	public void processImage(FImage image) {
		switch (this.mode) {
		case DOUBLE:
			internalDoubleSize(image);
			break;
		case HALF:
			internalHalfSize(image);
			break;
		case FIT:
			zoomInplace(image, (int) newX, (int) newY, filterFunction);
			break;
		case SCALE:
			newX = image.width * amount;
			newY = image.height * amount;
		case ASPECT_RATIO:
			resample(image, (int) newX, (int) newY, true, filterFunction);
			break;
		case MAX:
			resizeMax(image, (int) newX, filterFunction);
			break;
		case MAX_AREA:
			resizeMaxArea(image, (int) newX, filterFunction);
			break;
		case NONE:
			return;
		default:
			zoomInplace(image, (int) newX, (int) newY, this.filterFunction);
		}
	}

	/**
	 * 设置滤波器方法
	 *
	 */
	public void setFilterFunction(ResizeFilterFunction filterFunction) {
		this.filterFunction = filterFunction;
	}

	/**
	 * 调整图像大小
	 *
	 */
	public static FImage resizeMax(FImage image, int maxDim, ResizeFilterFunction filterf) {
		final int width = image.width;
		final int height = image.height;

		int newWidth, newHeight;
		if (width < maxDim && height < maxDim) {
			return image;
		} else if (width < height) {
			newHeight = maxDim;
			final float resizeRatio = ((float) maxDim / (float) height);
			newWidth = (int) (width * resizeRatio);
		} else {
			newWidth = maxDim;
			final float resizeRatio = ((float) maxDim / (float) width);
			newHeight = (int) (height * resizeRatio);
		}

		zoomInplace(image, newWidth, newHeight, filterf);

		return image;
	}

	/**
	 * 调整图像大小
	 */
	public static FImage resizeMaxArea(FImage image, int maxArea, ResizeFilterFunction filterf) {
		final int width = image.width;
		final int height = image.height;
		final int area = width * height;

		if (area < maxArea) {
			return image;
		} else {
			final float whRatio = width / height;
			final int newWidth = (int) Math.sqrt(maxArea * whRatio);
			final int newHeight = maxArea / newWidth;

			zoomInplace(image, newWidth, newHeight, filterf);

			return image;
		}
	}

	/**
	 * 调整图像大小
	 */
	public static FImage resizeMax(FImage image, int maxDim) {
		final int width = image.width;
		final int height = image.height;

		int newWidth, newHeight;
		if (width < maxDim && height < maxDim) {
			return image;
		} else if (width < height) {
			newHeight = maxDim;
			final float resizeRatio = ((float) maxDim / (float) height);
			newWidth = (int) (width * resizeRatio);
		} else {
			newWidth = maxDim;
			final float resizeRatio = ((float) maxDim / (float) width);
			newHeight = (int) (height * resizeRatio);
		}

		zoomInplace(image, newWidth, newHeight);

		return image;
	}

	/**
	 * 调整图像大小
	 */
	public static FImage resizeMaxArea(FImage image, int maxArea) {
		final int width = image.width;
		final int height = image.height;
		final int area = width * height;

		if (area < maxArea) {
			return image;
		} else {
			final float whRatio = width / height;
			final int newWidth = (int) Math.sqrt(maxArea * whRatio);
			final int newHeight = maxArea / newWidth;

			zoomInplace(image, newWidth, newHeight);

			return image;
		}
	}

	/**
	 * 双倍大小
	 */
	public static <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> I doubleSize(I image) {
		return image.process(new ResizeProcessor(Mode.DOUBLE));
	}

	/**
	 * 双倍大小
	 */
	public static FImage doubleSize(FImage image) {
		int nheight, nwidth;
		float im[][], tmp[][];
		FImage newimage;

		nheight = 2 * image.height - 2;
		nwidth = 2 * image.width - 2;
		newimage = new FImage(nwidth, nheight);
		im = image.pixels;
		tmp = newimage.pixels;

		for (int y = 0; y < image.height - 1; y++) {
			for (int x = 0; x < image.width - 1; x++) {
				final int y2 = 2 * y;
				final int x2 = 2 * x;
				tmp[y2][x2] = im[y][x];
				tmp[y2 + 1][x2] = 0.5f * (im[y][x] + im[y + 1][x]);
				tmp[y2][x2 + 1] = 0.5f * (im[y][x] + im[y][x + 1]);
				tmp[y2 + 1][x2 + 1] = 0.25f * (im[y][x] + im[y + 1][x] + im[y][x + 1] + im[y + 1][x + 1]);
			}
		}
		return newimage;
	}

	protected static void internalDoubleSize(FImage image) {
		image.internalAssign(doubleSize(image));
	}

	/**
	 * 一半大小
	 */
	public static <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> I halfSize(I image) {
		return image.process(new ResizeProcessor(Mode.HALF));
	}

	/**
	 * 一半大小
	 */
	public static FImage halfSize(FImage image) {
		int newheight, newwidth;
		float im[][], tmp[][];
		FImage newimage;

		newheight = image.height / 2;
		newwidth = image.width / 2;
		newimage = new FImage(newwidth, newheight);
		im = image.pixels;
		tmp = newimage.pixels;

		for (int y = 0, yi = 0; y < newheight; y++, yi += 2) {
			for (int x = 0, xi = 0; x < newwidth; x++, xi += 2) {
				tmp[y][x] = im[yi][xi];
			}
		}

		return newimage;
	}

	protected static void internalHalfSize(FImage image) {
		image.internalAssign(halfSize(image));
	}

	/**
	 * 指定大小
	 */
	public static FImage resample(FImage in, int newX, int newY) {
		return resample(in.clone(), newX, newY, false);
	}

	/**
	 * 指定大小
	 */
	public static FImage resample(FImage in, int newX, int newY, boolean aspect) {

		int nx = newX;
		int ny = newY;
		if (aspect) {
			if (ny > nx)
				nx = (int) Math.round((in.width * ny) / (double) in.height);
			else
				ny = (int) Math.round((in.height * nx) / (double) in.width);
		}

		zoomInplace(in, nx, ny);
		return in;
	}

	/**
	 * 指定大小
	 */
	public static FImage resample(FImage in, int newX, int newY, boolean aspect, ResizeFilterFunction filterf) {

		int nx = newX;
		int ny = newY;
		if (aspect) {
			if (ny > nx)
				nx = (int) Math.round((in.width * ny) / (double) in.height);
			else
				ny = (int) Math.round((in.height * nx) / (double) in.width);
		}

		zoomInplace(in, nx, ny, filterf);
		return in;
	}

	/**
	 * 变焦
	 *
	 *
	 */
	static class PixelContribution {
		int pixel;

		double weight;
	}

	/**
	 * 变焦
	 *
	 */
	static class PixelContributions {
		int numberOfContributors;

		PixelContribution[] contributions;
	}

	/**
	 * 计算滤波器权重
	 */
	private static void calc_x_contrib(PixelContributions contribX, double xscale, double fwidth, int dstwidth,
			int srcwidth, ResizeFilterFunction filterf, int i) {
		double width;
		double fscale;
		double center;
		double weight;

		if (xscale < 1.0) {

			width = fwidth / xscale;
			fscale = 1.0 / xscale;

			if (width <= .5) {

				width = .5 + 1.0e-6;
				fscale = 1.0;
			}

			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) (width * 2.0 + 1.0)];

			center = i / xscale;
			final int left = (int) Math.ceil(center - width);// Note: Assumes
																// width <= .5
			final int right = (int) Math.floor(center + width);

			double density = 0.0;

			for (int j = left; j <= right; j++) {
				weight = center - j;
				weight = filterf.filter(weight / fscale) / fscale;
				int n;
				if (j < 0) {
					n = -j;
				} else if (j >= srcwidth) {
					n = (srcwidth - j) + srcwidth - 1;
				} else {
					n = j;
				}

				/**/
				if (n >= srcwidth) {
					n = n % srcwidth;
				} else if (n < 0) {
					n = srcwidth - 1;
				}
				/**/

				final int k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;

				density += weight;

			}

			if ((density != 0.0) && (density != 1.0)) {

				density = 1.0 / density;
				for (int k = 0; k < contribX.numberOfContributors; k++) {
					contribX.contributions[k].weight *= density;
				}
			}
		} else {
			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) (fwidth * 2.0 + 1.0)];

			center = i / xscale;
			final int left = (int) Math.ceil(center - fwidth);
			final int right = (int) Math.floor(center + fwidth);

			for (int j = left; j <= right; j++) {
				weight = center - j;
				weight = filterf.filter(weight);

				int n;
				if (j < 0) {
					n = -j;
				} else if (j >= srcwidth) {
					n = (srcwidth - j) + srcwidth - 1;
				} else {
					n = j;
				}

				/**/
				if (n >= srcwidth) {
					n = n % srcwidth;
				} else if (n < 0) {
					n = srcwidth - 1;
				}
				/**/

				final int k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;
			}
		}
	}

	/**
	 * 调整图片大小
	 */
	public static FImage zoomInplace(FImage in, int newX, int newY) {
		final ResizeFilterFunction filter = DEFAULT_FILTER;
		return zoomInplace(in, newX, newY, filter);
	}

	/**
	 * 调整图片大小
	 */
	public static FImage zoomInplace(FImage in, int newX, int newY, ResizeFilterFunction filterf) {
		final FImage dst = new FImage(newX, newY);
		zoom(in, dst, filterf);
		in.internalAssign(dst);
		return in;
	}

	/**
	 * 调整图片大小
	 */
	public static FImage zoom(FImage in, FImage dst, ResizeFilterFunction filterf) {
		final int dstWidth = dst.getWidth();
		final int dstHeight = dst.getHeight();

		final int srcWidth = in.getWidth();
		final int srcHeight = in.getHeight();

		final double xscale = (double) dstWidth / (double) srcWidth;
		final double yscale = (double) dstHeight / (double) srcHeight;

		final float[] work = new float[in.height];

		final PixelContributions[] contribY = new PixelContributions[dstHeight];
		for (int i = 0; i < contribY.length; i++) {
			contribY[i] = new PixelContributions();
		}

		final float maxValue = in.max();

		// TODO
		final double fwidth = filterf.getSupport();
		if (yscale < 1.0) {
			double width = fwidth / yscale;
			double fscale = 1.0 / yscale;

			if (width <= .5) {

				width = .5 + 1.0e-6;
				fscale = 1.0;
			}

			for (int i = 0; i < dstHeight; i++) {
				contribY[i].contributions = new PixelContribution[(int) (width * 2.0 + 1)];
				contribY[i].numberOfContributors = 0;

				final double center = i / yscale;
				final int left = (int) Math.ceil(center - width);
				final int right = (int) Math.floor(center + width);

				double density = 0.0;

				for (int j = left; j <= right; j++) {
					double weight = center - j;
					weight = filterf.filter(weight / fscale) / fscale;
					int n;
					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					/**/
					if (n >= srcHeight) {
						n = n % srcHeight;
					} else if (n < 0) {
						n = srcHeight - 1;
					}
					/**/

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;

					density += weight;
				}

				if ((density != 0.0) && (density != 1.0)) {

					density = 1.0 / density;
					for (int k = 0; k < contribY[i].numberOfContributors; k++) {
						contribY[i].contributions[k].weight *= density;
					}
				}
			}
		} else {
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i].contributions = new PixelContribution[(int) (fwidth * 2 + 1)];
				contribY[i].numberOfContributors = 0;

				final double center = i / yscale;
				final double left = Math.ceil(center - fwidth);
				final double right = Math.floor(center + fwidth);
				for (int j = (int) left; j <= right; ++j) {
					double weight = center - j;
					weight = filterf.filter(weight);
					int n;
					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					/**/
					if (n >= srcHeight) {
						n = n % srcHeight;
					} else if (n < 0) {
						n = srcHeight - 1;
					}
					/**/

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for (int xx = 0; xx < dstWidth; xx++) {
			final PixelContributions contribX = new PixelContributions();
			calc_x_contrib(contribX, xscale, fwidth, dst.width, in.width, filterf, xx);

			for (int k = 0; k < srcHeight; k++) {
				double weight = 0.0;
				boolean bPelDelta = false;

				final double pel = in.pixels[k][contribX.contributions[0].pixel];
				for (int j = 0; j < contribX.numberOfContributors; j++) {
					final double pel2 = j == 0 ? pel : in.pixels[k][contribX.contributions[j].pixel];
					if (pel2 != pel) {
						bPelDelta = true;
					}
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				} else if (weight > maxValue) {
					weight = maxValue;
				}

				work[k] = (float) weight;
			}

			for (int i = 0; i < dstHeight; i++) {
				double weight = 0.0;
				boolean bPelDelta = false;
				final double pel = work[contribY[i].contributions[0].pixel];

				for (int j = 0; j < contribY[i].numberOfContributors; j++) {
					final double pel2 = j == 0 ? pel : work[contribY[i].contributions[j].pixel];
					if (pel2 != pel) {
						bPelDelta = true;
					}
					weight += pel2 * contribY[i].contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				} else if (weight > maxValue) {
					weight = maxValue;
				}

				dst.pixels[i][xx] = (float) weight;
			}
		}

		return dst;
	}

	/**
	 * 抽取图像一部分加入到另一幅图中
	 */
	public static FImage zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect) {
		return zoom(in, inRect, dst, dstRect, DEFAULT_FILTER);
	}

	/**
	 * 抽取图像一部分加入到另一幅图中
	 */
	public static FImage zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect, ResizeFilterFunction filterf) {

		if (!in.getBounds().isInside(inRect) || !dst.getBounds().isInside(dstRect))
			throw new IllegalArgumentException("Bad bounds");

		double xscale, yscale;
		int n;
		double center, left, right;
		double width, fscale;
		double weight;
		boolean bPelDelta;
		float pel, pel2;
		PixelContributions contribX;

		final FImage src = in;
		final int srcX = (int) inRect.x;
		final int srcY = (int) inRect.y;
		final int srcWidth = (int) inRect.width;
		final int srcHeight = (int) inRect.height;

		final int dstX = (int) dstRect.x;
		final int dstY = (int) dstRect.y;
		final int dstWidth = (int) dstRect.width;
		final int dstHeight = (int) dstRect.height;

		final float maxValue = in.max();

		final Float[] work = new Float[srcHeight];

		xscale = (double) dstWidth / (double) srcWidth;

		final PixelContributions[] contribY = new PixelContributions[dstHeight];

		yscale = (double) dstHeight / (double) srcHeight;
		final double fwidth = filterf.getSupport();

		if (yscale < 1.0) {
			width = fwidth / yscale;
			fscale = 1.0 / yscale;
			double density = 0;
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round(width * 2 + 1)];

				center = i / yscale;
				left = Math.ceil(center - width);
				right = Math.floor(center + width);
				for (int j = (int) left; j <= right; ++j) {
					weight = center - j;
					weight = filterf.filter(weight / fscale) / fscale;

					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
					density += weight;
				}

				if ((density != 0.0) && (density != 1.0)) {

					density = 1.0 / density;
					for (int k = 0; k < contribY[i].numberOfContributors; k++) {
						contribY[i].contributions[k].weight *= density;
					}
				}
			}
		} else {
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round(fwidth * 2 + 1)];

				center = i / yscale;
				left = Math.ceil(center - fwidth);
				right = Math.floor(center + fwidth);
				for (int j = (int) left; j <= right; ++j) {
					weight = center - j;
					weight = filterf.filter(weight);

					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for (int xx = 0; xx < dstWidth; xx++) {
			contribX = new PixelContributions();
			calc_x_contrib(contribX, xscale, fwidth, dstWidth, srcWidth, filterf, xx);

			for (int k = 0; k < srcHeight; ++k) {
				weight = 0.0;
				bPelDelta = false;

				pel = src.pixels[k + srcY][contribX.contributions[0].pixel + srcX];

				for (int j = 0; j < contribX.numberOfContributors; ++j) {
					pel2 = src.pixels[k + srcY][contribX.contributions[j].pixel + srcX];
					if (pel2 != pel)
						bPelDelta = true;
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255f) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				} else if (weight > maxValue) {
					weight = maxValue;
				}

				work[k] = (float) weight;
			}

			for (int i = 0; i < dstHeight; ++i) {
				weight = 0.0;
				bPelDelta = false;
				pel = work[contribY[i].contributions[0].pixel];

				for (int j = 0; j < contribY[i].numberOfContributors; ++j) {
					pel2 = work[contribY[i].contributions[j].pixel];
					if (pel2 != pel)
						bPelDelta = true;
					weight += pel2 * contribY[i].contributions[j].weight;
				}

				weight = bPelDelta ? Math.round(weight * 255f) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				} else if (weight > maxValue) {
					weight = maxValue;
				}

				dst.pixels[i + dstY][xx + dstX] = (float) weight;

			}
		}

		return dst;
	}

}
