package edu.hfut.lpr.intelligence;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.hfut.lpr.analysis.Band;
import edu.hfut.lpr.analysis.CarSnapshot;
import edu.hfut.lpr.analysis.Char;
import edu.hfut.lpr.analysis.HoughTransformation;
import edu.hfut.lpr.analysis.Photo;
import edu.hfut.lpr.analysis.Plate;
import edu.hfut.lpr.gui.utils.TimeMeter;
import edu.hfut.lpr.recognizer.CharacterRecognizer;
import edu.hfut.lpr.recognizer.CharacterRecognizer.RecognizedChar;
import edu.hfut.lpr.recognizer.KnnPatternClassificator;
import edu.hfut.lpr.recognizer.NeuralPatternClassificator;
import edu.hfut.lpr.run.SimpleLPR;
import edu.hfut.lpr.utils.Configurator;

/**
 * 智能信息处理类
 *
 * @author wanggang
 *
 */
public class Intelligence {

	// 上次处理的持续时间
	private static long lastProcessDuration = 0;

	// 配置文件类
	private static Configurator configurator = Configurator.getConfigurator();

	// 字符识别
	public static CharacterRecognizer chrRecog;
	// 解析器
	public static Parser parser;

	public Intelligence() throws ParserConfigurationException, SAXException, IOException {

		int classification_method = configurator.getIntProperty("intelligence_classification_method");

		if (classification_method == 0) {
			chrRecog = new KnnPatternClassificator();
		} else {
			chrRecog = new NeuralPatternClassificator();
		}

		parser = new Parser();
	}

	/**
	 * 获取上次处理的时间
	 */
	public long lastProcessDuration() {
		return lastProcessDuration;
	}

