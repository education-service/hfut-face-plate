package edu.hfut.lpr.ann;

import java.util.Vector;

/**
 * 神经层
 *
 * @author wanggang
 *
 */
public class NeuralLayer {

	// 神经元向量
	Vector<Neuron> listNeurons = new Vector<>();

	// 层级数
	int index;

	// 神经网络
	NeuralNetwork neuralNetwork;

	NeuralLayer(NeuralNetwork neuralNetwork) {
		this.neuralNetwork = neuralNetwork;
		this.index = this.neuralNetwork.numberOfLayers();
	}

	// 初始化该神经层中的所有神经元
	NeuralLayer(int numberOfNeurons, NeuralNetwork neuralNetwork) {
		this.neuralNetwork = neuralNetwork;
		this.index = this.neuralNetwork.numberOfLayers();
		for (int i = 0; i < numberOfNeurons; i++) {
			if (this.index == 0) {
				this.listNeurons.add(new Neuron(1, 0.0, this));
			} else {
				this.listNeurons.add(new Neuron(this.neuralNetwork.getLayer(this.index - 1).numberOfNeurons(), 0.0,
						this));
			}
		}
		//		System.out.println("Created neural layer " + this.index + " with " + numberOfNeurons + " neurons");
	}

	/**
	 * 神经元数量
	 */
	public int numberOfNeurons() {
		return this.listNeurons.size();
	}

	/**
	 * 是否为隐含层最后一层
	 */
	public boolean isLayerTop() {
		return (this.index == (this.neuralNetwork.numberOfLayers() - 1));
	}

	/**
	 * 是否为隐含层第一层
	 */
	public boolean isLayerBottom() {
		return (this.index == 0);
	}

	/**
	 * 隐含层向上进一层
	 */
	public NeuralLayer upperLayer() {
		if (this.isLayerTop()) {
			return null;
		}
		return this.neuralNetwork.getLayer(this.index + 1);
	}

	/**
	 * 隐含层向下降一层
	 */
	public NeuralLayer lowerLayer() {
		if (this.isLayerBottom()) {
			return null;
		}
		return this.neuralNetwork.getLayer(this.index - 1);
	}

	/**
	 * 获取当前神经层的某个神经元
	 */
	public Neuron getNeuron(int index) {
		return this.listNeurons.elementAt(index);
	}

}
