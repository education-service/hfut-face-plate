package edu.hfut.fr.image.processing.edges;

import gnu.trove.list.array.TFloatArrayList;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * SUSAN边缘检测算法实现
 *
 * @author wanghao
 */
@Reference(author = { "S. M. Smith" }, title = "A new class of corner finder", type = ReferenceType.Article, url = "http://users.fmrib.ox.ac.uk/~steve/susan/susan/node4.html", year = "1992", booktitle = "Proc. 3rd British Machine Vision Conference", pages = "139-148")
public class SUSANEdgeDetector implements SinglebandImageProcessor<Float, FImage> {

	private enum SUSANDetector {
		/**
		 * 简单快速SUSAN算法
		 */
		SIMPLE {
			@Override
			public FImage process(FImage img) {
				return SUSANEdgeDetector.simpleSusan(img, threshold, nmax);
			}
		},
		/**
		 * 平滑检测
		 */
		SMOOTH {
			@Override
			public FImage process(FImage img) {
				return SUSANEdgeDetector.smoothSusan(img, threshold, nmax);
			}
		},
		/**
		 * 平滑圆形检测
		 */
		CIRCULAR {
			@Override
			public FImage process(FImage img) {
				return SUSANEdgeDetector.smoothCircularSusan(img, threshold, nmax, radius);
			}
		};

		protected double threshold = 0.08;
		protected double nmax = 9;
		protected double radius = 3.4;

		public abstract FImage process(FImage img);
	}

	private SUSANDetector susan = SUSANDetector.SIMPLE;

	/**
	 * 默认构造函数
	 */
	public SUSANEdgeDetector() {
		this.susan = SUSANDetector.SIMPLE;
	}

	public SUSANEdgeDetector(SUSANDetector s, double threshold, double nmax) {
		this.susan = s;
		susan.threshold = threshold;
		susan.nmax = nmax;
	}

	public SUSANEdgeDetector(SUSANDetector s, double threshold, double nmax, double radius) {
		this.susan = s;
		susan.threshold = threshold;
		susan.nmax = nmax;
		susan.radius = radius;
	}

	@Override
	public void processImage(FImage image) {
		image.internalAssign(susan.process(image));
	}

	/**
	 * 实现最简单susan边缘检测
	 */
	public static FImage simpleSusan(FImage img, double thresh, double nmax) {
		final FImage area = new FImage(img.getWidth(), img.getHeight());

		final double globalThresh = (3.0 * nmax) / 4.0;

		for (int y = 1; y < img.getHeight() - 1; y++) {
			for (int x = 1; x < img.getWidth() - 1; x++) {
				double a = 0;
				for (int x1 = x - 1; x1 < x + 2; x1++) {
					for (int y1 = y - 1; y1 < y + 2; y1++) {
						if (Math.abs(img.getPixel(x1, y1) - img.getPixel(x, y)) < thresh)
							a++;
					}
				}

				if (a < globalThresh)
					area.setPixel(x, y, (float) (globalThresh - a));
			}
		}

		return area;
	}

	public static FImage smoothSusan(FImage img, double thresh, double nmax) {
		final FImage area = new FImage(img.getWidth(), img.getHeight());

		final double globalThresh = (3.0 * nmax) / 4.0;

		for (int y = 1; y < img.getHeight() - 1; y++) {
			for (int x = 1; x < img.getWidth() - 1; x++) {
				double a = 0;
				for (int x1 = x - 1; x1 < x + 2; x1++) {
					for (int y1 = y - 1; y1 < y + 2; y1++) {
						a += Math.exp(-Math.pow(Math.abs(img.getPixel(x1, y1) - img.getPixel(x, y)) / thresh, 6));
					}
				}

				if (a < globalThresh)
					area.setPixel(x, y, (float) (globalThresh - a));
			}
		}

		return area;
	}

	public static FImage smoothCircularSusan(FImage img, double thresh, double nmax, double radius) {
		final FImage area = new FImage(img.getWidth(), img.getHeight());
		final double globalThresh = (3.0 * nmax) / 4.0;

		final int r = (int) Math.ceil(radius);
		for (int y = r; y < img.getHeight() - r; y++) {
			for (int x = r; x < img.getWidth() - r; x++) {
				final float[] pixelValues = getPixelsInCircle(x, y, radius, img);
				double a = 0;
				for (final float f : pixelValues)
					a += Math.exp(-Math.pow(Math.abs(f - img.getPixel(x, y)) / thresh, 6));

				if (a < globalThresh)
					area.setPixel(x, y, (float) (globalThresh - a));
			}
		}

		return area;
	}

	/**
	 * 返回圆形像素点值
	 */
	private static float[] getPixelsInCircle(int cx, int cy, double r, FImage img) {
		final TFloatArrayList f = new TFloatArrayList();
		for (int i = (int) Math.ceil(cx - r); i < (int) Math.ceil(cx + r); i++) {
			final double ri = Math.sqrt(r * r - (i - cx) * (i - cx));
			for (int j = (int) Math.ceil(cy - ri); j < (int) Math.ceil(cy + ri); j++) {
				f.add(img.getPixel(i, j));
			}
		}
		return f.toArray();
	}

}
