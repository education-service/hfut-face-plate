package edu.hfut.lpr.ann;

import java.util.Vector;

/**
 * 输入输出对集合向量
 *
 * @author wanggang
 *
 */
public class SetOfIOPairs {

	Vector<IOPair> pairs;

	public SetOfIOPairs() {
		this.pairs = new Vector<>();
	}

	public void addIOPair(Vector<Double> inputs, Vector<Double> outputs) {
		this.addIOPair(new IOPair(inputs, outputs));
	}

	public void addIOPair(IOPair pair) {
		this.pairs.add(pair);
	}

	int size() {
		return this.pairs.size();
	}

}
