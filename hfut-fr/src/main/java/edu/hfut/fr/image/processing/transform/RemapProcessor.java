package edu.hfut.fr.image.processing.transform;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.algorithm.ImageInterpolation;
import edu.hfut.fr.image.analysis.algorithm.ImageInterpolation.Interpolator;

/**
 * 使用给定参数来转化图像
 *
 * @author Jimbo
 */
public class RemapProcessor implements SinglebandImageProcessor<Float, FImage> {

	ImageInterpolation interpolation;
	FImage xords;
	FImage yords;

	/**
	 * 构造函数
	 */
	public RemapProcessor(FImage xords, FImage yords) {
		this(xords, yords, ImageInterpolation.InterpolationType.BILINEAR);
	}

	/**
	 * 构造函数
	 */
	public RemapProcessor(FImage xords, FImage yords, Interpolator interpolator) {
		this.interpolation = new ImageInterpolation(interpolator);
		this.xords = xords;
		this.yords = yords;
	}

	@Override
	public void processImage(FImage image) {
		final FImage out = remap(image, xords, yords, interpolation);
		image.internalAssign(out);
	}

	/**
	 * 使用给定的参数转换图像
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords, Interpolator interpolator) {
		return remap(in, xords, yords, new ImageInterpolation(interpolator));
	}

	/**
	 * 使用给定的参数转换图像
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords, ImageInterpolation interpolation) {
		return remap(in, new FImage(xords.width, xords.height), xords, yords, interpolation);
	}

	/**
	 * 使用给定的参数转换图像
	 * 将结果写入out中
	 */
	public static FImage remap(FImage in, FImage out, FImage xords, FImage yords, ImageInterpolation interpolation) {
		final int width = Math.min(xords.width, out.width);
		final int height = Math.min(xords.height, out.height);

		interpolation.analyseImage(in);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				out.pixels[y][x] = interpolation.getPixelInterpolated(xords.pixels[y][x], yords.pixels[y][x]);
			}
		}
		return out;
	}

	/**
	 * 使用给定的参数转换图像
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords) {
		return remap(in, xords, yords, new ImageInterpolation(ImageInterpolation.InterpolationType.BILINEAR));
	}

	/**
	 * 使用给定的参数转换图像，将结果写入out中
	 */
	public static FImage remap(FImage in, FImage out, FImage xords, FImage yords) {
		return remap(in, out, xords, yords, new ImageInterpolation(ImageInterpolation.InterpolationType.BILINEAR));
	}

}
