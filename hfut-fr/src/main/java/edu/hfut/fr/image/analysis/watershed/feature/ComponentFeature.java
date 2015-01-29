package edu.hfut.fr.image.analysis.watershed.feature;

import org.openimaj.image.pixel.IntValuePixel;

/**
 * 区域特征实现接口
 *
 * @author wanghao
 */
public interface ComponentFeature extends Cloneable {

	/**
	 * 收集特征
	 */
	public void merge(ComponentFeature f);

	/**
	 *增加特征像素值
	 */
	public void addSample(IntValuePixel p);

	/**
	 * 复制特征值
	 */
	public ComponentFeature clone();

}
