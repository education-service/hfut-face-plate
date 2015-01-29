package edu.hfut.fr.image.analysis.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * OctaceProcessor的接口，时期能够在同一个参数空间
 *
 * @author wanghao
 */
public interface OctaveProcessor<OCTAVE extends Octave<?, ?, IMAGE>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>> {

	/**
	 * 处理提供的参数空间
	 */
	public void process(OCTAVE octave);

}
