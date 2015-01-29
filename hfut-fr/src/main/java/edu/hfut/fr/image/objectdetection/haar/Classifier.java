package edu.hfut.fr.image.objectdetection.haar;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 *单个 haar分类器接口
 *
 *@author wanghao
 */
public interface Classifier {

	public float classify(final SummedSqTiltAreaTable sat, final float wvNorm, final int x, final int y);

	/**
	 *更新缓存
	 */
	void updateCaches(StageTreeClassifier cascade);

}
