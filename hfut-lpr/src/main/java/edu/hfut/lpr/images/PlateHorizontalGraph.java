package edu.hfut.lpr.images;

import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车牌图像水平统计图
 *
 * @author wanggang
 *
 */
public class PlateHorizontalGraph extends Graph {

	// 默认0.1
	//	private static double peakFootConstant = Intelligence.configurator
	//			.getDoubleProperty("platehorizontalgraph_peakfootconstant");

	// 水平检测类型
	private static int horizontalDetectionType = Configurator.getConfigurator().getIntProperty(
			"platehorizontalgraph_detectionType");

	Plate handle;

	public PlateHorizontalGraph(Plate handle) {
		this.handle = handle;
	}

	/**
	 * y方向差值
	 */
	public float derivation(int index1, int index2) {
		return this.yValues.elementAt(index1) - this.yValues.elementAt(index2);
	}

	/**
	 * 寻找波峰
	 * @param count 波峰数量
	 * @return
	 */
	public Vector<Peak> findPeak(int count) {
		if (PlateHorizontalGraph.horizontalDetectionType == 1) {
			return this.findPeak_edgedetection(count);
		}
		return this.findPeak_derivate(count);
	}

	/**
	 * 寻找波峰，通过差值法
	 */
	public Vector<Peak> findPeak_derivate(int count) {

		int a, b;
		float maxVal = this.getMaxValue();

		for (a = 2; (-this.derivation(a, a + 4) < (maxVal * 0.2)) && (a < (this.yValues.size() - 2 - 2 - 4)); a++) {
			//
		}
		for (b = this.yValues.size() - 1 - 2; (this.derivation(b - 4, b) < (maxVal * 0.2)) && (b > (a + 2)); b--) {
			//
		}

		Vector<Peak> outPeaks = new Vector<>();

		outPeaks.add(new Peak(a, b));
		super.peaks = outPeaks;

		return outPeaks;
	}

	/**
	 * 寻找波峰，通过边缘检测
	 * @param count 波峰数量
	 * @return
	 */
	public Vector<Peak> findPeak_edgedetection(int count) {

		float average = this.getAverageValue();
		int a, b;
		for (a = 0; this.yValues.elementAt(a) < average; a++) {

		}
		for (b = this.yValues.size() - 1; this.yValues.elementAt(b) < average; b--) {

		}

		Vector<Peak> outPeaks = new Vector<>();
		a = Math.max(a - 5, 0);
		b = Math.min(b + 5, this.yValues.size());

		outPeaks.add(new Peak(a, b));
		super.peaks = outPeaks;

		return outPeaks;
	}

}