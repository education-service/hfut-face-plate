package edu.hfut.lpr.ann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 神经网络
 *
 * 注意：这里面包含很多变量和参数
 *
 * @author wanggang
 *
 */
public class NeuralNetwork {

	private static Logger logger = LoggerFactory.getLogger(NeuralNetwork.class);

	// 神经层（或神经网络层）向量
	private Vector<NeuralLayer> listLayers = new Vector<>();
	// 随机数生成器
	private final Random RANDOM = new Random();

	public NeuralNetwork(Vector<Integer> dimensions) {
		// 初始化各层级
		for (int i = 0; i < dimensions.size(); i++) {
			this.listLayers.add(new NeuralLayer(dimensions.elementAt(i), this));
		}
		logger.info("Created neural network with " + dimensions.size() + " layers");
	}

	public NeuralNetwork(InputStream inStream) {
		this.loadFromXml(inStream);
	}

	/**
	 * 测试输入是否正确
	 */
	public Vector<Double> test(Vector<Double> inputs) {
		if (inputs.size() != this.getLayer(0).numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException("[Error] ANN-Test: You are trying to pass vector with "
					+ inputs.size() + " values into neural layer with " + this.getLayer(0).numberOfNeurons()
					+ " neurons. Consider using another network, or another descriptors.");
		} else {
			return this.activities(inputs);
		}
	}

	/**
	 * 学习阶段
	 * @param trainingSet 训练集合
	 * @param maxK 最大迭代次数
	 * @param eps 终止条件
	 * @param lambda 学习率
	 * @param micro 微调因子
	 */
	public void learn(SetOfIOPairs trainingSet, int maxK, double eps, double lambda, double micro) {
		if (trainingSet.pairs.size() == 0) {
			throw new NullPointerException(
					"[Error] NN-Learn: You are using an empty training set, neural network couldn't be trained.");
		} else if (trainingSet.pairs.elementAt(0).inputs.size() != this.getLayer(0).numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException("[Error] NN-Test: You are trying to pass vector with "
					+ trainingSet.pairs.elementAt(0).inputs.size() + " values into neural layer with "
					+ this.getLayer(0).numberOfNeurons()
					+ " neurons. Consider using another network, or another descriptors.");
		} else if (trainingSet.pairs.elementAt(0).outputs.size() != this.getLayer(this.numberOfLayers() - 1)
				.numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException("[Error] NN-Test:  You are trying to pass vector with "
					+ trainingSet.pairs.elementAt(0).inputs.size() + " values into neural layer with "
					+ this.getLayer(0).numberOfNeurons()
					+ " neurons. Consider using another network, or another descriptors.");
		} else {
			this.adaptation(trainingSet, maxK, eps, lambda, micro);
		}
	}

	/**
	 * 层级数
	 */
	public int numberOfLayers() {
		return this.listLayers.size();
	}

