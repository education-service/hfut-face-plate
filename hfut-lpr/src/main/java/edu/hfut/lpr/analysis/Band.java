package edu.hfut.lpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
//import java.io.IOException;
import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车牌区域图
 *
 * @author wanggang
 *
 */
public class Band extends Photo {

	// 概率统计分布器
	static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0, 25, 25);

	// 候选车牌数量
	static private int numberOfCandidates = Configurator.getConfigurator()
			.getIntProperty("intelligence_numberOfPlates");

	// 车牌区域统计图
	private BandGraph graphHandle = null;

	public Band(BufferedImage bi) {
		super(bi);
	}

	/**
	 * 水平渲染统计图
	 */
	public BufferedImage renderGraph() {
		this.computeGraph();
		return this.graphHandle.renderHorizontally(this.getWidth(), 100);
	}

	/**
	 * 计算统计图波峰
	 */
	private Vector<Graph.Peak> computeGraph() {
		if (this.graphHandle != null) {
			return this.graphHandle.peaks;
		}
		// 复制图像
		BufferedImage imageCopy = Photo.duplicateBufferedImage(this.image);
		// 全部边缘检测
		this.fullEdgeDetector(imageCopy);
		// 统计直方图
		this.graphHandle = this.histogram(imageCopy);
		// 根据高度排名过滤
		this.graphHandle.rankFilter(this.image.getHeight());
		// 使用概率分布器
		this.graphHandle.applyProbabilityDistributor(Band.distributor);
		// 找出候选车牌波峰
		this.graphHandle.findPeaks(Band.numberOfCandidates);
		//		DisplayUtilities.display(imageCopy);
		return this.graphHandle.peaks;
	}

	/**
	 * 获取全部车牌区域
	 */
	public Vector<Plate> getPlates() {

		Vector<Plate> out = new Vector<Plate>();

		Vector<Graph.Peak> peaks = this.computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			Graph.Peak p = peaks.elementAt(i);
			out.add(new Plate(this.image.getSubimage(p.getLeft(), 0, p.getDiff(), this.image.getHeight())));
		}

		return out;
	}

	/*public void horizontalRankBi(BufferedImage image) {
		BufferedImage imageCopy = duplicateBi(image);

		float data[] = new float[image.getHeight()];
		for (int i = 0; i < data.length; i++)
			data[i] = 1.0f / data.length;

		new ConvolveOp(new Kernel(data.length, 1, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
	}*/

	/**
	 * 缓冲图的垂直统计直方图
	 */
	public BandGraph histogram(BufferedImage bi) {
		BandGraph graph = new BandGraph(this);
		for (int x = 0; x < bi.getWidth(); x++) {
			float counter = 0;
			for (int y = 0; y < bi.getHeight(); y++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	/**
	 * 缓冲图的完全边缘检测
	 */
	public void fullEdgeDetector(BufferedImage source) {

		float verticalMatrix[] = { -1, 0, 1, -2, 0, 2, -1, 0, 1, };
		float horizontalMatrix[] = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };

		BufferedImage i1 = Photo.createBlankBi(source);
		BufferedImage i2 = Photo.createBlankBi(source);

		new ConvolveOp(new Kernel(3, 3, verticalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i1);
		new ConvolveOp(new Kernel(3, 3, horizontalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i2);

		int w = source.getWidth();
		int h = source.getHeight();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float sum = 0.0f;
				sum += Photo.getBrightness(i1, x, y);
				sum += Photo.getBrightness(i2, x, y);
				Photo.setBrightness(source, x, y, Math.min(1.0f, sum));
			}
		}

	}

}