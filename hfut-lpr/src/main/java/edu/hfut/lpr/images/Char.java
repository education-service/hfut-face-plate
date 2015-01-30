package edu.hfut.lpr.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.hfut.lpr.core.CharacterRecognizer;
import edu.hfut.lpr.utils.Configurator;

/**
 * 车牌中字符图像
 *
 * @author wanggang
 *
 */
public class Char extends Photo {

	public boolean normalized = false;
	public PositionInPlate positionInPlate = null;

	public int fullWidth, fullHeight, pieceWidth, pieceHeight;

	public float statisticAverageBrightness;
	public float statisticMinimumBrightness;
	public float statisticMaximumBrightness;
	public float statisticContrast;
	public float statisticAverageHue;
	public float statisticAverageSaturation;

	public BufferedImage thresholdedImage;

	public Char(BufferedImage bi, BufferedImage thresholdedImage, PositionInPlate positionInPlate) {
		super(bi);
		this.thresholdedImage = thresholdedImage;
		this.positionInPlate = positionInPlate;
		this.init();
	}

	public Char(BufferedImage bi) {
		this(bi, bi, null);
		this.init();
	}

	/**
	 * 构造函数
	 * @param fileName 字符文件名
	 * @throws IOException
	 */
	public Char(String fileName) throws IOException {
		super(Configurator.getConfigurator().getResourceAsStream(fileName));
		// this.thresholdedImage = this.image;
		BufferedImage origin = Photo.duplicateBufferedImage(this.image);
		this.adaptiveThresholding();
		this.thresholdedImage = this.image;
		this.image = origin;

		this.init();
	}

	/**
	 * 构造函数
	 * @param is 输入流
	 * @throws IOException
	 */
	public Char(InputStream is) throws IOException {
		super(is);
		// this.thresholdedImage = this.image;
		BufferedImage origin = Photo.duplicateBufferedImage(this.image);
		this.adaptiveThresholding();
		this.thresholdedImage = this.image;
		this.image = origin;

		this.init();
	}

	/**
	 * 字符图像克隆
	 */
	@Override
	public Char clone() throws CloneNotSupportedException {
		super.clone();
		return new Char(duplicateBufferedImage(this.image), duplicateBufferedImage(this.thresholdedImage),
				this.positionInPlate);
	}

	/**
	 * 初始图像长宽
	 */
	private void init() {
		this.fullWidth = super.getWidth();
		this.fullHeight = super.getHeight();
	}

	/**
	 * 图像标准化
	 */
	public void normalize() {

		if (this.normalized) {
			return;
		}

		BufferedImage colorImage = duplicateBufferedImage(this.getBi());
		this.image = this.thresholdedImage;

		//		boolean flag = false;
		//		for (int x = 0; x < this.getWidth(); x++)
		//			if (this.getBrightness(x, 0) > 0.5f)
		//				flag = true;
		//		if (flag == false)
		//			for (int x = 0; x < this.getWidth(); x++)
		//				this.setBrightness(x, 0, 1.0f);

		PixelMap pixelMap = this.getPixelMap();

		PixelMap.Piece bestPiece = pixelMap.getBestPiece();

		colorImage = this.getBestPieceInFullColor(colorImage, bestPiece);

		this.computeStatisticBrightness(colorImage);
		this.computeStatisticContrast(colorImage);
		this.computeStatisticHue(colorImage);
		this.computeStatisticSaturation(colorImage);

		this.image = bestPiece.render();

		if (this.image == null) {
			this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}

		this.pieceWidth = super.getWidth();
		this.pieceHeight = super.getHeight();

		this.normalizeResizeOnly();
		this.normalized = true;
	}

	/**
	 * 获取真彩图像中最好的块
	 * @param bi 真彩图像
	 * @param piece 像素块
	 * @return
	 */
	private BufferedImage getBestPieceInFullColor(BufferedImage bi, PixelMap.Piece piece) {
		if ((piece.width <= 0) || (piece.height <= 0)) {
			return bi;
		}
		return bi.getSubimage(piece.mostLeftPoint, piece.mostTopPoint, piece.width, piece.height);
	}

	/**
	 * 对图像进行大小标准化
	 */
	private void normalizeResizeOnly() {
		int x = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_x");
		int y = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_y");
		if ((x == 0) || (y == 0)) {
			return;
			// this.linearResize(x,y);
		}

		if (Configurator.getConfigurator().getIntProperty("char_resizeMethod") == 0) {
			this.linearResize(x, y);
		} else {
			this.averageResize(x, y);
		}

		this.normalizeBrightness(0.5f);
	}

