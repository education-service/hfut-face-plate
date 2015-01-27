package edu.hfut.lpr.ann;

import java.util.Vector;

/**
 * 输入输出对
 *
 * @author wanggang
 *
 */
public class IOPair {

	Vector<Double> inputs;
	Vector<Double> outputs;

	public IOPair(Vector<Double> inputs, Vector<Double> outputs) {
		// 不能按照如下方式初始化，因为对象是按照地址拷贝的
		// this.inputs = (Vector<>)inputs.clone();
		// this.outputs = (Vector<>)outputs.clone();
		this.inputs = new Vector<>(inputs);
		this.outputs = new Vector<>(outputs);
	}

}
