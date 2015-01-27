package edu.hfut.lpr.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * 统计图
 *
 * @author wanggang
 *
 */
public class Graph {

	/**
	 * 投影波峰类
	 */
	public class Peak {

		public int left, center, right;

		public Peak(int left, int center, int right) {
			this.left = left;
			this.center = center;
			this.right = right;
		}

		public Peak(int left, int right) {
			this.left = left;
			this.center = (left + right) / 2;
			this.right = right;
		}

		public int getLeft() {
			return this.left;
		}

		public int getRight() {
			return this.right;
		}

		public int getCenter() {
			return this.center;
		}

		public int getDiff() {
			return this.right - this.left;
		}

		public void setLeft(int left) {
			this.left = left;
		}

		public void setCenter(int center) {
			this.center = center;
		}

		public void setRight(int right) {
			this.right = right;
		}

	}

	/**
	 * 概率分布器
	 */
	static public class ProbabilityDistributor {

		float center;
		float power;
		int leftMargin;
		int rightMargin;

		public ProbabilityDistributor(float center, float power, int leftMargin, int rightMargin) {
			this.center = center;
			this.power = power;
			this.leftMargin = Math.max(1, leftMargin);
			this.rightMargin = Math.max(1, rightMargin);
		}

		private float distributionFunction(float value, float positionPercentage) {
			return value * (1 - (this.power * Math.abs(positionPercentage - this.center)));
		}

		public Vector<Float> distribute(Vector<Float> peaks) {
			Vector<Float> distributedPeaks = new Vector<Float>();
			for (int i = 0; i < peaks.size(); i++) {
				if ((i < this.leftMargin) || (i > (peaks.size() - this.rightMargin))) {
					distributedPeaks.add(0f);
				} else {
					distributedPeaks.add(this.distributionFunction(peaks.elementAt(i), ((float) i / peaks.size())));
				}
			}

			return distributedPeaks;
		}

	}

	public Vector<Peak> peaks = null;
	public Vector<Float> yValues = new Vector<Float>();
	// 统计信息
	private boolean actualAverageValue = false;
	private boolean actualMaximumValue = false;
	private boolean actualMinimumValue = false;
	private float averageValue;
	private float maximumValue;
	private float minimumValue;

	void deActualizeFlags() {
		this.actualAverageValue = false;
		this.actualMaximumValue = false;
		this.actualMinimumValue = false;
	}

