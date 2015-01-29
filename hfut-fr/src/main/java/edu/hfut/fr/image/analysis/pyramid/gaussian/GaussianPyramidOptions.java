package edu.hfut.fr.image.analysis.pyramid.gaussian;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.pyramid.PyramidOptions;
import edu.hfut.fr.image.processing.convolution.FGaussianConvolve;

/**
 * 高斯金字塔参数类
 *
 * @author wanghao
 */
public class GaussianPyramidOptions<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends PyramidOptions<GaussianOctave<IMAGE>, IMAGE> {

	/**
	 * 宽度像素大小
	 */
	protected int borderPixels = 5;

	protected boolean doubleInitialImage = true;

	protected int extraScaleSteps = 2;

	/**
	 * 每个octave第一幅图片的初始sigma的值
	 */
	protected float initialSigma = 1.6f;

	/**
	 * 尺度数
	 */
	protected int scales = 3;

	/**
	 * 默认构造函数
	 */
	public GaussianPyramidOptions() {

	}

	/**
	 *构造高斯金字塔
	 * @param options
	 */
	public GaussianPyramidOptions(GaussianPyramidOptions<?> options) {
		this.borderPixels = options.borderPixels;
		this.doubleInitialImage = options.doubleInitialImage;
		this.extraScaleSteps = options.extraScaleSteps;
		this.initialSigma = options.initialSigma;
		this.keepOctaves = options.keepOctaves;
		this.scales = options.scales;
	}

	/**
	 * 得到宽度像素值
	 *
	 */
	public int getBorderPixels() {
		return borderPixels;
	}

	/**
	 *得到扩张尺度
	 *
	 */
	public int getExtraScaleSteps() {
		return extraScaleSteps;
	}

	/**
	 *获得初始化sigma值
	 * @return the initialSigma
	 */
	public float getInitialSigma() {
		return initialSigma;
	}

	/**
	 *获得尺度
	 * @return the scales
	 */
	public int getScales() {
		return scales;
	}

	public boolean isDoubleInitialImage() {
		return doubleInitialImage;
	}

	/**
	 *设置宽度像素值
	 * @param borderPixels
	 */
	public void setBorderPixels(int borderPixels) {
		if (borderPixels < 2)
			throw new IllegalArgumentException("BorderDistance must be >= 2");
		this.borderPixels = borderPixels;
	}

	/**
	 *设置两个初始对象
	 * @param doubleInitialImage
	 */
	public void setDoubleInitialImage(boolean doubleInitialImage) {
		this.doubleInitialImage = doubleInitialImage;
	}

	/**
	 *设置扩展尺度步数
	/**
	 *
	 */
	public void setExtraScaleSteps(int extraScaleSteps) {
		this.extraScaleSteps = extraScaleSteps;
	}

	/**
	 *设置初始化sigma值
	 * @param initialSigma
	 */
	public void setInitialSigma(float initialSigma) {
		this.initialSigma = initialSigma;
	}

	/**
	 *设置尺度
	 * @param scales
	 */
	public void setScales(int scales) {
		this.scales = scales;
	}

	/**
	 *获得单个带状图处理器
	 * @param sigma
	 * @return the image processor to apply the blur
	 */
	public SinglebandImageProcessor<Float, FImage> createGaussianBlur(float sigma) {
		return new FGaussianConvolve(sigma);
	}

}
