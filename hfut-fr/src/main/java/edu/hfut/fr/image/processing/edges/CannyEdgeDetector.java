package edu.hfut.fr.image.processing.edges;

import java.util.ArrayDeque;
import java.util.Deque;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.HistogramAnalyser;
import edu.hfut.fr.image.processing.convolution.FSobel;

/**
 * Canny 边缘检测器
 *
 * @author wanghao
 */
public class CannyEdgeDetector implements SinglebandImageProcessor<Float, FImage> {

	static final float threshRatio = 0.4f;

	float lowThresh = -1;
	float highThresh = -1;
	float sigma = 1;

	/**
	 * 自动配置构造函数
	 */
	public CannyEdgeDetector() {
	}

	/**
	 * 给定sigma配置
	 */
	public CannyEdgeDetector(float sigma) {
		this.sigma = sigma;
	}

	public CannyEdgeDetector(float lowThresh, float highThresh, float sigma) {
		if (lowThresh < 0 || lowThresh > 1)
			throw new IllegalArgumentException("Low threshold must be between 0 and 1");
		if (highThresh < 0 || highThresh > 1)
			throw new IllegalArgumentException("High threshold must be between 0 and 1");
		if (highThresh < lowThresh)
			throw new IllegalArgumentException("High threshold must be bigger than the lower threshold");
		if (sigma < 0)
			throw new IllegalArgumentException("Sigma must be > 0");

		this.lowThresh = lowThresh;
		this.highThresh = highThresh;
		this.sigma = sigma;
	}

	float computeHighThreshold(FImage magnitudes) {
		final Histogram hist = HistogramAnalyser.getHistogram(magnitudes, 64);

		float cumSum = 0;
		for (int i = 0; i < 64; i++) {
			if (cumSum > 0.7 * magnitudes.width * magnitudes.height) {
				return i / 64f;
			}
			cumSum += hist.values[i];
		}

		return 1f;
	}

	@Override
	public void processImage(FImage image) {
		processImage(image, new FSobel(sigma));
	}

	/**
	 * 处理图像
	 */
	public void processImage(FImage image, FSobel sobel) {
		image.analyseWith(sobel);
		processImage(image, sobel.dx, sobel.dy);
	}

	public void processImage(FImage output, FImage dx, FImage dy) {
		final FImage tmpMags = new FImage(dx.width, dx.height);
		final FImage magnitudes = NonMaximumSuppressionTangent.computeSuppressed(dx, dy, tmpMags);
		magnitudes.normalise();

		float low = this.lowThresh;
		float high = this.highThresh;
		if (high < 0) {
			high = computeHighThreshold(tmpMags);
			low = threshRatio * high;
		}

		thresholdingTracker(magnitudes, output, low, high);
	}

	private void thresholdingTracker(FImage magnitude, FImage output, float low, float high) {
		output.zero();

		final Deque<Pixel> candidates = new ArrayDeque<Pixel>();
		for (int y = 0; y < magnitude.height; y++) {
			for (int x = 0; x < magnitude.width; x++) {
				if (magnitude.pixels[y][x] >= high && output.pixels[y][x] != 1) {
					candidates.add(new Pixel(x, y));

					while (!candidates.isEmpty()) {
						final Pixel current = candidates.pollFirst();

						if (current.x < 0 || current.x > magnitude.width || current.y < 0
								|| current.y > magnitude.height)
							continue;

						if (output.pixels[current.y][current.x] == 1)
							continue;

						if (magnitude.pixels[current.y][current.x] < low)
							continue;

						output.pixels[current.y][current.x] = 1;

						candidates.add(new Pixel(x - 1, y - 1));
						candidates.add(new Pixel(x, y - 1));
						candidates.add(new Pixel(x + 1, y - 1));
						candidates.add(new Pixel(x - 1, y));
						candidates.add(new Pixel(x + 1, y));
						candidates.add(new Pixel(x - 1, y + 1));
						candidates.add(new Pixel(x, y + 1));
						candidates.add(new Pixel(x + 1, y + 1));
					}
				}
			}
		}
	}

}
