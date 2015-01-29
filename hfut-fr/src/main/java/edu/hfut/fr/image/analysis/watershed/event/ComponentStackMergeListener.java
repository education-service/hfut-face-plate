package edu.hfut.fr.image.analysis.watershed.event;

import java.util.EventListener;

import edu.hfut.fr.image.analysis.watershed.Component;

/**
 * 事件监听接口
 *
 * @author wanghao
 */
public interface ComponentStackMergeListener extends EventListener {

	/**
	 * 区域合并
	 */
	public void componentsMerged(Component c1, Component c2);

	/**
	 * 提升区域密度值
	 */
	public void componentPromoted(Component c1);

}
