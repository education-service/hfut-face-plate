package edu.hfut.lpr.ann;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 梯度计算
 *
 * @author wanggang
 *
 */
public class Gradients {

	private static Logger logger = LoggerFactory.getLogger(Gradients.class);

	// 每层的阈值向量
	Vector<Vector<Double>> thresholds;
	// 每层的权重矩阵
	Vector<Vector<Vector<Double>>> weights;
	// 神经网络
	NeuralNetwork neuralNetwork;

	Gradients(NeuralNetwork network) {
		this.neuralNetwork = network;
		this.initGradients();
	}

	/**
	 * 初始化梯度
	 */
	public void initGradients() {
		this.thresholds = new Vector<>();
		this.weights = new Vector<>();
		logger.info("Init for threshold gradient " + this.toString());
		// 循环每层
		for (int il = 0; il < this.neuralNetwork.numberOfLayers(); il++) {
			this.thresholds.add(new Vector<Double>());
			this.weights.add(new Vector<Vector<Double>>());
			// 循环每个神经元
			for (int in = 0; in < this.neuralNetwork.getLayer(il).numberOfNeurons(); in++) {
				this.thresholds.elementAt(il).add(0.0);
				this.weights.elementAt(il).add(new Vector<Double>());
				// 循环每个输入
				for (int ii = 0; ii < this.neuralNetwork.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
					this.weights.elementAt(il).elementAt(in).add(0.0);
				}
			}
		}
	}

	/**
	 * 重置梯度域值为0
	 */
	public void resetGradients() {
		for (int il = 0; il < this.neuralNetwork.numberOfLayers(); il++) {
			for (int in = 0; in < this.neuralNetwork.getLayer(il).numberOfNeurons(); in++) {
				this.setThreshold(il, in, 0.0);
				for (int ii = 0; ii < this.neuralNetwork.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
					this.setWeight(il, in, ii, 0.0);
				}
			}
		}
	}

	/**
	 * 获取域值
	 * @param il 层级
	 * @param in 神经元
	 */
	public double getThreshold(int il, int in) {
		return this.thresholds.elementAt(il).elementAt(in);
	}

	/**
	 * 设置域值
	 * @param il 层级
	 * @param in 神经元
	 */
	public void setThreshold(int il, int in, double value) {
		this.thresholds.elementAt(il).setElementAt(value, in);
	}

	/**
	 * 增加域值
	 * @param il 层级
	 * @param in 神经元
	 * @param value 域值
	 */
	public void incrementThreshold(int il, int in, double value) {
		this.setThreshold(il, in, this.getThreshold(il, in) + value);
	}

	/**
	 * 获取权重
	 * @param il 层级
	 * @param in 神经元
	 * @param ii 输入
	 */
	public double getWeight(int il, int in, int ii) {
		return this.weights.elementAt(il).elementAt(in).elementAt(ii);
	}

	/**
	 * 设置权重
	 * @param il 层级
	 * @param in 神经元
	 * @param ii 输入
	 * @param value 权值
	 */
	public void setWeight(int il, int in, int ii, double value) {
		this.weights.elementAt(il).elementAt(in).setElementAt(value, ii);
	}

	/**
	 * 增加权重
	 * @param il 层级
	 * @param in 神经元
	 * @param ii 输入
	 * @param value 权值
	 */
	public void incrementWeight(int il, int in, int ii, double value) {
		this.setWeight(il, in, ii, this.getWeight(il, in, ii) + value);
	}

	/**
	 * 获取梯度绝对值
	 */
	public double getGradientAbs() {
		double currE = 0;

		for (int il = 1; il < this.neuralNetwork.numberOfLayers(); il++) {
			currE += this.vectorAbs(this.thresholds.elementAt(il));
			currE += this.doubleVectorAbs(this.weights.elementAt(il));
		}
		/*for (Vector<Double> vector : this.thresholds)
			currE += this.vectorAbs(vector);
		for (Vector<Vector<Double>> doubleVector : this.weights)
			currE += this.doubleVectorAbs(doubleVector);*/
		return currE;
	}

	/**
	 * 双层向量中值的平方根
	 */
	private double doubleVectorAbs(Vector<Vector<Double>> doubleVector) {
		double totalX = 0;
		for (Vector<Double> vector : doubleVector) {
			totalX += Math.pow(this.vectorAbs(vector), 2);
		}
		return Math.sqrt(totalX);
	}

	/**
	 * 单层向量中值的平方根
	 */
	private double vectorAbs(Vector<Double> vector) {
		double totalX = 0;
		for (Double x : vector) {
			totalX += Math.pow(x, 2);
		}
		return Math.sqrt(totalX);
	}

}
