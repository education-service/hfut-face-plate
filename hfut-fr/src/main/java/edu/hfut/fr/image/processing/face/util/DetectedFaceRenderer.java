package edu.hfut.fr.image.processing.face.util;

import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 将检测出的人脸明晰化
 *
 * @author jimbo
 */
public interface DetectedFaceRenderer<DETECTED_FACE extends DetectedFace> {

	public void drawDetectedFace(MBFImage image, int thickness, DETECTED_FACE f);

}
