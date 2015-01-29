package edu.hfut.fr.image.analysis.pyramid.gaussian;

import java.lang.reflect.Array;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.pyramid.Octave;

/**
 * 高斯octave实现类
 *
 * @author wanghao
 */
public class GaussianOctave<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends Octave<GaussianPyramidOptions<IMAGE>, GaussianPyramid<IMAGE>, IMAGE> {

	/**
	 *构造函数
	 * @param parent  父类
	 * @param octaveSize Octave大小
	 */
	public GaussianOctave(GaussianPyramid<IMAGE> parent, float octaveSize) {
		super(parent, octaveSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void process(IMAGE image) {
		images = (IMAGE[]) Array.newInstance(image.getClass(), options.scales + options.extraScaleSteps + 1);

		final float k = (float) Math.pow(2.0, 1.0 / options.scales);

		images[0] = image;

		float prevSigma = options.initialSigma;

		for (int i = 1; i < options.scales + options.extraScaleSteps + 1; i++) {
			images[i] = images[i - 1].clone();

			final float increase = prevSigma * (float) Math.sqrt(k * k - 1.0);

			images[i].processInplace(options.createGaussianBlur(increase));

			prevSigma *= k;
		}

		if (options.getOctaveProcessor() != null)
			options.getOctaveProcessor().process(this);
	}

	/**
	 *  返回下一个octave图像
	 */
	@Override
	public IMAGE getNextOctaveImage() {
		return images[options.scales];
	}

}
