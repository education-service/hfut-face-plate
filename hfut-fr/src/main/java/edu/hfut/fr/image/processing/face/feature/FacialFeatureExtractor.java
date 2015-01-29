package edu.hfut.fr.image.processing.face.feature;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.io.ReadWriteableBinary;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 特征提取接口
 *
 * @author wanggang
 */
public interface FacialFeatureExtractor<T extends FacialFeature, Q extends DetectedFace> extends
		FeatureExtractor<T, Q>, ReadWriteableBinary {

}
