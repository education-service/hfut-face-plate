package edu.hfut.lpr.images;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import edu.hfut.lpr.utils.ConfigUtil;

/**
 * 车牌图像垂直统计图
 *
 * @author wanggang
 *
 */
public class PlateVerticalGraph extends Graph {

	// 车牌区域垂直图-峰脚常量值
	private static double peakFootConstant = ConfigUtil.getConfigurator().getDoubleProperty(
			"plateverticalgraph_peakfootconstant");

	Plate handle;

	public PlateVerticalGraph(Plate handle) {
		this.handle = handle;
	}

	/**
	 * 波峰比较器
	 */
	public class PeakComparer implements Comparator<Object> {

		PlateVerticalGraph graphHandle = null;

		public PeakComparer(PlateVerticalGraph graph) {
			this.graphHandle = graph;
		}

		/**
		 * 获取波峰值，即波峰中心值
		 */
		private float getPeakValue(Object peak) {
			//			return ((Peak) peak).getDiff();
			return this.graphHandle.yValues.elementAt(((Peak) peak).getCenter());
			//			int peakCenter= (((Peak) peak).getRight() + ((Peak) peak).getLeft()) / 2;
			//			return Math.abs(pe akCenter - this.graphHandle.yValues.size() / 2);
		}

		/**
		 * 比较波峰中心值大小
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
	public Vector<Peak> findPeak(int count) {

		for (int i = 0; i < this.yValues.size(); i++) {
			this.yValues.set(i, this.yValues.elementAt(i) - this.getMinValue());
		}

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

			if (this.yValues.elementAt(maxIndex) < (0.05 * super.getMaxValue())) {
				break; // 0.4
			}

			int leftIndex = this.indexOfLeftPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);
			int rightIndex = this.indexOfRightPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);

			outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
		}

		Collections.sort(outPeaks, new PeakComparer(this));
		super.peaks = outPeaks;
		return outPeaks;
	}

}