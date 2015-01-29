package edu.hfut.fr.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 检测部分明晰化（边缘）
 *
 * @author Jimbo
 */
public class SimpleDetectedFaceRenderer implements DetectedFaceRenderer<DetectedFace> {

	private Float[] boundingBoxColour = RGBColour.RED;

	@Override
	public void drawDetectedFace(MBFImage image, int thickness, DetectedFace f) {
		image.drawShape(f.getShape(), thickness, boundingBoxColour);
	}

}
