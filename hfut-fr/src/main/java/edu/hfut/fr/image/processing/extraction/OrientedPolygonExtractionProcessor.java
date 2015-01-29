package edu.hfut.fr.image.processing.extraction;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import edu.hfut.fr.image.processing.transform.ProjectionProcessor;

/**
 * 从一幅图像中抽取出多边形形成新的图像
 *
 *@author wanggang
 */
public class OrientedPolygonExtractionProcessor implements ImageProcessor<FImage> {

	private final float background;
	private final Ellipse polygonEllipse;

	public OrientedPolygonExtractionProcessor(final Polygon polygon, final float background) {
		this.polygonEllipse = polygon.toEllipse();
		this.background = background;
	}

	@Override
	public void processImage(final FImage image) {
		image.internalAssign(this.orientedBoundingBoxProjection(image));
	}

	private FImage orientedBoundingBoxProjection(final FImage image) {
		final ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float, FImage>();
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(TransformUtilities.rotationMatrix(-this.polygonEllipse.getRotation()));
		trans = trans.times(TransformUtilities.translateToPointMatrix(this.polygonEllipse.calculateCentroid(),
				new Point2dImpl(0, 0)));
		pp.setMatrix(trans);
		pp.accumulate(image);
		return pp.performProjection((int) -this.polygonEllipse.getMajor(), (int) this.polygonEllipse.getMajor(),
				(int) -this.polygonEllipse.getMinor(), (int) this.polygonEllipse.getMinor(), this.background);
	}

}
