package edu.hfut.fr.image.analysis.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.queue.BoundedPriorityQueue;

import edu.hfut.fr.image.processing.algorithm.MeanCenter;

/**
 * 图像模板匹配
 *
 * @author wanggang
 */
public class TemplateMatcher implements ImageAnalyser<FImage> {

	/**
	 * 比较图像模板的算法
	 *
	 */
	public enum Mode {
		/**
		 * 计算图像和模板平方差的和
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y,
					final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				return computeMatchScore(imageData, x, y, templateData, 0, 0, template.width, template.height);
			}

			@Override
			public final float computeMatchScore(final float[][] img, int x, int y, final float[][] template,
					final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				for (int yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float diff = (img[yy1][xx1] - template[yy2][xx2]);

						score += diff * diff;
					}
				}
				return score;
			}

			@Override
			public boolean scoresAscending() {
				return false;
			}
		},

		/**
		 * 计算图片和模板之间标准差
		 */
		NORM_SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y,
					final Object workingSpace) {
				float score = 0;
				float si = 0;
				final float st = (Float) workingSpace;

				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				final int stopX = template.width + x;
				final int stopY = template.height + y;

				for (int yy = y, j = 0; yy < stopY; yy++, j++) {
					for (int xx = x, i = 0; xx < stopX; xx++, i++) {
						float diff = (imageData[yy][xx] - templateData[j][i]);

						score += diff * diff;
						si += (imageData[yy][xx] * imageData[yy][xx]);
					}
				}

				return (float) (score / Math.sqrt(si * st));
			}

			@Override
			public final float computeMatchScore(final float[][] img, final int x, final int y,
					final float[][] template, final int templateX, final int templateY, final int templateWidth,
					final int templateHeight) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float s1 = 0;
				float s2 = 0;
				float score = 0;