	/**
	 * 从XML文件中加载神经网络相关参数
	 */
	private void loadFromXml(InputStream inStream) {

		logger.info("NeuralNetwork : loading network topology from InputStream.");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder parser = factory.newDocumentBuilder();
			doc = parser.parse(inStream);
			if (doc == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Node nodeNeuralNetwork = doc.getDocumentElement();
		if (!nodeNeuralNetwork.getNodeName().equals("neuralNetwork")) {
			logger.error("[Error] NN-Load: Parse error in XML file, neural network couldn't be loaded.");
		}

		/********** 神经网络加载正确 **********/

		// 参数内容加载顺序： 神经网络参数 -> 结构化元素参数 -> 层级参数 -> 神经元参数 -> 神经输入参数
		// 神经网络参数
		NodeList nodeNeuralNetworkContent = nodeNeuralNetwork.getChildNodes();
		// 循环每个神经网络节点
		for (int innc = 0; innc < nodeNeuralNetworkContent.getLength(); innc++) {
			Node nodeStructure = nodeNeuralNetworkContent.item(innc);
			if (nodeStructure.getNodeName().equals("structure")) {
				// 结构化元素参数
				NodeList nodeStructureContent = nodeStructure.getChildNodes();
				// 循环每个结构化参数节点
				for (int isc = 0; isc < nodeStructureContent.getLength(); isc++) {
					// 层级参数
					Node nodeLayer = nodeStructureContent.item(isc);
					// 循环每个层级节点
					if (nodeLayer.getNodeName().equals("layer")) {
						NeuralLayer neuralLayer = new NeuralLayer(this);
						this.listLayers.add(neuralLayer);
						// 神经元参数
						NodeList nodeLayerContent = nodeLayer.getChildNodes();
						// 循环每个神经元节点
						for (int ilc = 0; ilc < nodeLayerContent.getLength(); ilc++) {
							Node nodeNeuron = nodeLayerContent.item(ilc);
							// 判断神经元是否在当前层级中
							if (nodeNeuron.getNodeName().equals("neuron")) {
								Neuron neuron = new Neuron(Double.parseDouble(((Element) nodeNeuron)
										.getAttribute("threshold")), neuralLayer);
								neuralLayer.listNeurons.add(neuron);
								NodeList nodeNeuronContent = nodeNeuron.getChildNodes();
								for (int inc = 0; inc < nodeNeuronContent.getLength(); inc++) {
									Node nodeNeuralInput = nodeNeuronContent.item(inc);
									/*if (nodeNeuralInput == null)
										System.out.print("-");
									else
										System.out.print("*");*/
									if (nodeNeuralInput.getNodeName().equals("input")) {
										//										logger.info("neuron at STR:" + innc + " LAY:" + isc + " NEU:" + ilc + " INP:"
										//												+ inc);
										NeuralInput neuralInput = new NeuralInput(
												Double.parseDouble(((Element) nodeNeuralInput).getAttribute("weight")),
												neuron);
										neuron.listInputs.add(neuralInput);
									}
								}
							}
						}
					}
				}
			}
		}

	}

	/**
	 * 将神经网络参数信息保存到XML文件中
	 * @param fileName 待保存文件名
	 */
	public void saveToXml(String fileName) throws ParserConfigurationException, FileNotFoundException,
			TransformerException {

		logger.info("Saving network topology to file " + fileName);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.newDocument();

		Element root = doc.createElement("neuralNetwork");
		root.setAttribute("dateOfExport", new Date().toString());
		Element layers = doc.createElement("structure");
		layers.setAttribute("numberOfLayers", Integer.toString(this.numberOfLayers()));

		// 循环每一层
		for (int il = 0; il < this.numberOfLayers(); il++) {
			Element layer = doc.createElement("layer");
			layer.setAttribute("index", Integer.toString(il));
			layer.setAttribute("numberOfNeurons", Integer.toString(this.getLayer(il).numberOfNeurons()));
			// 循环每个神经元
			for (int in = 0; in < this.getLayer(il).numberOfNeurons(); in++) {
				Element neuron = doc.createElement("neuron");
				neuron.setAttribute("index", Integer.toString(in));
				neuron.setAttribute("NumberOfInputs",
						Integer.toString(this.getLayer(il).getNeuron(in).numberOfInputs()));
				neuron.setAttribute("threshold", Double.toString(this.getLayer(il).getNeuron(in).threshold));
				// 循环每个输入
				for (int ii = 0; ii < this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
					Element input = doc.createElement("input");
					input.setAttribute("index", Integer.toString(ii));
					input.setAttribute("weight", Double.toString(this.getLayer(il).getNeuron(in).getInput(ii).weight));
					neuron.appendChild(input);
				}

				layer.appendChild(neuron);
			}

			layers.appendChild(layer);
		}

		root.appendChild(layers);
		doc.appendChild(root);

		// 保存
		File xmlOutputFile = new File(fileName);
		FileOutputStream fos;
		Transformer transformer;

		fos = new FileOutputStream(xmlOutputFile);
		// 对输出进行格式转换
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fos);
		// 将source转换到result中来进行保存
		transformer.setOutputProperty("encoding", "iso-8859-2");
		transformer.setOutputProperty("indent", "yes");
		transformer.transform(source, result);
	}

	/**
	 * 生成一个double随机数
	 */
	private double random() {
		return this.RANDOM.nextDouble();
	}

