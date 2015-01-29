package edu.hfut.fr.image.analysis.pyramid;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.Processor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayIterator;

import edu.hfut.fr.image.processing.convolution.FGaussianConvolve;
import edu.hfut.fr.image.processing.resize.BilinearInterpolation;

/**
 * 由一系列图片所构成的简单金字塔类实现
 *
 * @author  wanggang
 */
public class SimplePyramid<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements ImageAnalyser<IMAGE>, ImageProcessor<IMAGE>, Iterable<IMAGE> {

	/**
	*  构建金字塔类所需要的类
	 */
	public IMAGE[] pyramid;

	Processor<IMAGE> processor = null;

	float power;

	/**
	 * 数量
	 */
	int nlevels;

	/**
	 *通过给定的标定因子来构建金字塔
	 */
	public SimplePyramid(float power) {
		this.power = power;
		this.nlevels = -1;
	}

	/**
	 *通过标定因子和级别的数量来构建金字塔类
	 */
	public SimplePyramid(float power, int nlevels) {
		this.power = power;
		this.nlevels = nlevels;
	}

	public SimplePyramid(float power, Processor<IMAGE> processor) {
		this.power = power;
		this.nlevels = -1;
		this.processor = processor;
	}

	/**
	 * 对应不同参数的金字塔类的构造
	 */
	public SimplePyramid(float power, int nlevels, Processor<IMAGE> processor) {
		this.power = power;
		this.nlevels = nlevels;
		this.processor = processor;
	}

	protected int computeLevels(int size) {
		int levels = 1;
		while (true) {
			size /= power;

			if (size < 8)
				break;

			levels++;
		}

		return levels;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseImage(IMAGE image) {
		if (nlevels <= 0)
			nlevels = computeLevels(Math.min(image.getWidth(), image.getHeight()));

		this.pyramid = (IMAGE[]) Array.newInstance(image.getClass(), nlevels);

		pyramid[0] = image.clone();
		for (int i = 1; i < nlevels; i++) {
			final int m = (int) Math.floor(pyramid[i - 1].getHeight() / power);
			final int n = (int) Math.floor(pyramid[i - 1].getWidth() / power);

			pyramid[i] = pyramid[i - 1].process(processor).process(new BilinearInterpolation(n, m, power));
		}
	}

	@Override
	public void processImage(IMAGE image) {
		analyseImage(image);
		image.internalAssign(pyramid[nlevels - 1]);
	}

	@Override
	public Iterator<IMAGE> iterator() {
		return new ArrayIterator<IMAGE>(pyramid);
	}

	public static <T extends Image<?, T> & SinglebandImageProcessor.Processable<Float, FImage, T>> SimplePyramid<T> createGaussianPyramid(
			T image, float sigma, int nLevels) {
		@SuppressWarnings("unchecked")
		final SimplePyramid<T> pyr = new SimplePyramid<T>(2f, nLevels, (Processor<T>) (new FGaussianConvolve(sigma)));

		image.analyseWith(pyr);
		return pyr;
	}

}