	/**
	 * 识别车牌并生成报告
	 * @param carSnapshot 车辆快照
	 */
	public String recognizeWithReport(CarSnapshot carSnapshot) throws IllegalArgumentException, IOException {
		final boolean enableReportGeneration = true;

		TimeMeter time = new TimeMeter();
		int syntaxAnalysisMode = configurator.getIntProperty("intelligence_syntaxanalysis");
		int skewDetectionMode = configurator.getIntProperty("intelligence_skewdetection");

		if (enableReportGeneration) {
			SimpleLPR.rg.insertText("<h1>自动车牌识别报告</h1>");
			SimpleLPR.rg.insertText("<span>图像宽度: " + carSnapshot.getWidth() + " px</span>");
			SimpleLPR.rg.insertText("<span>图像长度: " + carSnapshot.getHeight() + " px</span>");

			SimpleLPR.rg.insertText("<h2>车牌的垂直和水平方向投影</h2>");

			SimpleLPR.rg.insertImage(carSnapshot.renderGraph(), "快照分析图", 0, 0);
			SimpleLPR.rg.insertImage(carSnapshot.getBiWithAxes(), "快照图", 0, 0);
		}

		// 循环每个带状图
		for (Band b : carSnapshot.getBands()) {

			if (enableReportGeneration) {
				SimpleLPR.rg.insertText("<div class='bandtxt'><h4>带状图<br></h4>");
				SimpleLPR.rg.insertImage(b.getBi(), "带状图片", 250, 30);
				SimpleLPR.rg.insertText("<span>带状图宽度: " + b.getWidth() + " px</span>");
				SimpleLPR.rg.insertText("<span>带状图长度: " + b.getHeight() + " px</span>");
				SimpleLPR.rg.insertText("</div>");
			}

			for (Plate plate : b.getPlates()) {

				if (enableReportGeneration) {
					SimpleLPR.rg.insertText("<div class='platetxt'><h4>车牌<br></h4>");
					SimpleLPR.rg.insertImage(plate.getBi(), "车牌图片", 120, 30);
					SimpleLPR.rg.insertText("<span>车牌宽度: " + plate.getWidth() + " px</span>");
					SimpleLPR.rg.insertText("<span>车牌长度: " + plate.getHeight() + " px</span>");
					SimpleLPR.rg.insertText("</div>");
				}

				Plate notNormalizedCopy = null;
				BufferedImage renderedHoughTransform;
				HoughTransformation hough = null;

				if (enableReportGeneration) {
					try {
						notNormalizedCopy = plate.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
					hough = notNormalizedCopy.getHoughTransformation();
					renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
				}
				if (skewDetectionMode != 0) {
					AffineTransform shearTransform = AffineTransform.getShearInstance(0, -(double) hough.dy / hough.dx);
					BufferedImage core = Photo.createBlankBi(plate.getBi());
					core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
					plate = new Plate(core);
				}

				plate.normalize();

				float plateWHratio = (float) plate.getWidth() / (float) plate.getHeight();
				if ((plateWHratio < configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio"))
						|| (plateWHratio > configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
					continue;
				}

				Vector<Char> chars = plate.getChars();

				//				 Recognizer.configurator.getIntProperty("intelligence_minimumChars")
				if ((chars.size() < configurator.getIntProperty("intelligence_minimumChars"))
						|| (chars.size() > configurator.getIntProperty("intelligence_maximumChars"))) {
					continue;
				}

				if (plate.getCharsWidthDispersion(chars) > configurator
						.getDoubleProperty("intelligence_maxCharWidthDispersion")) {
					continue;
				}

				// 带状图检测
				if (enableReportGeneration) {
					SimpleLPR.rg.insertText("<h2>需要检测的带状图</h2>");
					SimpleLPR.rg.insertImage(b.getBiWithAxes(), "带状图片", 0, 0);
					SimpleLPR.rg.insertImage(b.renderGraph(), "带状图分析图", 0, 0);
					SimpleLPR.rg.insertText("<h2>检测的车牌</h2>");
					Plate plateCopy = null;
					try {
						plateCopy = plate.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					plateCopy.linearResize(450, 90);
					SimpleLPR.rg.insertImage(plateCopy.getBiWithAxes(), "车牌图片", 0, 0);
					SimpleLPR.rg.insertImage(plateCopy.renderGraph(), "车牌图片分析图", 0, 0);
				}

				// 倾斜矫正
				if (enableReportGeneration) {
					SimpleLPR.rg.insertText("<h2>倾斜脚检测</h2>");
					// Main.rg.insertImage(notNormalizedCopy.getBi());
					SimpleLPR.rg.insertImage(notNormalizedCopy.getBi(), "倾斜图片", 0, 0);
					SimpleLPR.rg.insertImage(renderedHoughTransform, "倾斜矫正变换", 0, 0);
					SimpleLPR.rg.insertText("倾斜角度为: <b>" + hough.angle + "</b>");
				}

				RecognizedPlate recognizedPlate = new RecognizedPlate();

				// 字符识别
				if (enableReportGeneration) {
					SimpleLPR.rg.insertText("<h2>字符分割</h2>");
					SimpleLPR.rg.insertText("<div class='charsegment'>");
					for (Char chr : chars) {
						SimpleLPR.rg.insertImage(Photo.linearResizeBi(chr.getBi(), 70, 100), "", 0, 0);
					}
					SimpleLPR.rg.insertText("</div>");
				}

				for (Char chr : chars) {
					chr.normalize();
				}

				float averageHeight = plate.getAveragePieceHeight(chars);
				float averageContrast = plate.getAveragePieceContrast(chars);
				float averageBrightness = plate.getAveragePieceBrightness(chars);
				float averageHue = plate.getAveragePieceHue(chars);
				float averageSaturation = plate.getAveragePieceSaturation(chars);

				// 循环每个字符进行识别
				for (Char chr : chars) {
					boolean ok = true;
					String errorFlags = "";

					float widthHeightRatio = (chr.pieceWidth);
					widthHeightRatio /= (chr.pieceHeight);

					if ((widthHeightRatio < configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio"))
							|| (widthHeightRatio > configurator
									.getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
						errorFlags += "WHR ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}

					if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate.getWidth() - 1)))
							&& (widthHeightRatio < 0.12)) {
						errorFlags += "POS ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}

					//					float similarityCost = rc.getSimilarityCost();

					float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
					float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
					float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
					float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
					float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;

					if (brightnessCost > configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
						errorFlags += "BRI ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}
					if (contrastCost > configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
						errorFlags += "CON ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}
					if (hueCost > configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
						errorFlags += "HUE ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}
					if (saturationCost > configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
						errorFlags += "SAT ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}
					if (heightCost < -configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
						errorFlags += "HEI ";
						ok = false;
						if (!enableReportGeneration) {
							continue;
						}
					}

					float similarityCost = 0;
					RecognizedChar rc = null;
					if (ok) {
						rc = chrRecog.recognize(chr);
						similarityCost = rc.getPatterns().elementAt(0).getCost();

						if (similarityCost > configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
							errorFlags += "NEU ";
							ok = false;
							if (!enableReportGeneration) {
								continue;
							}
						}

					}

					if (ok) {
						recognizedPlate.addChar(rc);
					} else {
						//
					}

					if (enableReportGeneration) {
						SimpleLPR.rg.insertText("<div class='heuristictable'>");
						SimpleLPR.rg.insertImage(
								Photo.linearResizeBi(chr.getBi(), chr.getWidth() * 2, chr.getHeight() * 2), "skeleton",
								0, 0);
						SimpleLPR.rg.insertText("<span class='name'>WHR</span><span class='value'>" + widthHeightRatio
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>HEI</span><span class='value'>" + heightCost
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>NEU</span><span class='value'>" + similarityCost
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>CON</span><span class='value'>" + contrastCost
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>BRI</span><span class='value'>" + brightnessCost
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>HUE</span><span class='value'>" + hueCost
								+ "</span>");
						SimpleLPR.rg.insertText("<span class='name'>SAT</span><span class='value'>" + saturationCost
								+ "</span>");
						SimpleLPR.rg.insertText("</table>");
						if (errorFlags.length() != 0) {
							SimpleLPR.rg.insertText("<span class='errflags'>" + errorFlags + "</span>");
						}
						SimpleLPR.rg.insertText("</div>");
					}
				}

				if (recognizedPlate.chars.size() < configurator.getIntProperty("intelligence_minimumChars")) {
					continue;
				}

				lastProcessDuration = time.getTime();
				String parsedOutput = Intelligence.parser.parse(recognizedPlate, syntaxAnalysisMode);

				if (enableReportGeneration) {
					SimpleLPR.rg.insertText("<span class='recognized'>");
					SimpleLPR.rg.insertText("车牌识别结果 : " + parsedOutput);
					SimpleLPR.rg.insertText("</span>");
				}

				return parsedOutput;

			}

		}

		lastProcessDuration = time.getTime();
		//		return new String("not available yet ;-)");

		return null;
	}

	/**
	 * 车牌识别
	 */
	@SuppressWarnings("unused")
	public String recognize(CarSnapshot carSnapshot) {

		TimeMeter time = new TimeMeter();
		int syntaxAnalysisMode = configurator.getIntProperty("intelligence_syntaxanalysis");
		int skewDetectionMode = configurator.getIntProperty("intelligence_skewdetection");

		// 循环每个带状图，这一步基本上没问题，都可以找到车牌所在的带状图
		int bandCount = 0;
		for (Band b : carSnapshot.getBands()) {

			// 将带状图保存下来，用于校验
			try {
				b.saveImage("tmp/car/bands/band_" + bandCount++ + ".jpg");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// 循环每个候选车牌，有部分车牌字符被分割掉了(test_004.jpg)
			int plateCount = 0;
			for (Plate plate : b.getPlates()) {

				Plate notNormalizedCopy = null;

				BufferedImage renderedHoughTransform = null;
				HoughTransformation hough = null;
				if (skewDetectionMode != 0) {
					try {
						notNormalizedCopy = plate.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
					hough = notNormalizedCopy.getHoughTransformation();
					renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
				}
				if (skewDetectionMode != 0) {
					AffineTransform shearTransform = AffineTransform.getShearInstance(0, -(double) hough.dy / hough.dx);
					BufferedImage core = Photo.createBlankBi(plate.getBi());
					core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
					plate = new Plate(core);
				}

				plate.normalize();

				// 通过车牌的宽长比例来过滤候选车牌
				float plateWHratio = (float) plate.getWidth() / (float) plate.getHeight();
				if ((plateWHratio < configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio"))
						|| (plateWHratio > configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
					continue;
				}

				Vector<Char> chars = plate.getChars("china", (bandCount - 1) + "_" + plateCount++ + ".jpg");

				// 将候选车牌图保存下来，用于校验
				try {
					plate.saveImage("tmp/car/bands/plates/plate_" + (bandCount - 1) + "_" + plateCount + ".jpg");
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				// 将字符图保存下来，用于校验
				//				int charCount = 0;
				//				for (Char chr : chars) {
				//					try {
				//						chr.saveImage("tmp/car/bands/plates/chars/char_" + (bandCount - 1) + "_" + (plateCount - 1)
				//								+ "_" + charCount++ + ".jpg");
				//					} catch (IOException e3) {
				//						e3.printStackTrace();
				//					}
				//				}

				// 根据字符数来过滤候选车牌
				if ((chars.size() < configurator.getIntProperty("intelligence_minimumChars"))
						|| (chars.size() > configurator.getIntProperty("intelligence_maximumChars"))) {
					continue;
				}

				if (plate.getCharsWidthDispersion(chars) > configurator
						.getDoubleProperty("intelligence_maxCharWidthDispersion")) {
					continue;
				}

				RecognizedPlate recognizedPlate = new RecognizedPlate();

				for (Char chr : chars) {
					chr.normalize();
				}

				float averageHeight = plate.getAveragePieceHeight(chars);
				float averageContrast = plate.getAveragePieceContrast(chars);
				float averageBrightness = plate.getAveragePieceBrightness(chars);
				float averageHue = plate.getAveragePieceHue(chars);
				float averageSaturation = plate.getAveragePieceSaturation(chars);

				// 循环候选车牌中的每个字符
				for (Char chr : chars) {

					boolean ok = true;

					String errorFlags = "";

					float widthHeightRatio = (chr.pieceWidth);
					widthHeightRatio /= (chr.pieceHeight);

					if ((widthHeightRatio < configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio"))
							|| (widthHeightRatio > configurator
									.getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
						errorFlags += "WHR ";
						ok = false;
						continue;
					}

					if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate.getWidth() - 1)))
							&& (widthHeightRatio < 0.12)) {
						errorFlags += "POS ";
						ok = false;
						continue;
					}

					// float similarityCost = rc.getSimilarityCost();

					float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
					float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
					float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
					float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
					float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;

					if (brightnessCost > configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
						errorFlags += "BRI ";
						ok = false;
						continue;
					}
					if (contrastCost > configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
						errorFlags += "CON ";
						ok = false;
						continue;
					}
					if (hueCost > configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
						errorFlags += "HUE ";
						ok = false;
						continue;
					}
					if (saturationCost > configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
						errorFlags += "SAT ";
						ok = false;
						continue;
					}
					if (heightCost < -configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
						errorFlags += "HEI ";
						ok = false;
						continue;
					}

					float similarityCost;
					RecognizedChar rc = null;
					if (ok) {
						rc = chrRecog.recognize(chr);
						similarityCost = rc.getPatterns().elementAt(0).getCost();

						if (similarityCost > configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
							errorFlags += "NEU ";
							ok = false;
							continue;
						}

					}

					if (ok) {
						recognizedPlate.addChar(rc);
					}
				}

				if (recognizedPlate.chars.size() < configurator.getIntProperty("intelligence_minimumChars")) {
					continue;
				}

				lastProcessDuration = time.getTime();
				return Intelligence.parser.parse(recognizedPlate, syntaxAnalysisMode);

			}

		}

		lastProcessDuration = time.getTime();
		//		return new String("not available yet ;-)");

		return null;
	}
}