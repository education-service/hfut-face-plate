package edu.hfut.fr.image.processing.threshold;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.FloatFloatPair;

/**
 * Otsu自适应阈值算法.
 *
 * @author Jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Nobuyuki Otsu" }, title = "A Threshold Selection Method from Gray-Level Histograms", year = "1979", journal = "Systems, Man and Cybernetics, IEEE Transactions on", pages = {
		"62", "66" }, number = "1", volume = "9", customData = {
		"keywords",
		"Displays;Gaussian distribution;Histograms;Least squares approximation;Marine vehicles;Q measurement;Radar tracking;Sea measurements;Surveillance;Target tracking",
		"doi", "10.1109/TSMC.1979.4310076", "ISSN", "0018-9472" })
public class OtsuThreshold implements ImageProcessor<FImage> {

	private static final int DEFAULT_NUM_BINS = 256;
	int numBins = DEFAULT_NUM_BINS;

	/**
	 * 默认构造函数
	 */
	public OtsuThreshold() {

	}

	/**
	 * 构造函数
	 */
	public OtsuThreshold(int numBins) {
		this.numBins = numBins;
	}

	protected static int[] makeHistogram(FImage fimg, int numBins) {
		final int[] histData = new int[numBins];

		// 计算直方图
		for (int r = 0; r < fimg.height; r++) {
			for (int c = 0; c < fimg.width; c++) {
				final int h = (int) (fimg.pixels[r][c] * (numBins - 1));
				histData[h]++;
			}
		}

		return histData;
	}

	protected static int[] makeHistogram(float[] data, int numBins, float min, float max) {
		final int[] histData = new int[numBins];

		for (int c = 0; c < data.length; c++) {
			final float d = (data[c] - min) / (max - min);
			final int h = (int) (d * (numBins - 1));
			histData[h]++;
		}

		return histData;
	}

	/**
	 * 预测给定图像的阈值
	 *
	 */
	public static float calculateThreshold(FImage img, int numBins) {
		final int[] histData = makeHistogram(img, numBins);

		// 总的像素个数
		final int total = img.getWidth() * img.getHeight();

		return computeThresholdFromHistogram(histData, total);
	}

	/**
	 * 预测给定数据的阈值
	 */
	public static float calculateThreshold(float[] data, int numBins) {
		final float min = ArrayUtils.minValue(data);
		final float max = ArrayUtils.maxValue(data);
		final int[] histData = makeHistogram(data, numBins, min, max);

		return computeThresholdFromHistogram(histData, data.length) + min;
	}

	/**
	 * 预测给定数据的阈值
	 */
	public static FloatFloatPair calculateThresholdAndVariance(float[] data, int numBins) {
		final float min = ArrayUtils.minValue(data);
		final float max = ArrayUtils.maxValue(data);
		final int[] histData = makeHistogram(data, numBins, min, max);

		final FloatFloatPair result = computeThresholdAndVarianceFromHistogram(histData, data.length);
		result.first += min;
		return result;
	}

	/**
	 * 预测给定柱状图的阈值
	 *
	 */
	public static float computeThresholdFromHistogram(int[] histData, int total) {
		return computeThresholdAndVarianceFromHistogram(histData, total).first;
	}

	/**
	 * 预测给定柱状图的阈值
	 */
	public static FloatFloatPair computeThresholdAndVarianceFromHistogram(int[] histData, int total) {
		final int numBins = histData.length;
		float sum = 0;
		for (int t = 0; t < numBins; t++)
			sum += t * histData[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		float threshold = 0;

		for (int t = 0; t < numBins; t++) {
			wB += histData[t];
			if (wB == 0)
				continue;

			wF = total - wB;
			if (wF == 0)
				break;

			sumB += (t * histData[t]);

			final float mB = sumB / wB;
			final float mF = (sum - sumB) / wF;

			final float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}

		return new FloatFloatPair(threshold / (numBins - 1), varMax / total / total);
	}

	@Override
	public void processImage(FImage image) {
		final float threshold = calculateThreshold(image, numBins);

		image.threshold(threshold);
	}

}
