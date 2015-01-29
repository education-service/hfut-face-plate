package edu.hfut.fr.image.objectdetection;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;
import edu.hfut.fr.image.processing.transform.ProjectionProcessor;

/**
 * 旋转模拟检测器
 *
 * @author wanghao
 */
public class RotationSimulationObjectDetector<IMAGE extends Image<PIXEL, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>, PIXEL, DETECTED_OBJECT>
		implements ObjectDetector<IMAGE, TransformedDetection<DETECTED_OBJECT>> {

	private ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private Rectangle roi;
	private float scalefactor = 1f;
	private float[] simulationAngles;

	/**
	 * 构造函数
	 */
	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, int numRotations) {
		this.detector = detector;
		this.simulationAngles = computeAngles(numRotations);
	}

	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, int numRotations,
			float scalefactor) {
		this(detector, numRotations);
		this.scalefactor = scalefactor;
	}

	public RotationSimulationObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector, float[] simulationAngles,
			float scalefactor) {
		this.detector = detector;
		this.simulationAngles = simulationAngles;
		this.scalefactor = scalefactor;
	}

	private float[] computeAngles(int numRotations) {
		final float[] angles = new float[numRotations];

		for (int i = 1; i < numRotations; i++) {
			angles[i] = (float) (2 * i * Math.PI / numRotations);
		}

		return angles;
	}

	@Override
	public List<TransformedDetection<DETECTED_OBJECT>> detect(IMAGE image) {
		final List<TransformedDetection<DETECTED_OBJECT>> results = new ArrayList<TransformedDetection<DETECTED_OBJECT>>();

		Matrix scale;

		if (scalefactor != 1) {
			image = image.process(new ResizeProcessor(scalefactor));
			scale = TransformUtilities.scaleMatrix(scalefactor, scalefactor);
		} else {
			scale = Matrix.identity(3, 3);
		}

		for (final float angle : simulationAngles) {
			if (angle == 0) {
				detectObjects(image, scale, results);
			} else {
				final Matrix matrix = TransformUtilities.rotationMatrix(angle);
				final IMAGE rimg = ProjectionProcessor.project(image, matrix);

				final Rectangle actualBounds = image.getBounds();
				final Shape transformedActualBounds = actualBounds.transform(matrix);
				final double tminX = transformedActualBounds.minX();
				final double tminY = transformedActualBounds.minY();

				final int minc = (int) Math.floor(tminX);
				final int minr = (int) Math.floor(tminY);

				matrix.set(0, 2, -minc);
				matrix.set(1, 2, -minr);

				detectObjects(rimg, matrix.times(scale), results);
			}
		}

		return results;
	}

	private void detectObjects(IMAGE image, Matrix transform, List<TransformedDetection<DETECTED_OBJECT>> results) {
		if (this.roi != null) {
			final Rectangle troi = roi.transform(transform).calculateRegularBoundingBox();
			detector.setROI(troi);
		}

		final List<DETECTED_OBJECT> detections = detector.detect(image);

		if (detections == null)
			return;

		for (final DETECTED_OBJECT o : detections) {
			results.add(new TransformedDetection<DETECTED_OBJECT>(o, transform.inverse()));
		}
	}

	@Override
	public void setROI(Rectangle roi) {
		this.roi = roi;
	}

	/**
	 *获得内部检测器
	 */
	public ObjectDetector<IMAGE, DETECTED_OBJECT> getInnerDetector() {
		return detector;
	}

}
