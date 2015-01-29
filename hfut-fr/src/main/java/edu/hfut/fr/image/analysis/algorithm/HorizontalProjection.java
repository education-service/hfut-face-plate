package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 *  图像的水平方向投影
 *
 * @author wanghao
 */
public class HorizontalProjection implements ImageAnalyser<FImage> {

	float[] projection;

	@Override
	public void analyseImage(FImage image) {
		projection = project(image);
	}

	public static float[] project(FImage image) {
		float[] projection = new float[image.width];

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				projection[x] += image.pixels[y][x];
			}
		}

		return projection;
	}

	/**
	 * 返回处理后的图像结果
	 */
	public float[] getProjection() {
		return projection;
	}

}
