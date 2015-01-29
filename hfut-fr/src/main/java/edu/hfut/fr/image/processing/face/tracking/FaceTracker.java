package edu.hfut.fr.image.processing.face.tracking;

import java.util.List;

import org.openimaj.image.Image;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 	图像人脸跟踪器接口
 *
 * @author jimbo
 */
public interface FaceTracker<I extends Image<?, I>> {

	public List<DetectedFace> trackFace(I img);

}
