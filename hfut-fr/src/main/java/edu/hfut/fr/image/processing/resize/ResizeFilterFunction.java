package edu.hfut.fr.image.processing.resize;

/**
 * 基于重采样函数插值滤波函数
 *
 *@author jimbo
 */
public interface ResizeFilterFunction {

	double filter(double d);

	double getSupport();

}
