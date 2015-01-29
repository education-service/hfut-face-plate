package edu.hfut.lpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Vector;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import edu.hfut.fr.image.processing.threshold.OtsuThreshold;
import edu.hfut.lpr.utils.Configurator;

/**
 * 完整车牌图像对象
 *
 * @author wanggang
 *
 */
public class Plate extends Photo {

	// 概率分布计算器
	static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0, 0, 0);

	// 候选车牌数量
	static private int numberOfCandidates = Configurator.getConfigurator().getIntProperty("intelligence_numberOfChars");

	// 水平检测类型
	private static int horizontalDetectionType = Configurator.getConfigurator().getIntProperty(
			"platehorizontalgraph_detectionType");

	// 车牌统计图
	private PlateGraph graphHandle = null;

	// 车牌图像复制图，二值图
	public Plate plateCopy;

	public Plate(BufferedImage bi) {
		super(bi);
		this.plateCopy = new Plate(Photo.duplicateBufferedImage(this.image), true);
		this.plateCopy.adaptiveThresholding();
	}

	public Plate(BufferedImage bi, boolean isCopy) {
		super(bi);
	}

	/**
	 * 渲染统计图
	 */
	public BufferedImage renderGraph() {
		// 计算波峰
		this.computeGraph();
		// 返回水平渲染图
		return this.graphHandle.renderHorizontally(this.getWidth(), 100);
	}

	/**
	 * 计算波峰
	 */
	private Vector<Graph.Peak> computeGraph() {
		if (this.graphHandle != null) {
			return this.graphHandle.peaks;
		}

		this.graphHandle = this.histogram(this.plateCopy.getBi());
		//		PlateGraph graph = histogram(this.plateCopy.getBi());
		this.graphHandle.applyProbabilityDistributor(Plate.distributor);
		this.graphHandle.findPeaks(Plate.numberOfCandidates);

		return this.graphHandle.peaks;
	}

	/**
	 * 获取所有字符图像
	 */
	public Vector<Char> getChars() {

		Vector<Char> out = new Vector<>();

		Vector<Graph.Peak> peaks = this.computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			Graph.Peak p = peaks.elementAt(i);
			if (p.getDiff() <= 0) {
				continue;
			}
			out.add(new Char(this.image.getSubimage(p.getLeft(), 0, p.getDiff(), this.image.getHeight()),
					this.plateCopy.image.getSubimage(p.getLeft(), 0, p.getDiff(), this.image.getHeight()),
					new PositionInPlate(p.getLeft(), p.getRight())));
		}

		return out;
	}

	/**
	 * 克隆图像
	 */
	@Override
	public Plate clone() throws CloneNotSupportedException {
		super.clone();
		return new Plate(duplicateBufferedImage(this.image));
	}

	/**
	 * 缓冲图水平边缘
	 */
	public void horizontalEdgeBi(BufferedImage image) {
		BufferedImage imageCopy = Photo.duplicateBufferedImage(image);
		float data[] = { -1, 0, 1 };
		new ConvolveOp(new Kernel(1, 3, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
	}

	/**
	 * 标准化
	 */
	public void normalize() {
		// 垂直统计
		Plate clone1 = null;
		try {
			clone1 = this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		clone1.verticalEdgeDetector(clone1.getBi());
		PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getBi());
		this.image = this.cutTopBottom(this.image, vertical);
		this.plateCopy.image = this.cutTopBottom(this.plateCopy.image, vertical);
		// 水平统计
		Plate clone2 = null;
		try {
			clone2 = this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		if (Plate.horizontalDetectionType == 1) {
			clone2.horizontalEdgeDetector(clone2.getBi());
		}
		PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getBi());
		this.image = this.cutLeftRight(this.image, horizontal);
		this.plateCopy.image = this.cutLeftRight(this.plateCopy.image, horizontal);
	}

	// 剪切上下部分
	private BufferedImage cutTopBottom(BufferedImage origin, PlateVerticalGraph graph) {
		graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
		Graph.Peak p = graph.findPeak(3).elementAt(0);
		return origin.getSubimage(0, p.getLeft(), this.image.getWidth(), p.getDiff());
	}

	// 剪切左右部分
	private BufferedImage cutLeftRight(BufferedImage origin, PlateHorizontalGraph graph) {
		graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
		Vector<Graph.Peak> peaks = graph.findPeak(3);
		if (peaks.size() != 0) {
			Graph.Peak p = peaks.elementAt(0);
			return origin.getSubimage(p.getLeft(), 0, p.getDiff(), this.image.getHeight());
		}
		return origin;
	}

	// 直方图
	public PlateGraph histogram(BufferedImage bi) {

		// 针对蓝底车牌
		FImage grayImage = ImageUtilities.createFImage(this.getBi());
		OtsuThreshold threshold = new OtsuThreshold();
		threshold.processImage(grayImage);
		// 默认是针对白底车牌
		PlateGraph graph = new PlateGraph(this);
		for (int x = 0; x < bi.getWidth(); x++) {
			float counter = 0;
			for (int y = 0; y < bi.getHeight(); y++) {
				//				counter += Photo.getBrightness(bi, x, y);
				// 蓝底
				counter += grayImage.getPixelNative(x, y);
			}
			counter = bi.getHeight() - counter;
			graph.addPeak(counter);
		}
		return graph;
	}

	// 水平直方图
	private PlateVerticalGraph histogramYaxis(BufferedImage bi) {
		PlateVerticalGraph graph = new PlateVerticalGraph(this);
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int y = 0; y < h; y++) {
			float counter = 0;
			for (int x = 0; x < w; x++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	// 垂直直方图
	private PlateHorizontalGraph histogramXaxis(BufferedImage bi) {
		PlateHorizontalGraph graph = new PlateHorizontalGraph(this);
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			float counter = 0;
			for (int y = 0; y < h; y++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	// 垂直边缘检测
	@Override
	public void verticalEdgeDetector(BufferedImage source) {
		float matrix[] = { -1, 0, 1 };
		BufferedImage destination = Photo.duplicateBufferedImage(source);
		new ConvolveOp(new Kernel(3, 1, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
	}

	// 水平边缘检测
	public void horizontalEdgeDetector(BufferedImage source) {
		BufferedImage destination = Photo.duplicateBufferedImage(source);

		float matrix[] = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };

		new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
	}

	// 获取所有字符宽度的分散系或离差
	public float getCharsWidthDispersion(Vector<Char> chars) {
		float averageDispersion = 0;
		float averageWidth = this.getAverageCharWidth(chars);

		for (Char chr : chars) {
			averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
		}
		averageDispersion /= chars.size();

		return averageDispersion / averageWidth;
	}

	// 获取所有字符中像素块的分散系或离差
	public float getPiecesWidthDispersion(Vector<Char> chars) {
		float averageDispersion = 0;
		float averageWidth = this.getAveragePieceWidth(chars);

		for (Char chr : chars) {
			averageDispersion += (Math.abs(averageWidth - chr.pieceWidth));
		}
		averageDispersion /= chars.size();

		return averageDispersion / averageWidth;
	}

	// 获取所有字符平均宽度
	public float getAverageCharWidth(Vector<Char> chars) {
		float averageWidth = 0;
		for (Char chr : chars) {
			averageWidth += chr.fullWidth;
		}
		averageWidth /= chars.size();
		return averageWidth;
	}

	// 获取所有字符中像素块的平均宽度
	public float getAveragePieceWidth(Vector<Char> chars) {
		float averageWidth = 0;
		for (Char chr : chars) {
			averageWidth += chr.pieceWidth;
		}
		averageWidth /= chars.size();
		return averageWidth;
	}

	// 获取所有字符中像素块的平均色度
	public float getAveragePieceHue(Vector<Char> chars) {
		float averageHue = 0;
		for (Char chr : chars) {
			averageHue += chr.statisticAverageHue;
		}
		averageHue /= chars.size();
		return averageHue;
	}

	// 获取所有字符中像素块的平均对比度
	public float getAveragePieceContrast(Vector<Char> chars) {
		float averageContrast = 0;
		for (Char chr : chars) {
			averageContrast += chr.statisticContrast;
		}
		averageContrast /= chars.size();
		return averageContrast;
	}

	// 获取所有字符中像素块的平均亮度
	public float getAveragePieceBrightness(Vector<Char> chars) {
		float averageBrightness = 0;
		for (Char chr : chars) {
			averageBrightness += chr.statisticAverageBrightness;
		}
		averageBrightness /= chars.size();
		return averageBrightness;
	}

	// 获取所有字符中像素块的平均最小亮度
	public float getAveragePieceMinBrightness(Vector<Char> chars) {
		float averageMinBrightness = 0;
		for (Char chr : chars) {
			averageMinBrightness += chr.statisticMinimumBrightness;
		}
		averageMinBrightness /= chars.size();
		return averageMinBrightness;
	}

	// 获取所有字符中像素块的平均最大亮度
	public float getAveragePieceMaxBrightness(Vector<Char> chars) {
		float averageMaxBrightness = 0;
		for (Char chr : chars) {
			averageMaxBrightness += chr.statisticMaximumBrightness;
		}
		averageMaxBrightness /= chars.size();
		return averageMaxBrightness;
	}

	// 获取所有字符中像素块的平均饱和度
	public float getAveragePieceSaturation(Vector<Char> chars) {
		float averageSaturation = 0;
		for (Char chr : chars) {
			averageSaturation += chr.statisticAverageSaturation;
		}
		averageSaturation /= chars.size();
		return averageSaturation;
	}

	// 获取所有字符的平均长度
	public float getAverageCharHeight(Vector<Char> chars) {
		float averageHeight = 0;
		for (Char chr : chars) {
			averageHeight += chr.fullHeight;
		}
		averageHeight /= chars.size();
		return averageHeight;
	}

	// 获取所有字符中像素块的平均长度
	public float getAveragePieceHeight(Vector<Char> chars) {
		float averageHeight = 0;
		for (Char chr : chars) {
			averageHeight += chr.pieceHeight;
		}
		averageHeight /= chars.size();
		return averageHeight;
	}

	/*public float getAverageCharSquare(Vector<Char> chars) {
		float average = 0;
		for (Char chr : chars)
			average += chr.getWidth() * chr.getHeight();
		average /= chars.size();
		return average;
	}*/

}