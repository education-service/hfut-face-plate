package edu.hfut.fr.image.processing.face.detection;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.io.ReadWriteableBinary;

/**
 * 检测人脸接口
 *
 * @author wanggang
 */
public interface FaceDetector<T extends DetectedFace, I extends Image<?, I>> extends ReadWriteableBinary {

	/**
	 * 在图像中检测人脸并返回检测到的人脸数组
	 */
	public List<T> detectFaces(I image);

}
