package edu.hfut.fr.image.objectdetection;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 多维检测器抽象类
 *
 * @author wanghao
 */
public abstract class AbstractMultiScaleObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT> implements
		MultiScaleObjectDetector<IMAGE, DETECTED_OBJECT> {

	protected Rectangle roi;
	protected int minSize = 0;
	protected int maxSize = 0;

	/**
	 * 抽象多维检测器
	 */
	protected AbstractMultiScaleObjectDetector() {
	}

	protected AbstractMultiScaleObjectDetector(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	@Override
	public void setROI(Rectangle roi) {
		this.roi = roi;
	}

	@Override
	public void setMinimumDetectionSize(int size) {
		this.minSize = size;
	}

	@Override
	public void setMaximumDetectionSize(int size) {
		this.maxSize = size;
	}

	@Override
	public int getMinimumDetectionSize() {
		return minSize;
	}

	@Override
	public int getMaximumDetectionSize() {
		return maxSize;
	}

}
