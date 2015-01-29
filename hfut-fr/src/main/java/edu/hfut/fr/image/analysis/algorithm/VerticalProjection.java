package edu.hfut.fr.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * 投影图像至Y轴，竖直方向
 *
 * @author wanghao
 */
public class VerticalProjection implements ImageAnalyser<FImage> {

	float[] projection;

	@Override
	public void analyseImage(FImage image) {
		projection = project(image);
	}

	public static float[] project(FImage image) {
		float[] projection = new float[image.height];

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				projection[y] += image.pixels[y][x];
			}
		}

		return projection;
	}

	/**
	 * 得到投影
	 */
	public float[] getProjection() {
		return projection;
	}

}
