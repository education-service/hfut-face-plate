package edu.hfut.lpr.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import edu.hfut.lpr.images.Char;
import edu.hfut.lpr.utils.ConfigUtil;

/**
 * KNN分类器
 *
 * @author wanggang
 *
 */
public class KNNClassificator extends CharRecognizer {

	// 学习向量
	Vector<Vector<Double>> learnVectors;

	public KNNClassificator() {

		String path = ConfigUtil.getConfigurator().getPathProperty("char_learnAlphabetPath");

		this.learnVectors = new Vector<Vector<Double>>(36);

		ArrayList<String> filenames = (ArrayList<String>) Char.getAlphabetList(path);

		for (String fileName : filenames) {
			InputStream is = ConfigUtil.getConfigurator().getResourceAsStream(fileName);

			Char imgChar = null;

			try {
				imgChar = new Char(is);

			} catch (IOException e) {
				System.err.println("Failed to load Char: " + fileName);
				e.printStackTrace();
			}
			imgChar.normalize();
			this.learnVectors.add(imgChar.extractFeatures());
		}

		for (int i = 0; i < this.learnVectors.size(); i++) {
			if (this.learnVectors.elementAt(i) == null) {
				System.err.println("Warning : alphabet in " + path + " is not complete");
			}
		}

	}

	/**
	 * 字符识别
	 */
	@Override
	public RecognizedChar recognize(Char chr) {

		Vector<Double> tested = chr.extractFeatures();
		//		int minx = 0;
		//		float minfx = Float.POSITIVE_INFINITY;

		RecognizedChar recognized = new RecognizedChar();

		for (int x = 0; x < this.learnVectors.size(); x++) {
			float fx = this.simplifiedEuclideanDistance(tested, this.learnVectors.elementAt(x));

			recognized.addPattern(recognized.new RecognizedPattern(alphabet[x], fx));

			/*if (fx < minfx) {
				minfx = fx;
				minx = x;
			}*/
		}
		//		return new RecognizedChar(this.alphabet[minx], minfx);
		recognized.sort(0);

		return recognized;
	}

	/**
	 * 明氏距离
	 */
	@SuppressWarnings("unused")
	private float difference(Vector<Double> vectorA, Vector<Double> vectorB) {
		float diff = 0;
		for (int x = 0; x < vectorA.size(); x++) {
			diff += Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
		}
		return diff;
	}

	/**
	 * 简单的欧几里得距离
	 */
	private float simplifiedEuclideanDistance(Vector<Double> vectorA, Vector<Double> vectorB) {
		float diff = 0;
		float partialDiff;
		for (int x = 0; x < vectorA.size(); x++) {
			partialDiff = (float) Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
			diff += partialDiff * partialDiff;
		}
		return diff;
	}

}