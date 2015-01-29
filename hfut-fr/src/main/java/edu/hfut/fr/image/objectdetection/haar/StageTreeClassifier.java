package edu.hfut.fr.image.objectdetection.haar;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * 阶段树分类器
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Viola, P.", "Jones, M." }, title = "Rapid object detection using a boosted cascade of simple features", year = "2001", booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on", pages = {
		" I", "511 ", " I", "518 vol.1" }, number = "", volume = "1", customData = {
		"keywords",
		" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
		"doi", "10.1109/CVPR.2001.990517", "ISSN", "1063-6919 " })
public class StageTreeClassifier {

	/**
	 * 分类宽度
	 */
	int width;

	/**
	* 分类高度
	 */
	int height;

	/**
	*分类器名称
	 */
	String name;

	boolean hasTiltedFeatures;

	/**
	 * 树的根
	 */
	Stage root;

	float cachedScale;
	float cachedInvArea;
	int cachedW;
	int cachedH;

	/**
	 * 构造函数
	 */
	public StageTreeClassifier(int width, int height, String name, boolean hasTiltedFeatures, Stage root) {
		this.width = width;
		this.height = height;
		this.name = name;
		this.hasTiltedFeatures = hasTiltedFeatures;
		this.root = root;
	}

	float computeWindowVarianceNorm(SummedSqTiltAreaTable sat, int x, int y) {
		x += Math.round(cachedScale); // shift by 1 scaled px to centre box
		y += Math.round(cachedScale);

		final float sum = sat.sum.pixels[y + cachedH][x + cachedW] + sat.sum.pixels[y][x]
				- sat.sum.pixels[y + cachedH][x] - sat.sum.pixels[y][x + cachedW];
		final float sqSum = sat.sqSum.pixels[y + cachedH][x + cachedW] + sat.sqSum.pixels[y][x]
				- sat.sqSum.pixels[y + cachedH][x] - sat.sqSum.pixels[y][x + cachedW];

		final float mean = sum * cachedInvArea;
		float wvNorm = sqSum * cachedInvArea - mean * mean;
		wvNorm = (float) ((wvNorm >= 0) ? Math.sqrt(wvNorm) : 1);

		return wvNorm;
	}

	/**
	 * 设置检测维度
	 */
	public void setScale(float scale) {
		this.cachedScale = scale;

		cachedW = Math.round(scale * (width - 2));
		cachedH = Math.round(scale * (height - 2));
		cachedInvArea = 1.0f / (cachedW * cachedH);

		updateCaches(root);
	}

	/**
	 *更新缓存
	 */
	private void updateCaches(Stage s) {
		s.updateCaches(this);

		if (s.successStage != null)
			updateCaches(s.successStage);
		if (s.failureStage != null)
			updateCaches(s.failureStage);
	}

	/**
	 * 实现分类方法
	 */
	public int classify(SummedSqTiltAreaTable sat, int x, int y) {
		final float wvNorm = computeWindowVarianceNorm(sat, x, y);

		int matches = 0;
		Stage stage = root;
		while (true) {
			if (stage.pass(sat, wvNorm, x, y)) {
				matches++;
				stage = stage.successStage;
				if (stage == null) {
					return matches;
				}
			} else {
				stage = stage.failureStage;
				if (stage == null) {
					return -matches;
				}
			}
		}
	}

	/**
	 *得到分类宽度
	 */
	public int getWidth() {
		return width;
	}

	/**
	 *　得到高度
	 */
	public int getHeight() {
		return height;
	}

	/**
	 *　得到分类器名
	 */
	public String getName() {
		return name;
	}

	public boolean hasTiltedFeatures() {
		return hasTiltedFeatures;
	}

}