	/**
	 * 搜索图像中“带”的通用方法
	 */
	boolean allowedInterval(Vector<Peak> peaks, int xPosition) {
		for (Peak peak : peaks) {
			if ((peak.left <= xPosition) && (xPosition <= peak.right)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 添加波峰
	 */
	public void addPeak(float value) {
		this.yValues.add(value);
		this.deActualizeFlags();
	}

	/**
	 * 使用概率分布器
	 */
	public void applyProbabilityDistributor(Graph.ProbabilityDistributor probability) {
		this.yValues = probability.distribute(this.yValues);
		this.deActualizeFlags();
	}

	/**
	 * 重置或取消操作
	 */
	public void negate() {
		float max = this.getMaxValue();
		for (int i = 0; i < this.yValues.size(); i++) {
			this.yValues.setElementAt(max - this.yValues.elementAt(i), i);
		}

		this.deActualizeFlags();
	}

	// public class PeakComparer implements Comparator {
	// int sortBy;
	// Vector<Float> yValues = null;

	// public PeakComparer(Vector<Float> yValues, int sortBy) {
	// this.yValues = yValues;
	// this.sortBy = sortBy;
	// }

	// private float getPeakValue(Object peak) {
	// if (this.sortBy == 0) {
	// return ((Peak)peak).diff();
	// } else if (this.sortBy == 1) {
	// return this.yValues.elementAt( ((Peak)peak).center() );
	// } else if (this.sortBy == 2) {
	// return ((Peak)peak).center();
	// }
	// return 0;
	// }

	// public int compare(Object peak1, Object peak2) { // Peak
	// double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
	// if (comparison < 0) return -1;
	// if (comparison > 0) return 1;
	// return 0;
	// }
	// }

	// float getAverageValue() {
	// if (!this.actualAverageValue) {
	// float sum = 0.0f;
	// for (Float peak : this.yValues) sum += peak;
	// this.averageValue = sum/this.yValues.size();
	// this.actualAverageValue = true;
	// }
	// return this.averageValue;
	// }

	/**
	 * 返回平均值
	 */
	float getAverageValue() {
		if (!this.actualAverageValue) {
			this.averageValue = this.getAverageValue(0, this.yValues.size());
			this.actualAverageValue = true;
		}
		return this.averageValue;
	}

	/**
	 * 获取某一段的平均值
	 */
	float getAverageValue(int a, int b) {
		float sum = 0.0f;
		for (int i = a; i < b; i++) {
			sum += this.yValues.elementAt(i).doubleValue();
		}
		return sum / this.yValues.size();
	}

	// float getMaxValue() {
	// if (!this.actualMaximumValue) {
	// float maxValue = 0.0f;
	// for (int i=0; i<yValues.size(); i++)
	// maxValue = Math.max(maxValue, yValues.elementAt(i));
	// this.maximumValue = maxValue;
	// this.actualMaximumValue = true;
	// }
	// return this.maximumValue;
	// }

	/**
	 * 获取最大值
	 */
	float getMaxValue() {
		if (!this.actualMaximumValue) {
			this.maximumValue = this.getMaxValue(0, this.yValues.size());
			this.actualMaximumValue = true;
		}
		return this.maximumValue;
	}

	/**
	 * 获取某一段的最大值
	 */
	float getMaxValue(int a, int b) {
		float maxValue = 0.0f;
		for (int i = a; i < b; i++) {
			maxValue = Math.max(maxValue, this.yValues.elementAt(i));
		}
		return maxValue;
	}

	/**
	 * 获取某一段的最大值
	 */
	float getMaxValue(float a, float b) {
		int ia = (int) (a * this.yValues.size());
		int ib = (int) (b * this.yValues.size());
		return this.getMaxValue(ia, ib);
	}

	/**
	 * 获取某一段的最大值索引下标
	 */
	int getMaxValueIndex(int a, int b) {
		float maxValue = 0.0f;
		int maxIndex = a;
		for (int i = a; i < b; i++) {
			if (this.yValues.elementAt(i) >= maxValue) {
				maxValue = this.yValues.elementAt(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	// float getMinValue() {
	// if (!this.actualMinimumValue) {
	// float minValue = Float.POSITIVE_INFINITY;
	// for (int i=0; i<yValues.size(); i++)
	// minValue = Math.min(minValue, yValues.elementAt(i));
	//
	// this.minimumValue = minValue;
	// this.actualMinimumValue = true;
	// }
	// return this.minimumValue;
	// }

	/**
	 * 获取最小值
	 */
	float getMinValue() {
		if (!this.actualMinimumValue) {
			this.minimumValue = this.getMinValue(0, this.yValues.size());
			this.actualMinimumValue = true;
		}
		return this.minimumValue;
	}

	/**
	 * 获取某一段的最小值
	 */
	float getMinValue(int a, int b) {
		float minValue = Float.POSITIVE_INFINITY;
		for (int i = a; i < b; i++) {
			minValue = Math.min(minValue, this.yValues.elementAt(i));
		}
		return minValue;
	}

	/**
	 * 获取某一段的最小值
	 */
	float getMinValue(float a, float b) {
		int ia = (int) (a * this.yValues.size());
		int ib = (int) (b * this.yValues.size());
		return this.getMinValue(ia, ib);
	}

	/**
	 * 获取某一段的最小值索引下标
	 */
	int getMinValueIndex(int a, int b) {
		float minValue = Float.POSITIVE_INFINITY;
		int minIndex = b;
		for (int i = a; i < b; i++) {
			if (this.yValues.elementAt(i) <= minValue) {
				minValue = this.yValues.elementAt(i);
				minIndex = i;
			}
		}
		return minIndex;
	}

	/**
	 * 水平渲染
	 */
	public BufferedImage renderHorizontally(int width, int height) {

		BufferedImage content = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage axis = new BufferedImage(width + 40, height + 40, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphicContent = content.createGraphics();
		Graphics2D graphicAxis = axis.createGraphics();

		Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
		graphicAxis.setColor(Color.LIGHT_GRAY);
		graphicAxis.fill(backRect);
		graphicAxis.draw(backRect);
		backRect = new Rectangle(0, 0, width, height);
		graphicContent.setColor(Color.WHITE);
		graphicContent.fill(backRect);
		graphicContent.draw(backRect);

		int x, y, x0, y0;
		x = 0;
		y = 0;

		graphicContent.setColor(Color.GREEN);

		for (int i = 0; i < this.yValues.size(); i++) {
			x0 = x;
			y0 = y;
			x = (int) (((float) i / this.yValues.size()) * width);
			y = (int) ((1 - (this.yValues.elementAt(i) / this.getMaxValue())) * height);
			graphicContent.drawLine(x0, y0, x, y);
		}

		if (this.peaks != null) {
			graphicContent.setColor(Color.RED);
			int i = 0;
			double multConst = (double) width / this.yValues.size();
			for (Peak p : this.peaks) {
				graphicContent.drawLine((int) (p.left * multConst), 0, (int) (p.center * multConst), 30);
				graphicContent.drawLine((int) (p.center * multConst), 30, (int) (p.right * multConst), 0);
				graphicContent.drawString((i++) + ".", (int) (p.center * multConst) - 5, 42);
			}
		}

		graphicAxis.drawImage(content, 35, 5, null);

		graphicAxis.setColor(Color.BLACK);
		graphicAxis.drawRect(35, 5, content.getWidth(), content.getHeight());

		for (int ax = 0; ax < content.getWidth(); ax += 50) {
			graphicAxis.drawString(new Integer(ax).toString(), ax + 35, axis.getHeight() - 10);
			graphicAxis.drawLine(ax + 35, content.getHeight() + 5, ax + 35, content.getHeight() + 15);
		}

		for (int ay = 0; ay < content.getHeight(); ay += 20) {
			graphicAxis.drawString(
					new Integer(new Float((1 - ((float) ay / content.getHeight())) * 100).intValue()).toString() + "%",
					1, ay + 15);
			graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
		}
		graphicContent.dispose();
		graphicAxis.dispose();

		return axis;
	}

	/**
	 * 垂直渲染
	 */
	public BufferedImage renderVertically(int width, int height) {

		BufferedImage content = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage axis = new BufferedImage(width + 10, height + 40, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphicContent = content.createGraphics();
		Graphics2D graphicAxis = axis.createGraphics();

		Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
		graphicAxis.setColor(Color.LIGHT_GRAY);
		graphicAxis.fill(backRect);
		graphicAxis.draw(backRect);
		backRect = new Rectangle(0, 0, width, height);
		graphicContent.setColor(Color.WHITE);
		graphicContent.fill(backRect);
		graphicContent.draw(backRect);

		int x, y, x0, y0;
		x = width;
		y = 0;

		graphicContent.setColor(Color.GREEN);

		for (int i = 0; i < this.yValues.size(); i++) {
			x0 = x;
			y0 = y;
			y = (int) (((float) i / this.yValues.size()) * height);
			x = (int) ((this.yValues.elementAt(i) / this.getMaxValue()) * width);
			graphicContent.drawLine(x0, y0, x, y);
		}

		if (this.peaks != null) {
			graphicContent.setColor(Color.RED);
			int i = 0;
			double multConst = (double) height / this.yValues.size();
			for (Peak p : this.peaks) {
				graphicContent.drawLine(width, (int) (p.left * multConst), width - 30, (int) (p.center * multConst));
				graphicContent.drawLine(width - 30, (int) (p.center * multConst), width, (int) (p.right * multConst));
				graphicContent.drawString((i++) + ".", width - 38, (int) (p.center * multConst) + 5);
			}
		}

		graphicAxis.drawImage(content, 5, 5, null);

		graphicAxis.setColor(Color.BLACK);
		graphicAxis.drawRect(5, 5, content.getWidth(), content.getHeight());

		// for (int ax = 0; ax < content.getWidth(); ax += 50) {
		// graphicAxis.drawString(new Integer(ax).toString() , ax + 35,
		// axis.getHeight()-10);
		// graphicAxis.drawLine(ax+35, content.getHeight()+5 ,ax+35,
		// content.getHeight()+15);
		// }
		//
		// for (int ay = 0; ay < content.getHeight(); ay += 20) {
		// graphicAxis.drawString(
		// new Integer(new
		// Float((1-(float)ay/content.getHeight())*100).intValue()).toString() +
		// "%"
		// , 1 ,ay + 15);
		// graphicAxis.drawLine(25,ay+5,35,ay+5);
		// }
		graphicContent.dispose();
		graphicAxis.dispose();

		return axis;
	}

	/**
	 * 排名过滤
	 */
	public void rankFilter(int size) {
		int halfSize = size / 2;
		// Vector<Float> clone = (Vector<Float>)this.yValues.clone();
		Vector<Float> clone = new Vector<>(this.yValues);

		for (int i = halfSize; i < (this.yValues.size() - halfSize); i++) {
			float sum = 0;
			for (int ii = i - halfSize; ii < (i + halfSize); ii++) {
				sum += clone.elementAt(ii);
			}
			this.yValues.setElementAt(sum / size, i);
		}
	}

	/**
	 * 左边波峰降级索引下标
	 */
	public int indexOfLeftPeakRel(int peak, double peakFootConstantRel) {
		int index = peak;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (this.yValues.elementAt(index) < (peakFootConstantRel * this.yValues.elementAt(peak))) {
				break;
			}
		}
		return Math.max(0, index);
	}

	/**
	 * 右边波峰降级索引下标
	 */
	public int indexOfRightPeakRel(int peak, double peakFootConstantRel) {
		int index = peak;
		for (int i = peak; i < this.yValues.size(); i++) {
			index = i;
			if (this.yValues.elementAt(index) < (peakFootConstantRel * this.yValues.elementAt(peak))) {
				break;
			}
		}
		return Math.min(this.yValues.size(), index);
	}

	/**
	 * 平均波峰差值
	 */
	public float averagePeakDiff(Vector<Peak> peaks) {
		float sum = 0;
		for (Peak p : peaks) {
			sum += p.getDiff();
		}
		return sum / peaks.size();
	}

	/**
	 * 最小波峰差值
	 */
	public float maximumPeakDiff(Vector<Peak> peaks, int from, int to) {
		float max = 0;
		for (int i = from; i <= to; i++) {
			max = Math.max(max, peaks.elementAt(i).getDiff());
		}
		return max;
	}

}