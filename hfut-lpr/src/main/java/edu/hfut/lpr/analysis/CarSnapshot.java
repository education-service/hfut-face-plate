package edu.hfut.lpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车辆快照图
 *
 * @author wanggang
 *
 */
public class CarSnapshot extends Photo {

	// 车辆快照图的分布器边缘值
	private static int distributor_margins = Configurator.getConfigurator().getIntProperty(
			"carsnapshot_distributormargins");

	// private static int carsnapshot_projectionresize_x =
	// Main.configurator.getIntProperty("carsnapshot_projectionresize_x");
	// private static int carsnapshot_projectionresize_y =
	// Main.configurator.getIntProperty("carsnapshot_projectionresize_y");

	// 车辆快照图的统计图排名过滤值
	private static int carsnapshot_graphrankfilter = Configurator.getConfigurator().getIntProperty(
			"carsnapshot_graphrankfilter");

	// 候选车牌区域数
	static private int numberOfCandidates = Configurator.getConfigurator().getIntProperty("intelligence_numberOfBands");

	// 车辆快照统计图
	private CarSnapshotGraph graphHandle = null;

	// 概率分布器
	public static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0,
			CarSnapshot.distributor_margins, CarSnapshot.distributor_margins);

	public CarSnapshot(String filename) throws IOException {
		super(Configurator.getConfigurator().getResourceAsStream(filename));

	}

	public CarSnapshot(BufferedImage bi) {
		super(bi);
	}

	public CarSnapshot(InputStream is) throws IOException {
		super(is);
	}

	/**
	 * 垂直渲染统计图
	 */
	public BufferedImage renderGraph() {
		this.computeGraph();
		return this.graphHandle.renderVertically(100, this.getHeight());
	}

	/**
	 * 计算统计图波峰数
	 */
	private Vector<Graph.Peak> computeGraph() {

		if (this.graphHandle != null) {
			return this.graphHandle.peaks;
		}

		/****** 垂直边缘检测，并二值化 ******/

		// 拷贝图像
		BufferedImage imageCopy = duplicateBufferedImage(this.image, "china");
		// 垂直边缘检测和二值化
		this.verticalEdgeBi(imageCopy);
		Photo.thresholding(imageCopy);

		// 统计计算
		this.graphHandle = this.histogram(imageCopy);
		this.graphHandle.rankFilter(CarSnapshot.carsnapshot_graphrankfilter); // 对于中文车牌暂时不需要
		this.graphHandle.applyProbabilityDistributor(CarSnapshot.distributor);

		this.graphHandle.findPeaks(CarSnapshot.numberOfCandidates); // 设置为3,基本上就可以找到车牌对应的带状图，也可以适当的调大写
		return this.graphHandle.peaks;
	}

	/**
	 * 获取带状图或类车牌区域候选图
	 */
	public Vector<Band> getBands() {

		Vector<Band> out = new Vector<Band>();

		Vector<Graph.Peak> peaks = this.computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			Graph.Peak p = peaks.elementAt(i);
			out.add(new Band(this.image.getSubimage(0, (p.getLeft()), this.image.getWidth(), (p.getDiff()))));
		}

		return out;
	}

	/**
	 * 垂直边缘检测处理
	 */
	public void verticalEdgeBi(BufferedImage image) {
		BufferedImage imageCopy = Photo.duplicateBufferedImage(image);

		float data[] = { -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1 };

		new ConvolveOp(new Kernel(3, 4, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
	}

	/*public void verticalRankBi(BufferedImage image) {
		BufferedImage imageCopy = duplicateBi(image);

		float data[] = new float[9];
		for (int i = 0; i < data.length; i++)
			data[i] = 1.0f / data.length;

		new ConvolveOp(new Kernel(1, data.length, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
	}*/

	/**
	 * 车辆快照水平直方图
	 */
	public CarSnapshotGraph histogram(BufferedImage bi) {
		CarSnapshotGraph graph = new CarSnapshotGraph(this);
		for (int y = 0; y < bi.getHeight(); y++) {
			float counter = 0;
			for (int x = 0; x < bi.getWidth(); x++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

}