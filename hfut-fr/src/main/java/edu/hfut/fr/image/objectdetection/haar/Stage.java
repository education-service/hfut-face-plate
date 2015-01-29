package edu.hfut.fr.image.objectdetection.haar;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * 分类stage的实现
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Viola, P.", "Jones, M." }, title = "Rapid object detection using a boosted cascade of simple features", year = "2001", booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on", pages = {
		" I", "511 ", " I", "518 vol.1" }, number = "", volume = "1", customData = {
		"keywords",
		" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
		"doi", "10.1109/CVPR.2001.990517", "ISSN", "1063-6919 " })
public class Stage {

	float threshold;
	Classifier[] ensemble;

	Stage successStage;

	Stage failureStage;

	private boolean hasNegativeValueFeatures;

	/**
	 *构造函数
	 */
	public Stage(float threshold, Classifier[] trees, Stage successStage, Stage failureStage) {
		this.threshold = threshold;
		this.ensemble = trees;
		this.successStage = successStage;
		this.failureStage = failureStage;

		this.hasNegativeValueFeatures = checkForNegativeValueFeatures();
	}

	private boolean checkForNegativeValueFeatures() {
		for (int i = 0; i < ensemble.length; i++) {
			if (checkForNegativeValueFeatures(ensemble[i]))
				return true;
		}
		return false;
	}

	private boolean checkForNegativeValueFeatures(Classifier classifier) {
		if (classifier instanceof ValueClassifier) {
			if (((ValueClassifier) classifier).value < 0)
				return true;
		} else {
			if (checkForNegativeValueFeatures(((HaarFeatureClassifier) classifier).left))
				return true;
			if (checkForNegativeValueFeatures(((HaarFeatureClassifier) classifier).right))
				return true;
		}

		return false;
	}

	/**
	 *测试stage
	 */
	public boolean pass(final SummedSqTiltAreaTable sat, final float wvNorm, final int x, final int y) {
		float total = 0;

		if (hasNegativeValueFeatures) {
			for (int i = 0; i < ensemble.length; i++) {
				total += ensemble[i].classify(sat, wvNorm, x, y);
			}

			return total >= threshold;
		} else {
			for (int i = 0; i < ensemble.length; i++) {
				total += ensemble[i].classify(sat, wvNorm, x, y);
				if (total >= threshold)
					return true;
			}

			return false;
		}
	}

	/**
	 *更新缓存
	 */
	void updateCaches(StageTreeClassifier cascade) {
		for (int i = 0; i < ensemble.length; i++) {
			ensemble[i].updateCaches(cascade);
		}
	}

	public Stage successStage() {
		return successStage;
	}

	/**
	 *获得下个stage
	 */
	public Stage failureStage() {
		return failureStage;
	}

}
