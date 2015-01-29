package edu.hfut.fr.image.objectdetection;

import Jama.Matrix;

/**
 * 转化检测对象
 *
 * @author wanghao
 */
public class TransformedDetection<DETECTED_OBJECT> {

	public Matrix transform;

	/**
	 * 检测对象
	 */
	public DETECTED_OBJECT detected;

	/**
	 *构造函数
	 */
	public TransformedDetection(DETECTED_OBJECT detected, Matrix transform) {
		this.detected = detected;
		this.transform = transform;
	}

}
