package edu.hfut.fr.image.processing.effects;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.line.Line2d;

import edu.hfut.fr.image.analysis.algorithm.SummedAreaTable;

/**
 * Diorama Effect实现类
 *
 * @author wanghao
 */
public class DioramaEffect implements SinglebandImageProcessor<Float, FImage> {

	Line2d axis;

	/**
	 * 给定axis进行构造
	 *
	 */
	public DioramaEffect(Line2d axis) {
		this.axis = axis;
	}

	/**
	 * 返回当前axis
	 */
	public Line2d getAxis() {
		return axis;
	}

	/**
	 * 设置当前axis
	 */
	public void setAxis(Line2d axis) {
		this.axis = axis;
	}

	@Override
	public void processImage(FImage image) {
		render(image, new SummedAreaTable(image), (int) axis.getBeginPoint().getX(), (int) axis.getBeginPoint().getY(),
				(int) axis.getEndPoint().getX(), (int) axis.getEndPoint().getY());
	}

	private void render(final FImage image, final SummedAreaTable sat, final int x1, final int y1, final int x2,
			final int y2) {
		final int w = image.width;
		final int h = image.height;
		final double s = (w + h) * 2.0;

		final int dx = x2 - x1;
		final int dy = y2 - y1;

		final float[][] pixels = image.pixels;

		for (int y = 0; y < h; ++y) {
			final double yt = y - y1;
			for (int x = 0; x < w; ++x) {
				final double xt = x - x1;

				final double r = (dx * xt + dy * yt) / s;
				final int ri = r < 0 ? (int) -r : (int) r;

				final int yMin = Math.max(0, y - ri);
				final int yMax = Math.min(h, y + ri);
				final int bh = yMax - yMin;

				if (bh == 0)
					continue;

				final int xMin = Math.max(0, x - ri);
				final int xMax = Math.min(w, x + ri);
				final float scale = 1.0f / (xMax - xMin) / bh;

				pixels[y][x] = sat.calculateArea(xMin, yMin, xMax, yMax) * scale;
			}
		}
	}

}