				for (int yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float diff = (img[yy1][xx1] - template[yy2][xx2]);
						score += diff * diff;
						s1 += (img[yy1][xx1] * img[yy1][xx1]);
						s2 += (template[yy2][xx2] * template[yy2][xx2]);
					}
				}

				return (float) (score / Math.sqrt(s1 * s2));
			}

			@Override
			public boolean scoresAscending() {
				return false;
			}

			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;

				for (int y = 0; y < template.height; y++)
					for (int x = 0; x < template.width; x++)
						sumsq += template.pixels[y][x] * template.pixels[y][x];

				return new Float(sumsq);
			}
		},

		/**
		 * 计算图片和模板的关联度
		 */
		CORRELATION {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y,
					final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				return computeMatchScore(imageData, x, y, templateData, 0, 0, template.width, template.height);
			}

			@Override
			public float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template,
					final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float score = 0;

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				for (int yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = (img[yy1][xx1] * template[yy2][xx2]);

						score += prod;
					}
				}

				return score;
			}

			@Override
			public boolean scoresAscending() {
				return true;
			}
		},
		/**
		 * 计算关联度的标准化的值
		 */
		NORM_CORRELATION {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y,
					final Object workingSpace) {
				float score = 0;
				float si = 0;
				final float st = (Float) workingSpace;

				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				final int stopX = template.width + x;
				final int stopY = template.height + y;

				for (int yy = y, j = 0; yy < stopY; yy++, j++) {
					for (int xx = x, i = 0; xx < stopX; xx++, i++) {
						float prod = (imageData[yy][xx] * templateData[j][i]);
						score += prod;
						si += (imageData[yy][xx] * imageData[yy][xx]);
					}
				}

				return (float) (score / Math.sqrt(si * st));
			}

			@Override
			public float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template,
					final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float score = 0;
				float s1 = 0;
				float s2 = 0;

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				int xx1, xx2, yy1, yy2;
				for (yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = (img[yy1][xx1] * template[yy2][xx2]);

						s1 += (img[yy1][xx1] * img[yy1][xx1]);
						s2 += (template[yy2][xx2] * template[yy2][xx2]);

						score += prod;
					}
				}

				return (float) (score / Math.sqrt(s1 * s2));
			}

			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;

				for (int y = 0; y < template.height; y++)
					for (int x = 0; x < template.width; x++)
						sumsq += template.pixels[y][x] * template.pixels[y][x];

				return new Float(sumsq);
			}
		},
		/**
		 *计算图像和模板之间的相关系数的值
		 */
		CORRELATION_COEFFICIENT {
			@Override
			protected final float computeMatchScore(final FImage image, final FImage template, final int x,
					final int y, final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				final float templateMean = (Float) workingSpace;
				final float imgMean = MeanCenter.patchMean(imageData);

				return computeMatchScore(imageData, x, y, imgMean, templateData, 0, 0, template.width, template.height,
						templateMean);
			}

			@Override
			public final float computeMatchScore(final float[][] img, final int x, final int y,
					final float[][] template, final int templateX, final int templateY, final int templateWidth,
					final int templateHeight) {
				float imgMean = MeanCenter.patchMean(img, x, y, templateWidth, templateHeight);
				float templateMean = MeanCenter
						.patchMean(template, templateX, templateY, templateWidth, templateHeight);

				return computeMatchScore(img, x, y, imgMean, template, templateX, templateY, templateWidth,
						templateHeight, templateMean);
			}

			private final float computeMatchScore(final float[][] img, final int x, final int y, final float imgMean,
					final float[][] template, final int templateX, final int templateY, final int templateWidth,
					final int templateHeight, final float templateMean) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				for (int yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = ((img[yy1][xx1] - imgMean) * (template[yy2][xx2] - templateMean));
						score += prod;
					}
				}

				return score;
			}

			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public Float prepareWorkingSpace(FImage template) {
				return MeanCenter.patchMean(template.pixels);
			}
		},
		/**
		 *计算规范相关系数
		 */
		NORM_CORRELATION_COEFFICIENT {
			@Override
			protected final float computeMatchScore(final FImage image, final FImage template, final int x,
					final int y, final Object workingSpace) {
				final int width = template.width;
				final int height = template.height;

				float imgMean = MeanCenter.patchMean(image.pixels, x, y, width, height);

				float score = 0;
				float si = 0;
				final float st = (Float) workingSpace;

				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				for (int j = 0; j < height; j++) {
					for (int i = 0; i < width; i++) {
						float ival = imageData[j + y][i + x] - imgMean;

						float prod = (ival * templateData[j][i]);

						score += prod;

						si += (ival * ival);
					}
				}

				double norm = Math.sqrt(si * st);

				if (norm == 0)
					return 0;

				return (float) (score / norm);
			}

			@Override
			public final float computeMatchScore(final float[][] img, final int x, final int y,
					final float[][] template, final int templateX, final int templateY, final int templateWidth,
					final int templateHeight) {
				float imgMean = MeanCenter.patchMean(img, x, y, templateWidth, templateHeight);
				float templateMean = MeanCenter
						.patchMean(template, templateX, templateY, templateWidth, templateHeight);

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				float s1 = 0;
				float s2 = 0;

				for (int yy1 = y, yy2 = templateY; yy1 < stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1 = x, xx2 = templateX; xx1 < stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float ival = (img[yy1][xx1] - imgMean);
						float tval = (template[yy2][xx2] - templateMean);

						float prod = (ival * tval);

						score += prod;

						s1 += (ival * ival);
						s2 += (tval * tval);
					}
				}

				double norm = Math.sqrt(s1 * s2);

				if (norm == 0)
					return 0;

				return (float) (score / norm);
			}

			@Override
			public boolean scoresAscending() {
				return true;
			}

			@Override
			public FImage prepareTemplate(FImage template) {
				return template.process(new MeanCenter());
			}

			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;

				for (int y = 0; y < template.height; y++)
					for (int x = 0; x < template.width; x++)
						sumsq += template.pixels[y][x] * template.pixels[y][x];

				return sumsq;
			}
		};

		/**
		 *计算图片匹配的得分值
		 */
		protected abstract float computeMatchScore(final FImage image, final FImage template, final int x, final int y,
				final Object workingSpace);

		public abstract float computeMatchScore(final float[][] img, int x, int y, final float[][] template,
				final int templateX, final int templateY, final int templateWidth, final int templateHeight);

		/**
		 * 如果是高分值计算，则是高分值返回为true,小得分值于此相反
		 */
		public abstract boolean scoresAscending();

		protected FImage prepareTemplate(FImage template) {
			return template;
		}

		protected Object prepareWorkingSpace(FImage template) {
			return null;
		}
	}

	private FImage template;
	private Mode mode;
	private Object workingSpace;
	private Rectangle searchBounds;
	private FImage responseMap;

	/**
	 * 默认的匹配的模式和模板
	 */
	public TemplateMatcher(FImage template, Mode mode) {
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(this.template);
	}

	public TemplateMatcher(FImage template, Mode mode, Rectangle bounds) {
		this.searchBounds = bounds;
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(this.template);
	}

	/**
	 * 返回搜索的矩阵区域
	 */
	public Rectangle getSearchBounds() {
		return searchBounds;
	}

	/**
	 * 设置搜索的矩阵区域
	 */
	public void setSearchBounds(Rectangle searchBounds) {
		this.searchBounds = searchBounds;
	}

	@Override
	public void analyseImage(FImage image) {
		Rectangle searchSpace = null;

		if (this.searchBounds != null) {
			final int halfWidth = template.width / 2;
			final int halfHeight = template.height / 2;

			float x = Math.max(searchBounds.x - halfWidth, 0);
			x = Math.min(x, image.width - template.width);
			float width = searchBounds.width;
			if (searchBounds.x - halfWidth < 0) {
				width += (searchBounds.x - halfWidth);
			}
			if (x + width > image.width - template.width)
				width += (image.width - template.width) - (x + width);

			float y = Math.max(searchBounds.y - halfHeight, 0);
			y = Math.min(y, image.height - template.height);
			float height = searchBounds.height;
			if (searchBounds.y - halfHeight < 0) {
				height += (searchBounds.y - halfHeight);
			}
			if (y + height > image.height - template.height)
				height += (image.height - template.height) - (y + height);

			searchSpace = new Rectangle(x, y, width, height);

		} else {
			searchSpace = new Rectangle(0, 0, image.width - template.width + 1, image.height - template.height + 1);
		}

		final int scanX = (int) searchSpace.x;
		final int scanY = (int) searchSpace.y;
		final int scanWidth = (int) searchSpace.width;
		final int scanHeight = (int) searchSpace.height;

		responseMap = new FImage(scanWidth, scanHeight);
		final float[][] responseMapData = responseMap.pixels;

		for (int y = 0; y < scanHeight; y++) {
			for (int x = 0; x < scanWidth; x++) {
				responseMapData[y][x] = mode.computeMatchScore(image, template, x + scanX, y + scanY, workingSpace);
			}
		}
	}

	/**
	 *返回与模板匹配的前N个最佳匹配
	 */
	public FValuePixel[] getBestResponses(int numResponses) {
		Comparator<FValuePixel> comparator = mode.scoresAscending() ? FValuePixel.ReverseValueComparator.INSTANCE
				: FValuePixel.ValueComparator.INSTANCE;

		return getBestResponses(numResponses, responseMap, getXOffset(), getYOffset(), comparator);
	}

	public static FValuePixel[] getBestResponses(int numResponses, FImage responseMap, int offsetX, int offsetY,
			Comparator<FValuePixel> comparator) {
		BoundedPriorityQueue<FValuePixel> bestResponses = new BoundedPriorityQueue<FValuePixel>(numResponses,
				comparator);

		final float[][] responseMapData = responseMap.pixels;

		final int scanWidth = responseMap.width;
		final int scanHeight = responseMap.height;

		FValuePixel tmpPixel = new FValuePixel(0, 0, 0);
		for (int y = 0; y < scanHeight; y++) {
			for (int x = 0; x < scanWidth; x++) {
				tmpPixel.x = x + offsetX;
				tmpPixel.y = y + offsetY;
				tmpPixel.value = responseMapData[y][x];

				FValuePixel removed = bestResponses.offerItem(tmpPixel);

				if (removed == null)
					tmpPixel = new FValuePixel(0, 0, 0);
				else
					tmpPixel = removed;
			}
		}

		return bestResponses.toOrderedArray(new FValuePixel[numResponses]);
	}

	/**
	 * 返回x-offset的值
	 */
	public int getXOffset() {
		final int halfWidth = template.width / 2;

		if (this.searchBounds == null)
			return halfWidth;
		else
			return (int) Math.max(searchBounds.x, halfWidth);
	}

	/**
	 * 返回y-offset的值
	 */
	public int getYOffset() {
		final int halfHeight = template.height / 2;

		if (this.searchBounds == null)
			return halfHeight;
		else
			return (int) Math.max(searchBounds.y, halfHeight);
	}

	public FImage getResponseMap() {
		return responseMap;
	}

	public FImage getTemplate() {
		return template;
	}

	/**
	 *  测试功能主类
	 */
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/image.png"));
		FImage template = image.extractROI(100, 100, 100, 100);
		image.fill(0f);
		image.drawImage(template, 100, 100);

		TemplateMatcher matcher = new TemplateMatcher(template, Mode.CORRELATION);
		matcher.setSearchBounds(new Rectangle(100, 100, 200, 200));
		image.analyseWith(matcher);
		DisplayUtilities.display(matcher.responseMap.normalise());

		MBFImage cimg = image.toRGB();
		for (FValuePixel p : matcher.getBestResponses(10)) {
			System.out.println(p);
			cimg.drawPoint(p, RGBColour.RED, 1);
		}

		cimg.drawShape(matcher.getSearchBounds(), RGBColour.BLUE);
		cimg.drawShape(new Rectangle(100, 100, 100, 100), RGBColour.GREEN);

		DisplayUtilities.display(cimg);
	}

}
