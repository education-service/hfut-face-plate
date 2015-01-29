package edu.hfut.fr.image.processing.resize;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.algorithm.ImageInterpolation;

/**
 * 双立方插值来调整图像
 *
 * @author wanghao
 */
public class BicubicInterpolation implements SinglebandImageProcessor<Float, FImage> {

	protected int width;
	protected int height;
	protected float scale;

	public BicubicInterpolation(int width, int height, float scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	@Override
	public void processImage(FImage image) {
		final FImage newimage = image.newInstance(width, height);

		final float[][] working = new float[4][4];

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				newimage.pixels[y][x] = ImageInterpolation.InterpolationType.BICUBIC.interpolate(x * scale, y * scale,
						image, working);

		image.internalAssign(newimage);
	}

}
