package edu.hfut.fr.image.processing.background;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 *基本背景减法
 *
 *@author wanghao
 */
public class BasicBackgroundSubtract<I extends Image<?, I>> implements ImageProcessor<I> {

	I background;

	/**
	 * 默认构造函数
	 */
	public BasicBackgroundSubtract() {
	}

	public BasicBackgroundSubtract(I background) {
		this.background = background;
	}

	/**
	 * 设置背景
	 */
	public void setBackground(I background) {
		this.background = background;
	}

	/**
	 *处去图像背景
	 */
	@Override
	public void processImage(I image) {
		image.subtractInplace(background);
	}

}
