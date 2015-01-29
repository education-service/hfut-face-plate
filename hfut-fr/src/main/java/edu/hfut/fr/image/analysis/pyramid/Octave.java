package edu.hfut.fr.image.analysis.pyramid;

import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayIterator;

/**
 * Octave 抽象类：图像参数空间
 *
 * @author wanghao
 */
public abstract class Octave<OPTIONS extends PyramidOptions<?, IMAGE>, PYRAMID extends Pyramid<OPTIONS, ?, IMAGE>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements Iterable<IMAGE> {

	public OPTIONS options;

	/** 存储图像数组*/
	public IMAGE[] images;

	/**父类的金字塔类**/
	public PYRAMID parentPyramid;

	/** 原始图像参数空间大小 */
	public float octaveSize;

	/**
	 * 构造高斯Octave参数空间
	 */
	public Octave(PYRAMID parent, float octaveSize) {
		parentPyramid = parent;
		if (parent != null)
			this.options = parent.options;
		this.octaveSize = octaveSize;
	}

	/**
	 */
	public abstract void process(IMAGE image);

	/**
	 * 获得Octave参数空间的下一个图像
	 */
	public abstract IMAGE getNextOctaveImage();

	@Override
	public Iterator<IMAGE> iterator() {
		return new ArrayIterator<IMAGE>(images);
	}

}
