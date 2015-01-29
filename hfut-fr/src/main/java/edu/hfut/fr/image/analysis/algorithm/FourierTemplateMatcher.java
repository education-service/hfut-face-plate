package edu.hfut.fr.image.analysis.algorithm;

import java.util.Comparator;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.util.FloatArrayStatsUtils;

import edu.hfut.fr.image.processing.algorithm.FourierCorrelation;

/**
 * 基本图像的匹配
 *
 * @author wanggang
 */
public class FourierTemplateMatcher implements ImageAnalyser<FImage> {

	/**
	 *不同算法来比较图像
	 */
	public enum Mode {
		/**
		 *计算图像和对比图像的平方和
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			public boolean scoresAscending() {
				return false;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);

				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);

				float templateNorm = templateStdDev * templateStdDev;
				float templateSum2 = templateNorm + templateMean * templateMean;

				templateNorm = templateSum2;

				double invArea = 1.0 / ((double) template.width * template.height);
				templateSum2 /= invArea;
				templateNorm = (float) Math.sqrt(templateNorm);
				templateNorm /= Math.sqrt(invArea);

				final float[][] pix = corr.pixels;

				for (int y = 0; y < corr.height; y++) {
					for (int x = 0; x < corr.width; x++) {
						double num = pix[y][x];
						double wndSum2 = 0;

						double t = sum.calculateSqSumArea(x, y, x + template.width, y + template.height);
						wndSum2 += t;

						num = wndSum2 - 2 * num + templateSum2;

						pix[y][x] = (float) num;
					}
				}
			}
		},
		/**
		 *计算图像和对比图像的标准化后的平方和
		 */
		NORM_SUM_SQUARED_DIFFERENCE {
			@Override
			public boolean scoresAscending() {
				return false;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);

				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);

				float templateNorm = templateStdDev * templateStdDev;
				float templateSum2 = templateNorm + templateMean * templateMean;

				templateNorm = templateSum2;

				double invArea = 1.0 / ((double) template.width * template.height);
				templateSum2 /= invArea;
				templateNorm = (float) Math.sqrt(templateNorm);
				templateNorm /= Math.sqrt(invArea);

				final float[][] pix = corr.pixels;

