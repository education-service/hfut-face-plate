package edu.hfut.fr.image.processing.transform;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.renderer.ScanRasteriser;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * 实现分段的弯曲
 *
 * @author Jimbo
 */
public class PiecewiseMeshWarp<T, I extends Image<T, I>> implements ImageProcessor<I> {

	List<Pair<Shape>> matchingRegions;
	List<Matrix> transforms = new ArrayList<Matrix>();
	Rectangle bounds;

	/**
	 * 构造函数
	 * @param matchingRegions
	 *            匹配的形状
	 */
	public PiecewiseMeshWarp(List<Pair<Shape>> matchingRegions) {
		this.matchingRegions = matchingRegions;
		initTransforms();
	}

	protected final Matrix getTransform(Point2d p) {
		final int sz = matchingRegions.size();

		for (int i = 0; i < sz; i++) {
			if (matchingRegions.get(i).secondObject().isInside(p)) {
				return transforms.get(i);
			}
		}
		return null;
	}

	/**
	 * 获取匹配的形状
	 *
	 */
	public Shape getMatchingShape(Point2d p) {
		for (int i = 0; i < matchingRegions.size(); i++) {
			final Pair<Shape> matching = matchingRegions.get(i);
			if (matching.secondObject().isInside(p)) {
				return matching.firstObject();
			}
		}
		return null;
	}

	/**
	 *	获取正则空间的一个点的形状对指数
	 *
	 */
	public int getMatchingShapeIndex(Point2d p) {
		for (int i = 0; i < matchingRegions.size(); i++) {
			final Pair<Shape> matching = matchingRegions.get(i);
			if (matching.secondObject().isInside(p)) {
				return i;
			}
		}
		return -1;
	}

	protected void initTransforms() {
		bounds = new Rectangle(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);

		for (final Pair<Shape> shape : matchingRegions) {
			final Polygon p1 = shape.firstObject().asPolygon();
			final Polygon p2 = shape.secondObject().asPolygon();

			bounds.x = (float) Math.min(bounds.x, p2.minX());
			bounds.y = (float) Math.min(bounds.y, p2.minY());
			bounds.width = (float) Math.max(bounds.width, p2.maxX());
			bounds.height = (float) Math.max(bounds.height, p2.maxY());

			if (p1.nVertices() == 3) {
				transforms.add(getTransform3(polyMatchToPointsMatch(p2, p1)));
			} else if (p1.nVertices() == 4) {
				transforms.add(getTransform4(polyMatchToPointsMatch(p2, p1)));
			} else {
				throw new RuntimeException("Only polygons with 3 or 4 vertices are supported!");
			}
		}

		bounds.width -= bounds.x;
		bounds.height -= bounds.y;
	}

	protected List<Pair<Point2d>> polyMatchToPointsMatch(Polygon pa, Polygon pb) {
		final List<Pair<Point2d>> pts = new ArrayList<Pair<Point2d>>();
		for (int i = 0; i < pa.nVertices(); i++) {
			final Point2d pta = pa.getVertices().get(i);
			final Point2d ptb = pb.getVertices().get(i);

			pts.add(new Pair<Point2d>(pta, ptb));
		}
		return pts;
	}

	protected Matrix getTransform4(List<Pair<Point2d>> pts) {
		return TransformUtilities.homographyMatrixNorm(pts);
	}

	protected Matrix getTransform3(List<Pair<Point2d>> pts) {
		return TransformUtilities.affineMatrix(pts);
	}

	@Override
	public void processImage(final I image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final I ret = image.newInstance(width, height);

		final Scan scan = new Scan(width, height, image, ret);

		for (int i = 0; i < matchingRegions.size(); i++) {
			final Polygon from = matchingRegions.get(i).secondObject().asPolygon();
			scan.tf = transforms.get(i);

			ScanRasteriser.scanFill(from.points, scan);
		}

		image.internalAssign(ret);
	}

	/**
	 *	将输入图像的内容按照给定尺寸的输出图像
	 *
	 */
	public I transform(final I image, int width, int height) {
		final I ret = image.newInstance(width, height);

		final Scan scan = new Scan(width, height, image, ret);

		for (int i = 0; i < matchingRegions.size(); i++) {
			final Polygon from = matchingRegions.get(i).secondObject().asPolygon();
			scan.tf = transforms.get(i);

			ScanRasteriser.scanFill(from.points, scan);
		}

		return ret;
	}

	private class Scan implements ScanLineListener {
		private final Pixel p = new Pixel();
		private final int xmin = (int) Math.max(0, bounds.x);
		private final int ymin = (int) Math.max(0, bounds.y);
		private final int xmax;
		private final int ymax;
		private final I image;
		private final I ret;
		Matrix tf;

		Scan(int width, int height, I image, I ret) {
			xmax = (int) Math.min(width, bounds.x + bounds.width);
			ymax = (int) Math.min(height, bounds.y + bounds.height);
			this.image = image;
			this.ret = ret;
		}

		@Override
		public void process(int x1, int x2, int y) {
			if (y < ymin || y > ymax)
				return;

			final int startx = Math.max(xmin, Math.min(x1, x2));
			final int stopx = Math.min(xmax, Math.max(x1, x2));

			for (int x = startx; x <= stopx; x++) {
				p.x = x;
				p.y = y;

				p.transformInplace(tf);

				ret.setPixel(x, y, image.getPixelInterp(p.x, p.y));
			}
		}
	}

}
