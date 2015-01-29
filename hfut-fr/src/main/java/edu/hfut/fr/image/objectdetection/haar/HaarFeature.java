package edu.hfut.fr.image.objectdetection.haar;

import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * 描述haar特征类
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Viola, P.", "Jones, M." }, title = "Rapid object detection using a boosted cascade of simple features", year = "2001", booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on", pages = {
		" I", "511 ", " I", "518 vol.1" }, number = "", volume = "1", customData = {
		"keywords",
		" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
		"doi", "10.1109/CVPR.2001.990517", "ISSN", "1063-6919 " })
public abstract class HaarFeature {

	/**
	 *特征矩阵范围
	 */
	public WeightedRectangle[] rects;

	private final float correctionFactor;
	protected WeightedRectangle[] cachedRects;

	/**
	 * 构造新的特征
	 */
	private HaarFeature(WeightedRectangle[] rects, final float correctionFactor) {
		this.rects = rects;
		this.correctionFactor = correctionFactor;

		cachedRects = new WeightedRectangle[rects.length];
		for (int i = 0; i < cachedRects.length; i++) {
			cachedRects[i] = new WeightedRectangle(0, 0, 0, 0, 0);
		}
	}

	final void updateCaches(StageTreeClassifier cascade) {
		setScale(cascade.cachedScale, cascade.cachedInvArea);
	}

	/**
	 *设置当前检维度
	 */
	public final void setScale(float scale, float invArea) {
		double sum0 = 0;
		double area0 = 0;

		int base_w = Integer.MAX_VALUE;
		int base_h = Integer.MAX_VALUE;
		int new_base_w = 0;
		int new_base_h = 0;
		int kx;
		int ky;
		boolean flagx = false;
		boolean flagy = false;
		int x0 = 0;
		int y0 = 0;

		final WeightedRectangle firstArea = rects[0];
		for (final WeightedRectangle r : rects) {
			if ((r.width - 1) >= 0) {
				base_w = Math.min(base_w, (r.width - 1));
			}
			if ((r.x - firstArea.x - 1) >= 0) {
				base_w = Math.min(base_w, (r.x - firstArea.x - 1));
			}
			if ((r.height - 1) >= 0) {
				base_h = Math.min(base_h, (r.height - 1));
			}
			if ((r.y - firstArea.y - 1) >= 0) {
				base_h = Math.min(base_h, (r.y - firstArea.y - 1));
			}
		}

		base_w += 1;
		base_h += 1;
		kx = firstArea.width / base_w;
		ky = firstArea.height / base_h;

		if (kx <= 0) {
			flagx = true;
			new_base_w = Math.round(firstArea.width * scale) / kx;
			x0 = Math.round(firstArea.x * scale);
		}

		if (ky <= 0) {
			flagy = true;
			new_base_h = Math.round(firstArea.height * scale) / ky;
			y0 = Math.round(firstArea.y * scale);
		}

		for (int k = 0; k < rects.length; k++) {
			final WeightedRectangle r = rects[k];
			int x;
			int y;
			int width;
			int height;
			float correction_ratio;

			if (flagx) {
				x = (r.x - firstArea.x) * new_base_w / base_w + x0;
				width = r.width * new_base_w / base_w;
			} else {
				x = Math.round(r.x * scale);
				width = Math.round(r.width * scale);
			}

			if (flagy) {
				y = (r.y - firstArea.y) * new_base_h / base_h + y0;
				height = r.height * new_base_h / base_h;
			} else {
				y = Math.round(r.y * scale);
				height = Math.round(r.height * scale);
			}

			correction_ratio = correctionFactor * invArea;

			cachedRects[k].weight = (rects[k].weight * correction_ratio);
			cachedRects[k].x = x;
			cachedRects[k].y = y;
			cachedRects[k].width = width;
			cachedRects[k].height = height;

			if (k == 0) {
				area0 = width * height;
			} else {
				sum0 += cachedRects[k].weight * width * height;
			}
		}

		cachedRects[0].weight = (float) (-sum0 / area0);
	}

	/**
	 *检测给定区域的特征
	 */
	public abstract float computeResponse(SummedSqTiltAreaTable sat, int x, int y);

	static class TiltedFeature extends HaarFeature {
		public TiltedFeature(WeightedRectangle[] rects) {
			super(rects, 2f);
		}

		@Override
		public float computeResponse(SummedSqTiltAreaTable sat, int rx, int ry) {
			float total = 0;
			for (int i = 0; i < cachedRects.length; i++) {
				final WeightedRectangle rect = cachedRects[i];

				final int x = rx + rect.x;
				final int y = ry + rect.y;
				final int width = rect.width;
				final int height = rect.height;

				final float p0 = sat.tiltSum.pixels[y][x];
				final float p1 = sat.tiltSum.pixels[y + height][x - height];
				final float p2 = sat.tiltSum.pixels[y + width][x + width];
				final float p3 = sat.tiltSum.pixels[y + width + height][x + width - height];

				final float regionSum = p0 - p1 - p2 + p3;

				total += regionSum * rect.weight;
			}

			return total;
		}
	}

	static class NormalFeature extends HaarFeature {
		public NormalFeature(WeightedRectangle[] rects) {
			super(rects, 1f);
		}

		@Override
		public float computeResponse(SummedSqTiltAreaTable sat, int rx, int ry) {
			float total = 0;
			for (int i = 0; i < cachedRects.length; i++) {
				final WeightedRectangle rect = cachedRects[i];

				final int x = rx + rect.x;
				final int y = ry + rect.y;
				final int width = rect.width;
				final int height = rect.height;

				final int yh = y + height;
				final int xw = x + width;

				final float regionSum = sat.sum.pixels[yh][xw] - sat.sum.pixels[yh][x] - sat.sum.pixels[y][xw]
						+ sat.sum.pixels[y][x];

				total += regionSum * rect.weight;
			}

			return total;
		}
	}

	/**
	 *通过给定数据创建特征
	 */
	public static HaarFeature create(List<WeightedRectangle> rectList, boolean tilted) {
		final WeightedRectangle[] rects = rectList.toArray(new WeightedRectangle[rectList.size()]);

		if (tilted)
			return new TiltedFeature(rects);

		return new NormalFeature(rects);
	}

	/**
	 * 创建haar特征
	 */
	public static HaarFeature create(boolean tilted, int x0, int y0, int w0, int h0, float wt0, int x1, int y1, int w1,
			int h1, float wt1) {
		final WeightedRectangle[] rects = new WeightedRectangle[2];
		rects[0] = new WeightedRectangle(x0, y0, w0, h0, wt0);
		rects[1] = new WeightedRectangle(x1, y1, w1, h1, wt1);

		return tilted ? new TiltedFeature(rects) : new NormalFeature(rects);
	}

	public static HaarFeature create(boolean tilted, int x0, int y0, int w0, int h0, float wt0, int x1, int y1, int w1,
			int h1, float wt1, int x2, int y2, int w2, int h2, float wt2) {
		final WeightedRectangle[] rects = new WeightedRectangle[3];
		rects[0] = new WeightedRectangle(x0, y0, w0, h0, wt0);
		rects[1] = new WeightedRectangle(x1, y1, w1, h1, wt1);
		rects[2] = new WeightedRectangle(x2, y2, w2, h2, wt2);

		return tilted ? new TiltedFeature(rects) : new NormalFeature(rects);
	}

}
