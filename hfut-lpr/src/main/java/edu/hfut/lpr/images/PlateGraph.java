package edu.hfut.lpr.images;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import edu.hfut.lpr.utils.Configurator;

/**
 * 车牌统计图
 *
 * @author wanggang
 *
 */
public class PlateGraph extends Graph {

	Plate handle;

	// 车牌统计图中最小波峰大小
	private static double plategraph_rel_minpeaksize = Configurator.getConfigurator().getDoubleProperty(
			"plategraph_rel_minpeaksize");

	// 峰脚常量值
	private static double peakFootConstant = Configurator.getConfigurator().getDoubleProperty(
			"plategraph_peakfootconstant");

	public PlateGraph(Plate handle) {
		this.handle = handle;
	}

	/**
	 * 空间比较器
	 */
	public class SpaceComparer implements Comparator<Object> {

		Vector<Float> yValues = null;

		public SpaceComparer(Vector<Float> yValues) {
			this.yValues = yValues;
		}

		private float getPeakValue(Object peak) {
			// left > right
			return ((Peak) peak).getCenter();
			// return this.yValues.elementAt( ((Peak)peak).center() );
		}

		/**
		 * 根据峰中心值比较
		 */
		@Override
		public int compare(Object peak1, Object peak2) {
			double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
			if (comparison < 0) {
				return 1;
			}
			if (comparison > 0) {
				return -1;
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

		Vector<Peak> spacesTemp = new Vector<Peak>();

		float diffGVal = (2 * this.getAverageValue()) - this.getMaxValue();

		Vector<Float> yValuesNew = new Vector<Float>();
		for (Float f : this.yValues) {
			yValuesNew.add(f.floatValue() - diffGVal);
		}
		this.yValues = yValuesNew;

		this.deActualizeFlags();

		for (int c = 0; c < count; c++) {
			float maxValue = 0.0f;
			int maxIndex = 0;
			for (int i = 0; i < this.yValues.size(); i++) {
				if (this.allowedInterval(spacesTemp, i)) {
					if (this.yValues.elementAt(i) >= maxValue) {
						maxValue = this.yValues.elementAt(i);
						maxIndex = i;
					}
				}
			}
			if (this.yValues.elementAt(maxIndex) < (PlateGraph.plategraph_rel_minpeaksize * this.getMaxValue())) {
				break;
			}

			int leftIndex = this.indexOfLeftPeakRel(maxIndex, PlateGraph.peakFootConstant);
			int rightIndex = this.indexOfRightPeakRel(maxIndex, PlateGraph.peakFootConstant);

			spacesTemp.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
		}

		Vector<Peak> spaces = new Vector<Peak>();
		for (Peak p : spacesTemp) {
			if (p.getDiff() < (1 * this.handle.getHeight())) {
				spaces.add(p);
			}
		}

		Collections.sort(spaces, new SpaceComparer(this.yValues));

		Vector<Peak> chars = new Vector<Peak>();

		if (spaces.size() != 0) {
			//			int minIndex = getMinValueIndex(0, spaces.elementAt(0).getCenter());
			//			System.out.println("minindex found at " + minIndex + " in interval 0 - "
			//					+ outPeaksFiltered.elementAt(0).getCenter());
			int leftIndex = 0;
			// for (int i=minIndex; i>=0; i--) {
			// leftIndex = i;
			// if (this.yValues.elementAt(i) >
			// 0.9 * this.yValues.elementAt(
			// outPeaksFiltered.elementAt(0).getCenter()
			// )
			// ) break;
			// }

			Peak first = new Peak(leftIndex/* 0 */, spaces.elementAt(0).getCenter());
			if (first.getDiff() > 0) {
				chars.add(first);
			}
		}

		for (int i = 0; i < (spaces.size() - 1); i++) {
			int left = spaces.elementAt(i).getCenter();
			int right = spaces.elementAt(i + 1).getCenter();
			chars.add(new Peak(left, right));
		}

		if (spaces.size() != 0) {
			Peak last = new Peak(spaces.elementAt(spaces.size() - 1).getCenter(), this.yValues.size() - 1);
			if (last.getDiff() > 0) {
				chars.add(last);
			}
		}

		super.peaks = chars;

		return chars;
	}

	/*public int indexOfLeftPeak(int peak) {
		int index = peak;
		int counter = 0;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (yValues.elementAt(index) < 0.7 * yValues.elementAt(peak))
				break;
		}
		return Math.max(0, index);
	}*/

	/*public int indexOfRightPeak(int peak) {
		int index = peak;
		int counter = 0;
		for (int i = peak; i < yValues.size(); i++) {
			index = i;
			if (yValues.elementAt(index) < 0.7 * yValues.elementAt(peak))
				break;
		}
		return Math.min(yValues.size(), index);
	}*/

	/*public float minValInInterval(float a, float b) {
		int ia = (int) (a * yValues.size());
		int ib = (int) (b * yValues.size());
		float min = Float.POSITIVE_INFINITY;
		for (int i = ia; i < ib; i++) {
			min = Math.min(min, yValues.elementAt(i));
		}
		return min;
	}*/

	/*public float maxValInInterval(float a, float b) {
		int ia = (int) (a * yValues.size());
		int ib = (int) (b * yValues.size());
		float max = 0;
		for (int i = ia; i < ib; i++) {
			max = Math.max(max, yValues.elementAt(i));
		}
		return max;
	}*/

}