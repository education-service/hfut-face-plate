package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * 分析图像，建立直方图
 *
 * @author wanghao
 */
public class HistogramAnalyser implements ImageAnalyser<FImage> {

	private int nbins;

	private Histogram histogram;

	public HistogramAnalyser(int nbins) {
		this.nbins = nbins;
	}

	@Override
	public void analyseImage(FImage image) {
		this.histogram = new Histogram(nbins);
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				int bin = (int) (image.pixels[r][c] * nbins);
				if (bin > (nbins - 1))
					bin = nbins - 1;
				histogram.values[bin]++;
			}
		}
	}

	public Histogram getHistogram() {
		return histogram;
	}

	/**
	 * 快速建立图像直方图
	 */
	public static Histogram getHistogram(FImage image, int nbins) {
		final HistogramAnalyser p = new HistogramAnalyser(nbins);
		image.analyseWith(p);
		return p.getHistogram();
	}

}
