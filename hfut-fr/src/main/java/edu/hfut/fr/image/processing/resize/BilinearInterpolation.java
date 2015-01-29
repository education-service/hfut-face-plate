package edu.hfut.fr.image.processing.resize;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 简单的双线性插值转换图像大小
 *
 * @author wanghao
 */
public class BilinearInterpolation implements SinglebandImageProcessor<Float, FImage> {

	protected int width;
	protected int height;
	protected float scale;

	public BilinearInterpolation(int width, int height, float scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	@Override
	public void processImage(FImage image) {
		FImage newimage = image.newInstance(width, height);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				newimage.pixels[y][x] = image.getPixelInterp(x * scale, y * scale);

		image.internalAssign(newimage);
	}

}
