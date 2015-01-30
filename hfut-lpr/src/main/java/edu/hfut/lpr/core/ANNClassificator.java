package edu.hfut.lpr.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import edu.hfut.lpr.ann.IOPair;
import edu.hfut.lpr.ann.NeuralNetwork;
import edu.hfut.lpr.ann.SetOfIOPairs;
import edu.hfut.lpr.images.Char;
import edu.hfut.lpr.utils.ConfUtil;

/**
 * 神经网络分类器
 *
 * @author wanggang
 *
 */
public class ANNClassificator extends CharRecognizer {

	// 字符标准化x维度值
	private static int normalize_x = ConfUtil.getConfigurator().getIntProperty("char_normalizeddimensions_x");
	// 字符标准化y维度值
	private static int normalize_y = ConfUtil.getConfigurator().getIntProperty("char_normalizeddimensions_y");

	// 神经网络规模： 10 x 16 = 160
	public NeuralNetwork network;

	// 通过加载配置文件来获取训练后的网络模型
	public ANNClassificator() {
		this(false);
	}

	public ANNClassificator(boolean learn) {

		ConfUtil configurator = ConfUtil.getConfigurator();

		//		this.normalize_x = configurator.getIntProperty("char_normalizeddimensions_x");
		//		this.normalize_y = configurator.getIntProperty("char_normalizeddimensions_y");

		Vector<Integer> dimensions = new Vector<Integer>();

		// 根据选取的特征提取方法来确定输入层大小
		int inputLayerSize;
		if (configurator.getIntProperty("char_featuresExtractionMethod") == 0) {
			inputLayerSize = ANNClassificator.normalize_x * ANNClassificator.normalize_y;
		} else {
			inputLayerSize = CharRecognizer.features.length * 4;
		}

		// 使用指定的维度构造新的神经网络
		dimensions.add(inputLayerSize);
		dimensions.add(configurator.getIntProperty("neural_topology"));
		dimensions.add(CharRecognizer.alphabet.length);
		this.network = new NeuralNetwork(dimensions);

		// 网络学习阶段
		if (learn) {
			String learnAlphabetPath = configurator.getStrProperty("char_learnAlphabetPath");
			try {
				this.learnAlphabet(learnAlphabetPath);
			} catch (IOException e) {
				System.err.println("Failed to load alphabet: " + learnAlphabetPath);
				e.printStackTrace();
			}
		} else {
			// 或者从配置文件中加载所需参数
			String neuralNetPath = configurator.getPathProperty("char_neuralNetworkPath");
			InputStream is = configurator.getResourceAsStream(neuralNetPath);
			this.network = new NeuralNetwork(is);
		}

	}

	// 字符识别
	@Override
	public RecognizedChar recognize(Char imgChar) {
		imgChar.normalize();
		Vector<Double> output = this.network.test(imgChar.extractFeatures());
		//		double max = 0.0;
		//		int indexMax = 0;

		RecognizedChar recognized = new RecognizedChar();

		for (int i = 0; i < output.size(); i++) {
			recognized.addPattern(recognized.new RecognizedPattern(alphabet[i], output.elementAt(i).floatValue()));
		}
		recognized.render();
		recognized.sort(1);

		return recognized;
	}

	/*public Vector<Double> imageToVector(Char imgChar) {
		Vector<Double> vectorInput = new Vector<Double>();
		for (int x = 0; x < imgChar.getWidth(); x++)
			for (int y = 0; y < imgChar.getHeight(); y++)
				vectorInput.add(new Double(imgChar.getBrightness(x, y)));
		return vectorInput;
	}*/

	/**
	 * 创建新的输入输出对
	 * @param chr 字符
	 * @param imgChar 字符图像
	 */
	public IOPair createNewPair(char chr, Char imgChar) {

		Vector<Double> vectorInput = imgChar.extractFeatures();

		Vector<Double> vectorOutput = new Vector<Double>();
		for (int i = 0; i < alphabet.length; i++) {
			if (chr == alphabet[i]) {
				vectorOutput.add(1.0);
			} else {
				vectorOutput.add(0.0);
			}
		}

		/*System.out.println();
		for (Double d : vectorInput)
			System.out.print(d + " ");
		System.out.println();
		for (Double d : vectorOutput)
			System.out.print(d + " ");
		System.out.println();*/

		return new IOPair(vectorInput, vectorOutput);
	}

	/**
	 * 数字字母学习
	 * @param folder 标准化数字字母图片目录
	 */
	public void learnAlphabet(String folder) throws IOException {

		SetOfIOPairs train = new SetOfIOPairs();

		ArrayList<String> fileList = (ArrayList<String>) Char.getAlphabetList(folder);

		for (String fileName : fileList) {
			InputStream is = ConfUtil.getConfigurator().getResourceAsStream(fileName);

			Char imgChar = new Char(is);
			imgChar.normalize();
			train.addIOPair(this.createNewPair(fileName.toUpperCase().charAt(0), imgChar));

			is.close();
		}

		this.network.learn(train, ConfUtil.getConfigurator().getIntProperty("neural_maxk"), ConfUtil
				.getConfigurator().getDoubleProperty("neural_eps"),
				ConfUtil.getConfigurator().getDoubleProperty("neural_lambda"), ConfUtil.getConfigurator()
						.getDoubleProperty("neural_micro"));
	}

}