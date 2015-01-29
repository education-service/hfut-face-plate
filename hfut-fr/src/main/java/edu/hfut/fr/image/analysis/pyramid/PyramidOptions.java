package edu.hfut.fr.image.analysis.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 构建金字塔类的基本配置选项
 *
 * @author wanghao
 */
public class PyramidOptions<OCTAVE extends Octave<?, ?, IMAGE>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>> {

	protected boolean keepOctaves = false;

	protected OctaveProcessor<OCTAVE, IMAGE> octaveProcessor;

	protected PyramidProcessor<IMAGE> pyramidProcessor = null;

	public OctaveProcessor<OCTAVE, IMAGE> getOctaveProcessor() {
		return octaveProcessor;
	}

	public PyramidProcessor<IMAGE> getPyramidProcessor() {
		return pyramidProcessor;
	}

	public boolean isKeepOctaves() {
		return keepOctaves;
	}

	public void setKeepOctaves(boolean keepOctaves) {
		this.keepOctaves = keepOctaves;
	}

	public void setOctaveProcessor(OctaveProcessor<OCTAVE, IMAGE> octaveProcessor) {
		this.octaveProcessor = octaveProcessor;
	}

	public void setPyramidProcessor(PyramidProcessor<IMAGE> pyramidProcessor) {
		this.pyramidProcessor = pyramidProcessor;
	}

}
