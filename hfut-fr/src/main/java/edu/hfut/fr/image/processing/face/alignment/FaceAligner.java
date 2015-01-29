package edu.hfut.fr.image.processing.face.alignment;

import org.openimaj.image.FImage;
import org.openimaj.io.ReadWriteableBinary;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 面部矫正器接口
 *
 *@author wanggang
 */
public interface FaceAligner<T extends DetectedFace> extends ReadWriteableBinary {

	public FImage align(T face);

	/**
	 * 返回mask对象
	 */
	public FImage getMask();

}
