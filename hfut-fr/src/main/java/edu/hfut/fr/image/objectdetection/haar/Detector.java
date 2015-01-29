package edu.hfut.fr.image.objectdetection.haar;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;
import edu.hfut.fr.image.objectdetection.AbstractMultiScaleObjectDetector;

/**
 * 检测器类
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Viola, P.", "Jones, M." }, title = "Rapid object detection using a boosted cascade of simple features", year = "2001", booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on", pages = {
		" I", "511 ", " I", "518 vol.1" }, number = "", volume = "1", customData = {
		"keywords",
		" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
		"doi", "10.1109/CVPR.2001.990517", "ISSN", "1063-6919 " })
public class Detector extends AbstractMultiScaleObjectDetector<FImage, Rectangle> {

	public static final int DEFAULT_SMALL_STEP = 1;

	public static final int DEFAULT_BIG_STEP = 2;

	public static final float DEFAULT_SCALE_FACTOR = 1.1f;

	protected StageTreeClassifier cascade;
	protected float scaleFactor = 1.1f;
	protected int smallStep = 1;
	protected int bigStep = 2;

	/**
	 * 构造函数	 */
	public Detector(StageTreeClassifier cascade, float scaleFactor, int smallStep, int bigStep) {
		super(Math.max(cascade.width, cascade.height), 0);

		this.cascade = cascade;
		this.scaleFactor = scaleFactor;
		this.smallStep = smallStep;
		this.bigStep = bigStep;
	}

	public Detector(StageTreeClassifier cascade, float scaleFactor) {
		this(cascade, scaleFactor, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP);
	}

	public Detector(StageTreeClassifier cascade) {
		this(cascade, DEFAULT_SCALE_FACTOR, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP);
	}

	/**
	 * 根据维度检测
	 */
	protected void detectAtScale(final SummedSqTiltAreaTable sat, final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight,
			final List<Rectangle> results) {
		for (int iy = startY; iy < stopY; iy++) {
			final int y = Math.round(iy * ystep);

			for (int ix = startX, xstep = 0; ix < stopX; ix += xstep) {
				final int x = Math.round(ix * ystep);

				final int result = cascade.classify(sat, x, y);

				if (result > 0) {
					results.add(new Rectangle(x, y, windowWidth, windowHeight));
				}

				xstep = (result > 0 ? smallStep : bigStep);

			}
		}
	}

	@Override
	public List<Rectangle> detect(FImage image) {
		final List<Rectangle> results = new ArrayList<Rectangle>();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		final SummedSqTiltAreaTable sat = new SummedSqTiltAreaTable(image, cascade.hasTiltedFeatures);

		int nFactors = 0;
		int startFactor = 0;
		for (float factor = 1; factor * cascade.width < imageWidth - 10 && factor * cascade.height < imageHeight - 10; factor *= scaleFactor) {
			final float width = factor * cascade.width;
			final float height = factor * cascade.height;

			if (width < minSize || height < minSize) {
				startFactor++;
			}

			if (maxSize > 0 && (width > maxSize || height > maxSize)) {
				break;
			}

			nFactors++;
		}

		float factor = (float) Math.pow(scaleFactor, startFactor);
		for (int scaleStep = startFactor; scaleStep < nFactors; factor *= scaleFactor, scaleStep++) {
			final float ystep = Math.max(2, factor);

			final int windowWidth = (int) (factor * cascade.width);
			final int windowHeight = (int) (factor * cascade.height);

			final int startX = (int) (roi == null ? 0 : Math.max(0, roi.x));
			final int startY = (int) (roi == null ? 0 : Math.max(0, roi.y));
			final int stopX = Math
					.round((((roi == null ? imageWidth : Math.min(imageWidth, roi.x + roi.width)) - windowWidth))
							/ ystep);
			final int stopY = Math
					.round((((roi == null ? imageHeight : Math.min(imageHeight, roi.y + roi.height)) - windowHeight))
							/ ystep);

			cascade.setScale(factor);

			detectAtScale(sat, startX, stopX, startY, stopY, ystep, windowWidth, windowHeight, results);
		}

		return results;
	}

	/**
	 *  获得返回最小步数
	 */
	public int smallStep() {
		return smallStep;
	}

	/**
	 *获得返回最大步数
	 */
	public int bigStep() {
		return bigStep;
	}

	/**
	 *设置最小步数
	 */
	public void setSmallStep(int smallStep) {
		this.smallStep = smallStep;
	}

	/**
	 * 初始化步数
	 */
	public void bigStep(int bigStep) {
		this.bigStep = bigStep;
	}

	/**
	 *获得维度因子
	 */
	public float getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 *获得由检测器得到的分类树
	 */
	public StageTreeClassifier getClassifier() {
		return cascade;
	}

}
