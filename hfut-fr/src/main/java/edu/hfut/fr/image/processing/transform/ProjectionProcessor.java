package edu.hfut.fr.image.processing.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.combiner.AccumulatingImageCombiner;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

import Jama.Matrix;

/**
 *计算一组图像的一组矩阵变换和构建包含所有像素的一个图像（或像素的窗口）在投影空间
 *
 * @author Jimbo
 */
public class ProjectionProcessor<Q, T extends Image<Q, T>> implements AccumulatingImageCombiner<T, T> {

	protected int minc;
	protected int minr;
	protected int maxc;
	protected int maxr;
	protected boolean unset;
	protected List<Matrix> transforms;
	protected List<Matrix> transformsInverted;
	protected List<T> images;
	protected List<Shape> projectedShapes;
	protected List<Rectangle> projectedRectangles;

	protected Matrix currentMatrix = new Matrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });

	/**
	 * 构造函数
	 */
	public ProjectionProcessor() {
		unset = true;
		this.minc = 0;
		this.minr = 0;
		this.maxc = 0;
		this.maxr = 0;

		transforms = new ArrayList<Matrix>();
		this.transformsInverted = new ArrayList<Matrix>();
		images = new ArrayList<T>();
		this.projectedShapes = new ArrayList<Shape>();
		this.projectedRectangles = new ArrayList<Rectangle>();
	}

	/**
	 * 设置矩阵
	 *
	 */
	public void setMatrix(Matrix matrix) {
		if (matrix.getRowDimension() == 2) {
			final int c = matrix.getColumnDimension() - 1;

			currentMatrix = new Matrix(3, 3);
			currentMatrix.setMatrix(0, 1, 0, c, matrix);
			currentMatrix.set(2, 2, 1);
		} else {
			this.currentMatrix = matrix;
		}
	}

	/**
	 * 利用给定的矩阵转化图像
	 */
	@Override
	public void accumulate(T image) {
		final Rectangle actualBounds = image.getBounds();
		final Shape transformedActualBounds = actualBounds.transform(this.currentMatrix);
		final double tminX = transformedActualBounds.minX();
		final double tmaxX = transformedActualBounds.maxX();
		final double tminY = transformedActualBounds.minY();
		final double tmaxY = transformedActualBounds.maxY();
		if (unset) {
			this.minc = (int) Math.floor(tminX);
			this.minr = (int) Math.floor(tminY);
			this.maxc = (int) Math.floor(tmaxX);
			this.maxr = (int) Math.floor(tmaxY);
			unset = false;
		} else {
			if (tminX < minc)
				minc = (int) Math.floor(tminX);
			if (tmaxX > maxc)
				maxc = (int) Math.floor(tmaxX);
			if (tminY < minr)
				minr = (int) Math.floor(tminY);
			if (tmaxY > maxr)
				maxr = (int) Math.floor(tmaxY);
		}
		final float padding = 1f;
		final Rectangle expandedBounds = new Rectangle(actualBounds.x - padding, actualBounds.y - padding,
				actualBounds.width + padding * 2, actualBounds.height + padding * 2);
		final Shape transformedExpandedBounds = expandedBounds.transform(this.currentMatrix);
		Matrix minv = null, m = null;
		try {
			m = this.currentMatrix.copy();
			minv = this.currentMatrix.copy().inverse();
		} catch (final Throwable e) {
			return;
		}

		this.images.add(image);
		this.transforms.add(m);
		this.transformsInverted.add(minv);
		this.projectedShapes.add(transformedExpandedBounds);
		this.projectedRectangles.add(transformedExpandedBounds.calculateRegularBoundingBox());

	}

	/**
	 * 返回包含像素点的图像
	 */
	public T performProjection() {
		return performProjection(false, this.images.get(0).newInstance(1, 1).getPixel(0, 0));
	}

	/**
	 * 返回图像
	 */
	public T performProjection(Q backgroundColour) {
		final int projectionMinC = minc, projectionMaxC = maxc, projectionMinR = minr, projectionMaxR = maxr;
		return performProjection(projectionMinC, projectionMaxC, projectionMinR, projectionMaxR, backgroundColour);
	}

	public T performProjection(boolean keepOriginalWindow, Q backgroundColour) {
		int projectionMinC = minc, projectionMaxC = maxc, projectionMinR = minr, projectionMaxR = maxr;
		if (keepOriginalWindow) {
			projectionMinC = 0;
			projectionMinR = 0;
			projectionMaxR = images.get(0).getRows();
			projectionMaxC = images.get(0).getCols();
		}
		return performProjection(projectionMinC, projectionMaxC, projectionMinR, projectionMaxR, backgroundColour);
	}

	public T performProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR) {
		return performProjection(windowMinC, windowMaxC, windowMinR, windowMaxR, this.images.get(0).newInstance(1, 1)
				.getPixel(0, 0));
	}

	public T performProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR, Q backgroundColour) {
		T output = null;
		output = images.get(0).newInstance(windowMaxC - windowMinC, windowMaxR - windowMinR);
		if (backgroundColour != null)
			output.fill(backgroundColour);

		final Shape[][] projectRectangleShapes = getCurrentShapes();

		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (int shapeIndex = 0; shapeIndex < this.projectedShapes.size(); shapeIndex++) {
					if (backgroundColour == null || isInside(shapeIndex, projectRectangleShapes, realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						final T im = this.images.get(i);
						if (backgroundColour != null)
							output.setPixel(x, y, im.getPixelInterp(xt, yt, backgroundColour));
						else
							output.setPixel(x, y, im.getPixelInterp(xt, yt));
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * 获得当前形状
	 *
	 */
	protected Shape[][] getCurrentShapes() {
		final Shape[][] currentShapes = new Shape[this.projectedShapes.size()][2];
		for (int i = 0; i < this.projectedShapes.size(); i++) {
			currentShapes[i][0] = this.projectedRectangles.get(i);
			currentShapes[i][1] = this.projectedShapes.get(i);
		}
		return currentShapes;
	}

	protected boolean isInside(int shapeIndex, Shape[][] projectRectangleShapes, Point2d realPoint) {
		return projectRectangleShapes[shapeIndex][0].isInside(realPoint)
				&& projectRectangleShapes[shapeIndex][1].isInside(realPoint);
	}

	public T performProjection(int windowMinC, int windowMinR, T output) {

		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (final Shape s : this.projectedShapes) {
					if (s.calculateRegularBoundingBox().isInside(realPoint) && s.isInside(realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						final T im = this.images.get(i);
						output.setPixel(x, y, im.getPixelInterp(xt, yt, output.getPixel(x, y)));
					}
					i++;
				}
			}
		}

		return output;
	}

	public T performBlendedProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR, Q backgroundColour) {
		T output = null;
		output = images.get(0).newInstance(windowMaxC - windowMinC, windowMaxR - windowMinR);
		final Map<Integer, Boolean> setMap = new HashMap<Integer, Boolean>();
		final T blendingPallet = output.newInstance(2, 1);
		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (final Shape s : this.projectedShapes) {
					if (s.isInside(realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						Q toSet = null;
						if (backgroundColour != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, backgroundColour);
						else if (setMap.get(y * output.getWidth() + x) != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, output.getPixelInterp(x, y));
						else
							toSet = this.images.get(i).getPixelInterp(xt, yt);
						if (setMap.get(y * output.getWidth() + x) != null) {
							blendingPallet.setPixel(1, 0, toSet);
							blendingPallet.setPixel(0, 0, output.getPixel(x, y));

							toSet = blendingPallet.getPixelInterp(0.1, 0.5);
						}
						setMap.put(y * output.getWidth() + x, true);
						output.setPixel(x, y, toSet);
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * 返回当前矩阵
	 */
	public Matrix getMatrix() {
		return this.currentMatrix;
	}

	/**
	 * 返回工程图像
	 */
	@SuppressWarnings("unchecked")
	public static <Q, T extends Image<Q, T>> T project(T image, Matrix matrix) {
		if ((Image<?, ?>) image instanceof FImage) {
			final FProjectionProcessor proc = new FProjectionProcessor();
			proc.setMatrix(matrix);
			((FImage) (Image<?, ?>) image).accumulateWith(proc);
			return (T) (Image<?, ?>) proc.performProjection();
		}
		if ((Image<?, ?>) image instanceof MBFImage) {
			final MBFProjectionProcessor proc = new MBFProjectionProcessor();
			proc.setMatrix(matrix);
			((MBFImage) (Image<?, ?>) image).accumulateWith(proc);
			return (T) (Image<?, ?>) proc.performProjection();
		} else {
			final ProjectionProcessor<Q, T> proc = new ProjectionProcessor<Q, T>();
			proc.setMatrix(matrix);
			image.accumulateWith(proc);
			return proc.performProjection();
		}
	}

	public static <Q, T extends Image<Q, T>> T project(T image, Matrix matrix, Q backgroundColour) {
		final ProjectionProcessor<Q, T> proc = new ProjectionProcessor<Q, T>();
		proc.setMatrix(matrix);
		image.accumulateWith(proc);
		return proc.performProjection(backgroundColour);
	}

	@Override
	public T combine() {
		return performProjection();
	}

}
