package edu.hfut.fr.image.processing.transform;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Jimbo
 *
 * 将图像转换为系列的矩阵
 */

public class FProjectionProcessor extends ProjectionProcessor<Float, FImage> {

	/**
	 *计算图像的投影
	 *
	 */
	@Override
	public FImage performProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR,
			Float backgroundColour) {
		FImage output = null;
		output = new FImage(windowMaxC - windowMinC, windowMaxR - windowMinR);
		if (backgroundColour != null)
			output.fill(backgroundColour);
		Shape[][] shapeRects = this.getCurrentShapes();
		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (int j = 0; j < shapeRects.length; j++) {
					if (backgroundColour == null || isInside(j, shapeRects, realPoint)) {
						double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						FImage im = this.images.get(i);
						if (backgroundColour != null)
							output.pixels[y][x] = im.getPixelInterp(xt, yt, backgroundColour);
						else
							output.pixels[y][x] = im.getPixelInterp(xt, yt);
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * 计算图像的投影
	 */
	@Override
	public FImage performBlendedProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR,
			Float backgroundColour) {
		FImage output = null;
		output = new FImage(windowMaxC - windowMinC, windowMaxR - windowMinR);
		Map<Integer, Boolean> setMap = new HashMap<Integer, Boolean>();
		FImage blendingPallet = output.newInstance(2, 1);
		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (Shape s : this.projectedShapes) {
					if (s.isInside(realPoint)) {
						double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						Float toSet = null;
						if (backgroundColour != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, backgroundColour);
						else if (setMap.get(y * output.getWidth() + x) != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, output.getPixelInterp(x, y));
						else
							toSet = this.images.get(i).getPixelInterp(xt, yt);
						if (setMap.get(y * output.getWidth() + x) != null) {
							blendingPallet.pixels[0][1] = toSet;
							blendingPallet.pixels[0][0] = output.getPixel(x, y);

							toSet = blendingPallet.getPixelInterp(0.1, 0.5);
						}
						setMap.put(y * output.getWidth() + x, true);
						output.pixels[y][x] = toSet;
					}
					i++;
				}
			}
		}
		return output;
	}

}
