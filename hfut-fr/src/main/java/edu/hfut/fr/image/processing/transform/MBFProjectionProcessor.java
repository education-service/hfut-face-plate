package edu.hfut.fr.image.processing.transform;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Jimbo
 *
 * 将图像的所有像素点转化为矩阵
 */
public class MBFProjectionProcessor extends ProjectionProcessor<Float[], MBFImage> {

	@Override
	public MBFImage performProjection(int windowMinC, int windowMinR, MBFImage output) {
		final FImage[] bands = new FImage[output.numBands()];
		for (int i = 0; i < bands.length; i++) {
			bands[i] = output.getBand(i);
		}
		final FImage[][] input = new FImage[this.projectedShapes.size()][];
		for (int i = 0; i < input.length; i++) {
			MBFImage inputMBF = this.images.get(i);
			input[i] = new FImage[inputMBF.numBands()];
			for (int j = 0; j < input[i].length; j++) {
				input[i][j] = inputMBF.getBand(j);
			}
		}
		for (int y = 0; y < output.getHeight(); y++) {
			for (int x = 0; x < output.getWidth(); x++) {
				Point2dImpl realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (int k = 0; k < this.projectedRectangles.size(); k++) {
					Rectangle r = this.projectedRectangles.get(k);
					Shape s = this.projectedShapes.get(k);
					if (r.isInside(realPoint) && s.isInside(realPoint)) {
						double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.x + (float) transform[0][1] * realPoint.y
								+ (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.x + (float) transform[1][1] * realPoint.y
								+ (float) transform[1][2];
						float zt = (float) transform[2][0] * realPoint.x + (float) transform[2][1] * realPoint.y
								+ (float) transform[2][2];

						xt /= zt;
						yt /= zt;

						for (int j = 0; j < bands.length; j++) {
							FImage in = input[i][j];
							FImage out = bands[j];
							out.pixels[y][x] = in.getPixelInterpNative(xt, yt, out.pixels[y][x]);
						}

					}
					i++;
				}
			}
		}
		return output;
	}

}
