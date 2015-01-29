package edu.hfut.fr.image.analysis.pyramid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * 定义图像金字塔类
 *
 * @author wanghao
 */
public abstract class Pyramid<OPTIONS extends PyramidOptions<OCTAVE, IMAGE>, OCTAVE extends Octave<OPTIONS, ?, IMAGE>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements ImageAnalyser<IMAGE>, Iterable<OCTAVE> {

	/**
	 * 类的选项
	 */
	protected OPTIONS options;

	/**
	 * octaves存储列表
	 */
	protected List<OCTAVE> octaves;

	/**
	 * 通过配置选项构造函数
	 */
	public Pyramid(OPTIONS options) {
		this.options = options;

		if (options.keepOctaves || options.pyramidProcessor != null)
			octaves = new ArrayList<OCTAVE>();
	}

	/**
	 * 处理图像，构造金字塔类
	 */
	public abstract void process(IMAGE img);

	@Override
	public void analyseImage(IMAGE image) {
		process(image);
	}

	/**
	 * 获得类的配置选项
	 */
	public OPTIONS getOptions() {
		return options;
	}

	/**
	 * 设置类的配置选项
	 */
	public void setOptions(OPTIONS options) {
		this.options = options;
	}

	/**
	 *返回octaces类的队列
	 */
	public List<OCTAVE> getOctaves() {
		return octaves;
	}

	@Override
	public Iterator<OCTAVE> iterator() {
		if (octaves == null)
			return null;
		return octaves.iterator();
	}

}
