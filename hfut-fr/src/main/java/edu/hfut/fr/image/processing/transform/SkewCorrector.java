package edu.hfut.fr.image.processing.transform;

import java.util.Collection;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;

import Jama.Matrix;
import edu.hfut.fr.image.analysis.algorithm.HoughLines;
import edu.hfut.fr.image.processing.edges.CannyEdgeDetector;
import edu.hfut.fr.image.processing.threshold.OtsuThreshold;

/**
 * 使用霍夫变换找到图像中的对称线使用基本的对称方法将转换为非对称
 *
 * @author Jimbo
 */
public class SkewCorrector implements ImageProcessor<FImage> {

	private static final boolean DEBUG = false;

	private int accuracy = 1;

	@Override
	public void processImage(final FImage image) {
		final CannyEdgeDetector cad = new CannyEdgeDetector();
		final FImage edgeImage = image.process(cad).inverse();

		// 检测图像中的线条
		final HoughLines hl = new HoughLines(360 * this.accuracy);
		edgeImage.analyseWith(hl);

		if (SkewCorrector.DEBUG)
			this.debugLines(edgeImage, Matrix.identity(3, 3), "Detection of Horizontal Lines", hl.getBestLines(2));

		double rotationAngle = hl.calculatePrevailingAngle();

		FImage rotImg = null;
		FImage outImg = null;
		if (rotationAngle == Double.MIN_VALUE) {
			System.out.println("WARNING: Detection of rotation angle failed.");
			rotImg = edgeImage.clone();
			outImg = image.clone();
		} else {
			rotationAngle -= 90;
			rotationAngle %= 360;

			if (SkewCorrector.DEBUG)
				System.out.println("Rotational angle: " + rotationAngle);

			rotationAngle *= 0.0174532925;

			final Matrix rotationMatrix = new Matrix(new double[][] {
					{ Math.cos(-rotationAngle), -Math.sin(-rotationAngle), 0 },
					{ Math.sin(-rotationAngle), Math.cos(-rotationAngle), 0 }, { 0, 0, 1 } });

			rotImg = ProjectionProcessor.project(edgeImage, rotationMatrix, 1f).process(new OtsuThreshold());

			outImg = ProjectionProcessor.project(image, rotationMatrix, 0f);
		}

		if (SkewCorrector.DEBUG)
			DisplayUtilities.display(outImg, "Rotated Image");

		rotImg.analyseWith(hl);

		final float shearAngleRange = 20;

		if (SkewCorrector.DEBUG)
			this.debugLines(rotImg, Matrix.identity(3, 3), "Detection of Vertical Lines",
					hl.getBestLines(2, -shearAngleRange, shearAngleRange));

		double shearAngle = hl.calculatePrevailingAngle(-shearAngleRange, shearAngleRange);

		if (shearAngle == Double.MIN_VALUE) {
			System.out.println("WARNING: Detection of shear angle failed.");
		} else {
			shearAngle %= 360;

			if (SkewCorrector.DEBUG)
				System.out.println("Shear angle = " + shearAngle);

			shearAngle *= 0.0174532925;

			final Matrix shearMatrix = new Matrix(new double[][] { { 1, Math.tan(shearAngle), 0 }, { 0, 1, 0 },
					{ 0, 0, 1 } });

			outImg = outImg.transform(shearMatrix);
		}

		if (SkewCorrector.DEBUG)
			DisplayUtilities.display(outImg, "Final Image");

		image.internalAssign(outImg);
	}

	/**
	 *
	 * 使用线条来展示图像
	 */
	private void debugLines(final FImage i, final Matrix tf, final String title, final Collection<Line2d> lines) {
		final MBFImage output = new MBFImage(i.getWidth(), i.getHeight(), 3);
		final MBFImageRenderer r = output.createRenderer(); // RenderHints.ANTI_ALIASED
															// );
		r.drawImage(i, 0, 0);

		for (final Line2d l : lines) {
			final Line2d l2 = l.transform(tf).lineWithinSquare(output.getBounds());

			if (l2 != null) {
				System.out.println(l2);
				r.drawLine(l2, 2, RGBColour.RED);
			}
		}

		DisplayUtilities.display(output, title);
	}

	/**
	 * 矫正倾斜度
	 */
	public void setAccuracy(final int accuracy) {
		this.accuracy = accuracy;
	}

}
