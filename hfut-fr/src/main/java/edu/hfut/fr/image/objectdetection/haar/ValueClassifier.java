package edu.hfut.fr.image.objectdetection.haar;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * 分类器
 *
 * @author wanghao
 */
public final class ValueClassifier implements Classifier {

	float value;

	/**
	 *构造函数
	 */
	public ValueClassifier(float value) {
		this.value = value;
	}

	@Override
	public final float classify(final SummedSqTiltAreaTable sat, final float wvNorm, final int x, final int y) {
		return value;
	}

	@Override
	public void updateCaches(StageTreeClassifier cascade) {
	}

}
