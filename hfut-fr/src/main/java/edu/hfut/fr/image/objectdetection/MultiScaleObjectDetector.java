package edu.hfut.fr.image.objectdetection;

import org.openimaj.image.Image;

/**
 * 交叉维度检测器
 *
 * @author wanghao
 */
public interface MultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT> extends
		ObjectDetector<IMAGE, DETECTED_OBJECT> {

	/**
	 *设置最小检测大小
	 */
	public void setMinimumDetectionSize(int size);

	public void setMaximumDetectionSize(int size);

	/**
	 *得到最小检测大小
	 *
	 */
	public int getMinimumDetectionSize();

	/**
	 *得到检测最大值
	 */
	public int getMaximumDetectionSize();

}
