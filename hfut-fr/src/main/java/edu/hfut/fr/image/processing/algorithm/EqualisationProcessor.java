package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 图像均衡处理器
 *
 * @author wanghao
 */
public class EqualisationProcessor implements SinglebandImageProcessor<Float, FImage> {

	@Override
	public void processImage(FImage image) {
		// 密度直方图
		final int[] hg = new int[256];

		// 创建直方图
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				final int i = Math.round(255 * image.pixels[r][c]);
				hg[i]++;
			}
		}

		for (int i = 1; i < 256; i++) {
			hg[i] += hg[i - 1];
		}

		final float alpha = 255f / (image.getWidth() * image.getHeight());

		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				final int i = Math.round(255 * image.pixels[r][c]);
				image.pixels[r][c] = Math.round(hg[i] * alpha) / 255.0f;
			}
		}
	}

}
