package edu.hfut.lpr.analysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车牌区域统计图
 *
 * @author wanggang
 *
 */
public class BandGraph extends Graph {

	// 车牌区域图
	private final Band handle;

	// 车牌区域统计图，峰脚常量 0.75
	private static double peakFootConstant = Configurator.getConfigurator().getDoubleProperty(
			"bandgraph_peakfootconstant");

	// 车牌区域统计图，峰差相乘常量值 0.2
	private static double peakDiffMultiplicationConstant = Configurator.getConfigurator().getDoubleProperty(
			"bandgraph_peakDiffMultiplicationConstant");

	public BandGraph(Band handle) {
		this.handle = handle;
	}

	/**
	 * 波峰比较器
	 */
	public class PeakComparer implements Comparator<Object> {

		Vector<Float> yValues = null;

		public PeakComparer(Vector<Float> yValues) {
			this.yValues = yValues;
		}

		/**
		 * 获取波峰中心值
		 */
		private float getPeakValue(Object peak) {
			// left > right
			// return ((Peak)peak).center();
			return this.yValues.elementAt(((Peak) peak).getCenter());
		}

		/**
		 * 根据波峰中心值比较
		 */
		@Override
		public int compare(Object peak1, Object peak2) {
			double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
			if (comparison < 0) {
				return -1;
			}
			if (comparison > 0) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * 寻找所有波峰
	 * @param count 波峰数量
	 * @return
	 */
	public Vector<Peak> findPeaks(int count) {

		Vector<Graph.Peak> outPeaks = new Vector<>();

		for (int c = 0; c < count; c++) {
			float maxValue = 0.0f;
			int maxIndex = 0;
			for (int i = 0; i < this.yValues.size(); i++) {
				if (this.allowedInterval(outPeaks, i)) {
					if (this.yValues.elementAt(i) >= maxValue) {
						maxValue = this.yValues.elementAt(i);
						maxIndex = i;
					}
				}
			}
			int leftIndex = this.indexOfLeftPeakRel(maxIndex, BandGraph.peakFootConstant);
			int rightIndex = this.indexOfRightPeakRel(maxIndex, BandGraph.peakFootConstant);
			int diff = rightIndex - leftIndex;
			leftIndex -= BandGraph.peakDiffMultiplicationConstant * diff; /* 常量 */
			rightIndex += BandGraph.peakDiffMultiplicationConstant * diff; /* 常量 */

			outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
		}

		Vector<Peak> outPeaksFiltered = new Vector<>();
		for (Peak p : outPeaks) {
			if ((p.getDiff() > (2 * this.handle.getHeight())) && (p.getDiff() < (15 * this.handle.getHeight()))) {
				outPeaksFiltered.add(p);
			}
		}

		Collections.sort(outPeaksFiltered, new PeakComparer(this.yValues));
		super.peaks = outPeaksFiltered;

		return outPeaksFiltered;
	}

	/**
	 * 波峰左边脚下标
	 * @param peak 波峰中心下标
	 * @param peakFootConstantAbs 峰脚值
	 * @return
	 */
	public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
		int index = peak;
		// int counter = 0;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (this.yValues.elementAt(index) < peakFootConstantAbs) {
				break;
			}
		}
		return Math.max(0, index);
	}

	/**
	 * 波峰左边脚下标
	 * @param peak 波峰中心下标
	 * @param peakFootConstantAbs 峰脚值
	 * @return
	 */
	public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
		int index = peak;
		// int counter = 0;
		for (int i = peak; i < this.yValues.size(); i++) {
			index = i;
			if (this.yValues.elementAt(index) < peakFootConstantAbs) {
				break;
			}
		}
		return Math.min(this.yValues.size(), index);
	}

}