package edu.hfut.lpr.ann;

import java.util.Vector;

/**
 * 神经元
 *
 * @author wanggang
 *
 */
public class Neuron {

	// 神经输入列表向量
	Vector<NeuralInput> listInputs = new Vector<>();

	// 神经元数目
	int index;
	// 阈值
	public double threshold;
	// 输出值
	public double output;
	// 神经层
	NeuralLayer neuralLayer;

	// 将所有的神经元权重处置化到一个指定的参数，代表权重数量
	Neuron(double threshold, NeuralLayer neuralLayer) {
		this.threshold = threshold;
		this.neuralLayer = neuralLayer;
		this.index = this.neuralLayer.numberOfNeurons();
	}

	Neuron(int numberOfInputs, double threshold, NeuralLayer neuralLayer) {
		this.threshold = threshold;
		this.neuralLayer = neuralLayer;
		this.index = this.neuralLayer.numberOfNeurons();
		for (int i = 0; i < numberOfInputs; i++) {
			this.listInputs.add(new NeuralInput(1.0, this));
		}
	}

	public int numberOfInputs() {
		return this.listInputs.size();
	}

	public NeuralInput getInput(int index) {
		return this.listInputs.elementAt(index);
	}

}
