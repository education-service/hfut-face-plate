package edu.hfut.fr.image.processing.threshold;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 局部阈值的抽象函数
 *
 * @author Jimbo
 */
public abstract class AbstractLocalThreshold implements SinglebandImageProcessor<Float, FImage> {

	protected int sizeX;
	protected int sizeY;

	/**
	 * 构造函数
	 *
	 */
	public AbstractLocalThreshold(int size) {
		this(size, size);
	}

	/**
	 * 构造函数
	 */
	public AbstractLocalThreshold(int size_x, int size_y) {
		this.sizeX = size_x;
		this.sizeY = size_y;
	}

	/**
	 *	获取局部简单矩阵的高度
	 */
	public int getKernelHeight() {
		return sizeY;
	}

	/**
	 *	获取局部矩阵的宽度
	 */
	public int getKernelWidth() {
		return sizeX;
	}

}
