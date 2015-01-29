package edu.hfut.fr.image.analysis.watershed;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.IntValuePixel;

import edu.hfut.fr.image.analysis.watershed.event.ComponentStackMergeListener;
import edu.hfut.fr.image.analysis.watershed.feature.ComponentFeature;

/**
 * 浇水算法实现过程
 *
 * @author wanghao
 */
public class WatershedProcessor {

	/**开始浇水像素点 */
	private IntValuePixel startPixel = new IntValuePixel(0, 0);

	/** 获得堆栈内容 */
	private List<ComponentStackMergeListener> csmListeners = null;

	private Class<? extends ComponentFeature>[] featureClasses;

	/**
	 * 	watershedProcessor默认构造函数
	 */
	@SuppressWarnings("unchecked")
	public WatershedProcessor(Class<? extends ComponentFeature>... featureClasses) {
		this.csmListeners = new ArrayList<ComponentStackMergeListener>();
		this.featureClasses = featureClasses;
	}

	/**
	 *	处理图像.
	 */
	public void processImage(FImage greyscaleImage) {
		WatershedProcessorAlgorithm d = new WatershedProcessorAlgorithm(greyscaleImage, this.startPixel, featureClasses);

		for (ComponentStackMergeListener csm : csmListeners)
			d.addComponentStackMergeListener(csm);

		d.startPour();
	}

	/**
	 * 增加Componentstackmerge监听者
	 */
	public void addComponentStackMergeListener(ComponentStackMergeListener csml) {
		csmListeners.add(csml);
	}

}
