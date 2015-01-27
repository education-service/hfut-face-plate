package edu.hfut.lpr.ann;


/**
 * 神经输入
 *
 * @author wanggang
 *
 */
public class NeuralInput {

	// 权重
	double weight;
	// 输入的数量
	int index;
	// 神经元
	Neuron neuron;

	NeuralInput(double weight, Neuron neuron) {
		this.neuron = neuron;
		this.weight = weight;
		this.index = this.neuron.numberOfInputs();
		// System.out.println("Created neural input "+this.index+" with weight "+this.weight);
	}

}