				for (int y = 0; y < corr.height; y++) {
					for (int x = 0; x < corr.width; x++) {
						double num = pix[y][x];
						double wndMean2 = 0, wndSum2 = 0;

						double t = sum.calculateSqSumArea(x, y, x + template.width, y + template.height);
						wndSum2 += t;

						num = wndSum2 - 2 * num + templateSum2;

						t = Math.sqrt(Math.max(wndSum2 - wndMean2, 0)) * templateNorm;
						num /= t;

						pix[y][x] = (float) num;
					}
				}
			}
		},
		/**
		 * Compute the score at a point as the summed product between the image
		 * and the template.
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		CORRELATION {
			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {

			}
		},
		NORM_CORRELATION {
			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);

				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);

				float templateNorm = templateStdDev * templateStdDev;
				templateNorm += templateMean * templateMean;

				double invArea = 1.0 / ((double) template.width * template.height);
				templateNorm = (float) Math.sqrt(templateNorm);
				templateNorm /= Math.sqrt(invArea);

				final float[][] pix = corr.pixels;

				for (int y = 0; y < corr.height; y++) {
					for (int x = 0; x < corr.width; x++) {
						double num = pix[y][x];
						double wndMean2 = 0, wndSum2 = 0;

						double t = sum.calculateSqSumArea(x, y, x + template.width, y + template.height);
						wndSum2 += t;

						t = Math.sqrt(Math.max(wndSum2 - wndMean2, 0)) * templateNorm;
						num /= t;

						pix[y][x] = (float) num;
					}
				}
			}
		},

		CORRELATION_COEFFICIENT {
			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedAreaTable sum = new SummedAreaTable();
				img.analyseWith(sum);

				final float templateMean = FloatArrayStatsUtils.mean(template.pixels); //TODO: cache this
				final float[][] pix = corr.pixels;

				for (int y = 0; y < corr.height; y++) {
					for (int x = 0; x < corr.width; x++) {
						double num = pix[y][x];
						double t = sum.calculateArea(x, y, x + template.width, y + template.height);

						num -= t * templateMean;

						pix[y][x] = (float) num;
					}
				}
			}
		},

		NORM_CORRELATION_COEFFICIENT {
			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);

				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);

				float templateNorm = templateStdDev;

				if (templateNorm == 0) {
					corr.fill(1);
					return;
				}

				double invArea = 1.0 / ((double) template.width * template.height);
				templateNorm /= Math.sqrt(invArea);

				final float[][] pix = corr.pixels;

				for (int y = 0; y < corr.height; y++) {
					for (int x = 0; x < corr.width; x++) {
						double num = pix[y][x];

						double t = sum.calculateSumArea(x, y, x + template.width, y + template.height);
						double wndMean2 = t * t * invArea;
						num -= t * templateMean;

						double wndSum2 = sum.calculateSqSumArea(x, y, x + template.width, y + template.height);

						t = Math.sqrt(Math.max(wndSum2 - wndMean2, 0)) * templateNorm;
						num /= t;

						pix[y][x] = (float) num;
					}
				}
			}
		};

		/**
		 * 判断对比分数为上升型还是下降型
		 */
		public abstract boolean scoresAscending();

		public abstract void processCorrelationMap(FImage img, FImage template, FImage corr);
	}

	private FourierCorrelation correlation;
	private Mode mode;
	private Rectangle searchBounds;
	private FImage responseMap;
	private int templateWidth;
	private int templateHeight;

	/**
	 * 默认的匹配构造函数
	 *
	 */
	public FourierTemplateMatcher(FImage template, Mode mode) {
		this.correlation = new FourierCorrelation(template);
		this.mode = mode;
		this.templateWidth = template.width;
		this.templateHeight = template.height;
	}

	public FourierTemplateMatcher(FImage template, Rectangle bounds, Mode mode) {
		this(template, mode);
		this.searchBounds = bounds;
	}

	/**
	 * 返回搜索结果的边框矩阵
	 */
	public Rectangle getSearchBounds() {
		return searchBounds;
	}

	/**
	 *设置搜索结果边框矩阵的值
	 */
	public void setSearchBounds(Rectangle searchBounds) {
		this.searchBounds = searchBounds;
	}

	/**
	 * 匹配图像方法
	 */
	@Override
	public void analyseImage(FImage image) {
		FImage subImage;

		if (this.searchBounds != null) {
			final int halfWidth = templateWidth / 2;
			final int halfHeight = templateHeight / 2;

			int x = (int) Math.max(searchBounds.x - halfWidth, 0);
			int width = (int) searchBounds.width + templateWidth;
			if (searchBounds.x - halfWidth < 0) {
				width += (searchBounds.x - halfWidth);
			}
			if (x + width > image.width)
				width = image.width;

			int y = (int) Math.max(searchBounds.y - halfHeight, 0);
			int height = (int) searchBounds.height + templateHeight;
			if (searchBounds.y - halfHeight < 0) {
				height += (searchBounds.y - halfHeight);
			}
			if (y + height > image.height)
				height = image.height;

			subImage = image.extractROI(x, y, width, height);
		} else {
			subImage = image.clone();
		}

		responseMap = subImage.process(correlation);
		responseMap.height = responseMap.height - correlation.template.height + 1;
		responseMap.width = responseMap.width - correlation.template.width + 1;

		mode.processCorrelationMap(subImage, correlation.template, responseMap);
	}

	/**
	 * 得到N个最佳的匹配结果.
	 *
	 */
	public FValuePixel[] getBestResponses(int numResponses) {
		Comparator<FValuePixel> comparator = mode.scoresAscending() ? FValuePixel.ReverseValueComparator.INSTANCE
				: FValuePixel.ValueComparator.INSTANCE;

		return TemplateMatcher.getBestResponses(numResponses, responseMap, getXOffset(), getYOffset(), comparator);
	}

	/**
	 *
	 * 得到XOFFset的值
	 */
	public int getXOffset() {
		final int halfWidth = templateWidth / 2;

		if (this.searchBounds == null)
			return halfWidth;
		else
			return (int) Math.max(searchBounds.x - halfWidth, halfWidth);
	}

	/**
	 */
	public int getYOffset() {
		final int halfHeight = templateHeight / 2;

		if (this.searchBounds == null)
			return halfHeight;
		else
			return (int) Math.max(searchBounds.y - halfHeight, halfHeight);
	}

	/**
	 * 得到返回的结果
	 */
	public FImage getResponseMap() {
		return responseMap;
	}

}
