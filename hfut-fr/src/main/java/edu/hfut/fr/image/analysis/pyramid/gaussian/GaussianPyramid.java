package edu.hfut.fr.image.analysis.pyramid.gaussian;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.pyramid.Pyramid;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;

/**
 * 高斯金字塔类
 *
 * @author wanghao
 */
public class GaussianPyramid<I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> extends
		Pyramid<GaussianPyramidOptions<I>, GaussianOctave<I>, I> implements ImageAnalyser<I>,
		Iterable<GaussianOctave<I>> {

	/**
	 * 通过给定的参数构造
	 *
	 * @param options  参数对象
	 */
	public GaussianPyramid(GaussianPyramidOptions<I> options) {
		super(options);
	}

	@Override
	public void process(I img) {
		if (img.getWidth() <= 1 || img.getHeight() <= 1)
			throw new IllegalArgumentException("Image is too small");

		float octaveSize = 1.0f;

		I image;
		if (options.doubleInitialImage) {
			image = ResizeProcessor.doubleSize(img);
			octaveSize *= 0.5;
		} else
			image = img.clone();

		final float currentSigma = (options.doubleInitialImage ? 1.0f : 0.5f);
		if (options.initialSigma > currentSigma) {
			final float sigma = (float) Math.sqrt(options.initialSigma * options.initialSigma - currentSigma
					* currentSigma);
			image.processInplace(this.options.createGaussianBlur(sigma));
		}

		final int minImageSize = 2 + (2 * options.getBorderPixels());

		while (image.getHeight() > minImageSize && image.getWidth() > minImageSize) {
			// 构建当前的高斯octave
			final GaussianOctave<I> currentOctave = new GaussianOctave<I>(this, octaveSize);

			currentOctave.process(image);

			image = ResizeProcessor.halfSize(currentOctave.getNextOctaveImage());
			octaveSize *= 2.0;
			if (octaves != null)
				octaves.add(currentOctave);
		}
		if (options.getPyramidProcessor() != null) {
			options.getPyramidProcessor().process(this);
		}
	}

}