	/**
	 * 计算梯度
	 * @param gradients 梯度计算类
	 * @param inputs 输入向量
	 * @param requiredOutputs 理想输出向量
	 */
	private void computeGradient(Gradients gradients, Vector<Double> inputs, Vector<Double> requiredOutputs) {
		// Gradients gradients = new Gradients(this);
		this.activities(inputs);
		for (int il = this.numberOfLayers() - 1; il >= 1; il--) {
			// 神经层
			NeuralLayer currentLayer = this.getLayer(il);
			if (currentLayer.isLayerTop()) {
				// gradients.thresholds.add(il, new Vector<Double>());
				// 循环每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					Neuron currentNeuron = currentLayer.getNeuron(in);
					gradients.setThreshold(il, in, currentNeuron.output * (1 - currentNeuron.output)
							* (currentNeuron.output - requiredOutputs.elementAt(in)));
				}
				// 循环每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					Neuron currentNeuron = currentLayer.getNeuron(in);
					// 循环每个神经元的输入
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
						//						NeuralInput currentInput = currentNeuron.getInput(ii);
						gradients.setWeight(il, in, ii, gradients.getThreshold(il, in)
								* currentLayer.lowerLayer().getNeuron(ii).output);
					}
				}
			} else {
				// gradients.thresholds.add(il, new Vector<Double>());
				// 循环每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					double aux = 0;
					for (int ia = 0; ia < currentLayer.upperLayer().numberOfNeurons(); ia++) {
						aux += gradients.getThreshold(il + 1, ia)
								* currentLayer.upperLayer().getNeuron(ia).getInput(in).weight;
					}
					gradients.setThreshold(il, in, currentLayer.getNeuron(in).output
							* (1 - currentLayer.getNeuron(in).output) * aux);
				}
				// 循环每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					Neuron currentNeuron = currentLayer.getNeuron(in);
					// 循环每个神经元的输入
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
						//						NeuralInput currentInput = currentNeuron.getInput(ii);
						gradients.setWeight(il, in, ii, gradients.getThreshold(il, in)
								* currentLayer.lowerLayer().getNeuron(ii).output);
					}
				}
			}
		}
		// return gradients;
	}

	/**
	 * 计算所有的梯度
	 * @param totalGradients 全部梯度
	 * @param partialGradients 部分梯度
	 * @param trainingSet 训练集合
	 */
	private void computeTotalGradient(Gradients totalGradients, Gradients partialGradients, SetOfIOPairs trainingSet) {
		// 重置所有梯度
		totalGradients.resetGradients();
		// partialGradients.resetGradients();
		// Gradients totalGradients = new Gradients(this);
		// Gradients partialGradients = new Gradients(this);
		// 循环每个输入输出对
		for (IOPair pair : trainingSet.pairs) {
			// partialGradients = computeGradient(pair.inputs, pair.outputs);
			this.computeGradient(partialGradients, pair.inputs, pair.outputs);
			// 循环每个神经层
			for (int il = this.numberOfLayers() - 1; il >= 1; il--) {
				NeuralLayer currentLayer = this.getLayer(il);
				// 循环每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					// 增加阈值
					totalGradients.incrementThreshold(il, in, partialGradients.getThreshold(il, in));
					// 循环每层的每个神经元，增加权重
					for (int ii = 0; ii < currentLayer.lowerLayer().numberOfNeurons(); ii++) {
						totalGradients.incrementWeight(il, in, ii, partialGradients.getWeight(il, in, ii));
					}
				}

			}
		}
		// return totalGradients;
	}

	/**
	 * 自适应调整
	 * @param trainingSet 训练集合
	 * @param maxK 最大迭代次数
	 * @param eps 终止条件
	 * @param lambda 学习率
	 * @param micro 微调因子
	 */
	private void adaptation(SetOfIOPairs trainingSet, int maxK, double eps, double lambda, double micro) {

		double delta;
		Gradients deltaGradients = new Gradients(this);
		Gradients totalGradients = new Gradients(this);
		Gradients partialGradients = new Gradients(this);

		logger.info("Setting up random weights and thresholds ...");

		// 循环每一层
		for (int il = this.numberOfLayers() - 1; il >= 1; il--) {
			NeuralLayer currentLayer = this.getLayer(il);
			// 循环每个神经元
			for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
				Neuron currentNeuron = currentLayer.getNeuron(in);
				currentNeuron.threshold = (2 * this.random()) - 1;
				// deltaGradients.setThreshold(il,in,0.0);
				for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
					currentNeuron.getInput(ii).weight = (2 * this.random()) - 1;
					// deltaGradients.setWeight(il,in,ii,0.0);
				}
			}
		}

		int currK = 0;
		double currE = Double.POSITIVE_INFINITY;

		logger.info("Entering adaptation loop ... (maxK = {})", maxK);

		// 如果没有达到最大迭代次数或者终止条件继续执行迭代
		while ((currK < maxK) && (currE > eps)) {
			this.computeTotalGradient(totalGradients, partialGradients, trainingSet);
			// 循环每层
			for (int il = this.numberOfLayers() - 1; il >= 1; il--) {
				NeuralLayer currentLayer = this.getLayer(il);
				// 循环每层的每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					Neuron currentNeuron = currentLayer.getNeuron(in);
					delta = (-lambda * totalGradients.getThreshold(il, in))
							+ (micro * deltaGradients.getThreshold(il, in));
					currentNeuron.threshold += delta;
					deltaGradients.setThreshold(il, in, delta);
				}
				// 循环每层的每个神经元
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) {
					Neuron currentNeuron = currentLayer.getNeuron(in);
					// 循环每个输入
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
						delta = (-lambda * totalGradients.getWeight(il, in, ii))
								+ (micro * deltaGradients.getWeight(il, in, ii));
						currentNeuron.getInput(ii).weight += delta;
						deltaGradients.setWeight(il, in, ii, delta);
					}
				}
			}

			currE = totalGradients.getGradientAbs();
			currK++;
			if ((currK % 25) == 0) {
				logger.info("currK={}, currE={}", currK, currE);
			}
		}
	}

	/**
	 * 激活输入向量
	 * @param inputs 输入向量
	 */
	private Vector<Double> activities(Vector<Double> inputs) {
		// 循环每层
		for (int il = 0; il < this.numberOfLayers(); il++) {
			// 循环每个神经元
			for (int in = 0; in < this.getLayer(il).numberOfNeurons(); in++) {
				double sum = this.getLayer(il).getNeuron(in).threshold;
				// 计算域值总和
				// 循环每个输入
				for (int ii = 0; ii < this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
					if (il == 0) {
						sum += this.getLayer(il).getNeuron(in).getInput(ii).weight * inputs.elementAt(in);
					} else {
						sum += this.getLayer(il).getNeuron(in).getInput(ii).weight
								* this.getLayer(il - 1).getNeuron(ii).output;
					}
				}
				/*if (il == 0)
					this.getLayer(il).getNeuron(in).output = sum;
				else*/
				// 计算增益
				this.getLayer(il).getNeuron(in).output = this.gainFunction(sum);
				//				this.getLayer(il).getNeuron(in).output = this.gainFunction(sum);
			}
		}
		Vector<Double> output = new Vector<>();

		for (int i = 0; i < this.getLayer(this.numberOfLayers() - 1).numberOfNeurons(); i++) {
			output.add(this.getLayer(this.numberOfLayers() - 1).getNeuron(i).output);
		}

		return output;
	}

	/**
	 * 增益函数
	 */
	private double gainFunction(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	/**
	 * 获取指定神经层
	 */
	NeuralLayer getLayer(int index) {
		return this.listLayers.elementAt(index);
	}

	/**
	 * 打印神经网络信息
	 */
	//	public void printNeuralNetwork() {
	//		for (int il = 0; il < this.numberOfLayers(); il++) {
	//			System.out.println("Layer " + il);
	//			for (int in = 0; in < this.getLayer(il).numberOfNeurons(); in++) {
	//				System.out.print("      Neuron " + in + " (threshold=" + this.getLayer(il).getNeuron(in).threshold
	//						+ ") : ");
	//				for (int ii = 0; ii < this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
	//					System.out.print(this.getLayer(il).getNeuron(in).getInput(ii).weight + " ");
	//				}
	//				System.out.println();
	//			}
	//		}
	//	}

}