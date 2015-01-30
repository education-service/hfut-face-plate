package edu.hfut.lpr.images;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车辆快照统计图
 *
 * @author wanggang
 *
 */
public class CarSnapshotGraph extends Graph {

	// 车辆快照峰脚值，方便在图像中搜索带状图
	private static double peakFootConstant = Configurator.getConfigurator().getDoubleProperty(
			"carsnapshotgraph_peakfootconstant"); // 0.55

	// 波峰差相乘常量
	private static double peakDiffMultiplicationConstant = Configurator.getConfigurator().getDoubleProperty(
			"carsnapshotgraph_peakDiffMultiplicationConstant"); // 0.1

	CarSnapshot handle;

	public CarSnapshotGraph(CarSnapshot handle) {
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
		 * 波峰中心值
		 */
		private float getPeakValue(Object peak) {
			return this.yValues.elementAt(((Peak) peak).getCenter());
			// return ((Peak)peak).getDiff();
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
	 * 寻找波峰
	 * @param count 波峰数量
	 * @return
	 */
	public Vector<Peak> findPeaks(int count) {

		Vector<Peak> outPeaks = new Vector<Peak>();

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
			int leftIndex = this.indexOfLeftPeakRel(maxIndex, CarSnapshotGraph.peakFootConstant);
			int rightIndex = this.indexOfRightPeakRel(maxIndex, CarSnapshotGraph.peakFootConstant);
			int diff = rightIndex - leftIndex;
			leftIndex -= CarSnapshotGraph.peakDiffMultiplicationConstant * diff; /* 常量 */
			rightIndex += CarSnapshotGraph.peakDiffMultiplicationConstant * diff; /* 常量 */

			outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
		}

		Collections.sort(outPeaks, new PeakComparer(this.yValues));

		super.peaks = outPeaks;

		return outPeaks;
	}

	/*public int indexOfLeftPeak(int peak, double peakFootConstant) {
		int index = peak;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (yValues.elementAt(index) < peakFootConstant * yValues.elementAt(peak))
				break;
		}
		return Math.max(0, index);
	}*/

	/*public int indexOfRightPeak(int peak, double peakFootConstant) {
		int index = peak;
		for (int i = peak; i < yValues.size(); i++) {
			index = i;
			if (yValues.elementAt(index) < peakFootConstant * yValues.elementAt(peak))
				break;
		}
		return Math.min(yValues.size(), index);
	}*/

}