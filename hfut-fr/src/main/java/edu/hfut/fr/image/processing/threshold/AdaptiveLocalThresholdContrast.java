package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.algorithm.FilterSupport;
import edu.hfut.fr.image.processing.algorithm.MinMaxAnalyser;

/**
 * 使用局部数据对比来调整局部阈值
 *
 *@author jimbo
 */
public class AdaptiveLocalThresholdContrast extends AbstractLocalThreshold {

	public AdaptiveLocalThresholdContrast(int size) {
		super(size);
	}

	public AdaptiveLocalThresholdContrast(int size_x, int size_y) {
		super(size_x, size_y);
	}

	@Override
	public void processImage(FImage image) {
		final MinMaxAnalyser minimax = new MinMaxAnalyser(FilterSupport.createBlockSupport(sizeX, sizeY));

		final float[][] minpix = minimax.min.pixels;
		final float[][] maxpix = minimax.max.pixels;
		final float[][] ipix = image.pixels;

		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				ipix[y][x] = (ipix[y][x] - minpix[y][x]) > (maxpix[y][x] - ipix[y][x]) ? 1 : 0;
	}

}
