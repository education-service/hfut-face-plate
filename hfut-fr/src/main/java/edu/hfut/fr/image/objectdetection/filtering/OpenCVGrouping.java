package edu.hfut.fr.image.objectdetection.filtering;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectIntPair;

/**
 * 使用opencv方法显示检测矩阵区域
 *
 * @author wanghao
 */
public final class OpenCVGrouping implements DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> {

	public static final float DEFAULT_EPS = 0.2f;

	public static final int DEFAULT_MINIMUM_SUPPORT = 3;

	float eps;
	int minSupport;

	/**
	 *构造函数
	 */
	public OpenCVGrouping(float eps, int minSupport) {
		this.eps = eps;
		this.minSupport = minSupport;
	}

	public OpenCVGrouping(int minSupport) {
		this(DEFAULT_EPS, minSupport);
	}

	public OpenCVGrouping() {
		this(DEFAULT_EPS, DEFAULT_MINIMUM_SUPPORT);
	}

	/**
	 * 返回矩阵对象队列
	 */
	@Override
	public List<ObjectIntPair<Rectangle>> apply(List<Rectangle> input) {
		final int[] classes = new int[input.size()];
		final int nClasses = partition(input, classes);

		final Rectangle[] meanRects = new Rectangle[nClasses];
		final int[] rectCounts = new int[nClasses];
		for (int i = 0; i < nClasses; i++) {
			meanRects[i] = new Rectangle(0, 0, 0, 0);
		}

		for (int i = 0; i < classes.length; i++) {
			final int cls = classes[i];

			meanRects[cls].x += input.get(i).x;
			meanRects[cls].y += input.get(i).y;
			meanRects[cls].width += input.get(i).width;
			meanRects[cls].height += input.get(i).height;
			rectCounts[cls]++;
		}

		for (int i = 0; i < nClasses; i++) {
			final Rectangle r = meanRects[i];
			final float s = 1.0f / rectCounts[i];
			meanRects[i] = new Rectangle(Math.round(r.x * s), Math.round(r.y * s), Math.round(r.width * s),
					Math.round(r.height * s));
		}

		final List<ObjectIntPair<Rectangle>> rectList = new ArrayList<ObjectIntPair<Rectangle>>();
		for (int i = 0; i < nClasses; i++) {
			final Rectangle r1 = meanRects[i];
			final int n1 = rectCounts[i];

			if (n1 <= minSupport)
				continue;

			int j;
			for (j = 0; j < nClasses; j++) {
				final int n2 = rectCounts[j];

				if (j == i || n2 <= minSupport)
					continue;
				final Rectangle r2 = meanRects[j];

				final int dx = Math.round(r2.width * eps);
				final int dy = Math.round(r2.height * eps);

				if (i != j && r1.x >= r2.x - dx && r1.y >= r2.y - dy && r1.x + r1.width <= r2.x + r2.width + dx
						&& r1.y + r1.height <= r2.y + r2.height + dy && (n2 > Math.max(3, n1) || n1 < 3))
					break;
			}

			if (j == nClasses) {
				rectList.add(new ObjectIntPair<Rectangle>(r1, n1));
			}
		}

		return rectList;
	}

	private int partition(List<Rectangle> rects, int[] classes) {
		int numClasses = 0;

		for (int i = 0; i < rects.size(); i++) {
			boolean found = false;
			for (int j = 0; j < i; j++) {
				if (equals(rects.get(j), rects.get(i))) {
					found = true;
					classes[i] = classes[j];
				}
			}
			if (!found) {
				classes[i] = numClasses;
				numClasses++;
			}
		}

		return numClasses;
	}

	private boolean equals(Rectangle r1, Rectangle r2) {
		final float delta = eps * (Math.min(r1.width, r2.width) + Math.min(r1.height, r2.height)) * 0.5f;

		return (Math.abs(r1.x - r2.x) <= delta && Math.abs(r1.y - r2.y) <= delta
				&& Math.abs(r1.x + r1.width - r2.x - r2.width) <= delta && Math
				.abs(r1.y + r1.height - r2.y - r2.height) <= delta);
	}

}
