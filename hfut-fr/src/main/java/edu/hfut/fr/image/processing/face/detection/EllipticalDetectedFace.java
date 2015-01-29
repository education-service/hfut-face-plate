package edu.hfut.fr.image.processing.face.detection;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import edu.hfut.fr.image.processing.transform.FProjectionProcessor;

/**
 * elliptical检测人脸识别类
 *
 *@author wanggang
 */
public class EllipticalDetectedFace extends DetectedFace {

	Ellipse ellipse;

	/**
	 * 构造函数
	 */
	public EllipticalDetectedFace(Ellipse ellipse, FImage image, float confidence) {
		super();

		this.ellipse = ellipse;
		this.bounds = ellipse.calculateRegularBoundingBox();
		this.confidence = confidence;

		if (image != null)
			this.facePatch = extractPatch(image, ellipse);
	}

	private FImage extractPatch(final FImage image, final Ellipse ellipse) {
		final float x = ellipse.calculateCentroid().getX();
		final float y = ellipse.calculateCentroid().getY();
		final double major = ellipse.getMajor();
		final double minor = ellipse.getMinor();

		final Matrix rot = TransformUtilities.rotationMatrixAboutPoint(-ellipse.getRotation() + Math.PI / 2, x, y);

		final Matrix translate = TransformUtilities.translateMatrix(-(x - minor), -(y - major));

		final Matrix tf = translate.times(rot);

		final FProjectionProcessor pp = new FProjectionProcessor();
		pp.setMatrix(tf);
		pp.accumulate(image);
		return pp.performProjection(0, (int) (2 * minor), 0, (int) (2 * major));
	}

	@Override
	public Shape getShape() {
		return ellipse;
	}

}