	/**
	 * 计算统计的对比度或反差
	 */
	private void computeStatisticContrast(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Math.abs(this.statisticAverageBrightness - Photo.getBrightness(bi, x, y));
			}
		}

		this.statisticContrast = sum / (w * h);
	}

	/**
	 * 计算统计的亮度值
	 */
	private void computeStatisticBrightness(BufferedImage bi) {
		float sum = 0;
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;

		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float value = Photo.getBrightness(bi, x, y);
				sum += value;
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}
		this.statisticAverageBrightness = sum / (w * h);
		this.statisticMinimumBrightness = min;
		this.statisticMaximumBrightness = max;
	}

	/**
	 * 计算统计的色度
	 */
	private void computeStatisticHue(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Photo.getHue(bi, x, y);
			}
		}
		this.statisticAverageHue = sum / (w * h);
	}

	/**
	 * 计算统计的饱和度
	 */
	private void computeStatisticSaturation(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Photo.getSaturation(bi, x, y);
			}
		}
		this.statisticAverageSaturation = sum / (w * h);
	}

	/**
	 * 获取像素映射表
	 */
	public PixelMap getPixelMap() {
		return new PixelMap(this);
	}

	/**
	 * 提取图像边缘特征信息
	 */
	public Vector<Double> extractEdgeFeatures() {

		int w = this.image.getWidth();
		int h = this.image.getHeight();
		double featureMatch;

		float[][] array = this.bufferedImageToArrayWithBounds(this.image, w, h);
		w += 2;
		h += 2;

		float[][] features = CharacterRecognizer.features;
		// Vector<Double> output = new Vector<Double>(features.length*4);
		double[] output = new double[features.length * 4];

		for (int f = 0; f < features.length; f++) {
			for (int my = 0; my < (h - 1); my++) {
				for (int mx = 0; mx < (w - 1); mx++) {
					featureMatch = 0;
					featureMatch += Math.abs(array[mx][my] - features[f][0]);
					featureMatch += Math.abs(array[mx + 1][my] - features[f][1]);
					featureMatch += Math.abs(array[mx][my + 1] - features[f][2]);
					featureMatch += Math.abs(array[mx + 1][my + 1] - features[f][3]);

					int bias = 0;
					if (mx >= (w / 2)) {
						bias += features.length;
					}
					if (my >= (h / 2)) {
						bias += features.length * 2;
					}
					output[bias + f] += featureMatch < 0.05 ? 1 : 0;
				}
			}
		}
		Vector<Double> outputVector = new Vector<>();
		for (Double value : output) {
			outputVector.add(value);
		}

		return outputVector;
	}

	/**
	 * 提取映射特征信息
	 */
	public Vector<Double> extractMapFeatures() {
		Vector<Double> vectorInput = new Vector<>();
		for (int y = 0; y < this.getHeight(); y++) {
			for (int x = 0; x < this.getWidth(); x++) {
				vectorInput.add((double) this.getBrightness(x, y));
			}
		}
		return vectorInput;
	}

	/**
	 * 提取特征信息
	 */
	public Vector<Double> extractFeatures() {
		int featureExtractionMethod = Configurator.getConfigurator().getIntProperty("char_featuresExtractionMethod");
		if (featureExtractionMethod == 0) {
			return this.extractMapFeatures();
		} else {
			return this.extractEdgeFeatures();
		}
	}

	/**
	 * 获取后缀
	 * @param directoryName 目录名
	 * @return
	 */
	private static String getSuffix(String directoryName) {
		if (directoryName.endsWith("/")) {
			directoryName = directoryName.substring(0, directoryName.length() - 1);
		}

		return directoryName.substring(directoryName.lastIndexOf('_'));
	}

	/**
	 * 获取数字字母列表
	 */
	public static List<String> getAlphabetList(String directory) {
		final String alphaString = "0123456789abcdefghijklmnopqrstuvwxyz";
		final String suffix = getSuffix(directory);

		if (directory.endsWith("/")) {
			directory = directory.substring(0, directory.length() - 1);
		}

		ArrayList<String> filenames = new ArrayList<>();

		String s;
		for (int i = 0; i < alphaString.length(); i++) {
			s = directory + File.separator + alphaString.charAt(i) + suffix + ".jpg";

			if (Configurator.getConfigurator().getResourceAsStream(s) != null) {
				filenames.add(s);
			}
		}

		return filenames;
	}

}