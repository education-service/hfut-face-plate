package edu.hfut.fr.image.analysis.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.analysis.pyramid.gaussian.GaussianPyramid;

/**
 * 使图片使用金字塔算法前的处理接口
 *
 * @author wanghao
 */
public interface PyramidProcessor<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>> {

	/**
	 * Process the given pyramid.
	 *
	 * @param pyramid the pyramid.
	 */
	public void process(GaussianPyramid<IMAGE> pyramid);

}
