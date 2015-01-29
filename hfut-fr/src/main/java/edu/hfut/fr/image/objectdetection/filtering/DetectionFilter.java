package edu.hfut.fr.image.objectdetection.filtering;

import java.util.List;

/**
 *识别方法接口
 *
 *@author wanghao
 */
public interface DetectionFilter<IN, OUT> {

	public List<OUT> apply(List<IN> input);

}
