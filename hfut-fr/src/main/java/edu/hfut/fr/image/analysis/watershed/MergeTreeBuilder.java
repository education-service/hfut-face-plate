package edu.hfut.fr.image.analysis.watershed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.util.tree.TreeNode;
import org.openimaj.util.tree.TreeNodeImpl;

import edu.hfut.fr.image.analysis.watershed.event.ComponentStackMergeListener;

/**
 * 监听浇水算法实现过程
 *
 * @author wanghao
 */
public class MergeTreeBuilder implements ComponentStackMergeListener {

	Logger logger = Logger.getLogger(MergeTreeBuilder.class);

	/** 创建过程树 */
	private TreeNode<Component> tree = null;

	private Map<Component, TreeNode<Component>> map = null;

	/**
	 * 	构造函数
	 */
	public MergeTreeBuilder() {
		map = new HashMap<Component, TreeNode<Component>>();
	}

	@Override
	public void componentsMerged(Component c1, Component c2) {
		/*
		 * 打印日志
		 */
		logger.debug("Map: " + map);
		logger.debug("Component c1: " + c1);
		logger.debug("Component c2: " + c2);

		//创建树结点
		TreeNode<Component> c1xtn = map.get(c1);
		if (c1xtn == null) {
			logger.debug("c1 not found");
			c1xtn = new TreeNodeImpl<Component>();
			Component c1x = c1.clone();
			c1xtn.setValue(c1x);
			map.put(c1, c1xtn);
		}

		c1xtn.getValue().merge(c2);

		TreeNode<Component> c2xtn = map.get(c2);
		if (c2xtn == null) {
			logger.debug("c2 not found");
			c2xtn = new TreeNodeImpl<Component>();
			Component c2x = c2.clone();
			c2xtn.setValue(c2x);
			map.put(c2, c2xtn);
		}

		logger.debug("Linking " + c1xtn + " and " + c2xtn);

		// 添加树结点
		c1xtn.addChild(c2xtn);
		this.tree = c1xtn;
	}

	@Override
	public void componentPromoted(Component c1) {
		TreeNode<Component> c1xtn_old = map.get(c1);

		Component c1x = c1.clone();

		TreeNode<Component> c1xtn = new TreeNodeImpl<Component>();
		c1xtn.setValue(c1x);

		map.put(c1, c1xtn);

		if (c1xtn_old != null)
			c1xtn.addChild(c1xtn_old);
	}

	/**
	 * 返回过程树
	 */
	public TreeNode<Component> getTree() {
		return tree;
	}

}
