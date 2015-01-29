package edu.hfut.fr.image.analysis.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * 实现直线的霍夫变换
 *
 * @author wanggang
 *
 */
public class HoughLines implements ImageAnalyser<FImage>, Iterable<Line2d>, Iterator<Line2d> {

	/** 计算图像 */
	private FImage accum = null;

	private int numberOfSegments = 360;

	/**
	 * 图片迭代器
	 */
	private FImage iteratorAccum = null;

	/** 当前图片迭代器所在的位置 */
	private FValuePixel iteratorCurrentPix = null;

	private float onValue;

	/**
	  默认构造
	 */
	public HoughLines() {
		this(360, 0f);
	}

	public HoughLines(float onValue) {
		this(360, onValue);
	}

	public HoughLines(int nSegments, float onValue) {
		this.setNumberOfSegments(nSegments);
		this.onValue = onValue;
	}

	@Override
	/**
	 * 分析图片
	 */
	public void analyseImage(FImage image) {
		int amax = (int) round(sqrt((image.getHeight() * image.getHeight()) + (image.getWidth() * image.getWidth())));

		if (accum == null || accum.height != amax || accum.width != getNumberOfSegments())
			accum = new FImage(getNumberOfSegments(), amax);
		else
			accum.zero();

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (image.getPixel(x, y) == onValue) {
					for (int m = 0; m < getNumberOfSegments(); m++) {
						//						double mm = PI*m*360d/getNumberOfSegments()/180d;
						double mm = ((double) m / (double) getNumberOfSegments()) * (2 * PI);
						int a = (int) round(x * cos(mm) + y * sin(mm));
						if (a < amax && a >= 0)
							accum.pixels[a][m]++;
					}
				}
			}
		}
	}

	public FImage getAccumulator() {
		return accum;
	}

	public FImage calculateHorizontalProjection() {
		return calculateHorizontalProjection(accum);
	}

	/**
	 * 	通过给定的计算空间来计算水平图像
	 */
	public FImage calculateHorizontalProjection(FImage accum) {
		FImage proj = new FImage(accum.getWidth(), 1);

		for (int x = 0; x < accum.getWidth(); x++) {
			float acc = 0;
			for (int y = 0; y < accum.getHeight(); y++)
				acc += accum.getPixel(x, y) * accum.getPixel(x, y);
			proj.setPixel(x, 0, (float) Math.sqrt(acc));
		}

		return proj;
	}

	/**
	 * 返回角度
	 */
	public double calculatePrevailingAngle() {
		return calculatePrevailingAngle(accum, 0, 360);
	}

	public double calculatePrevailingAngle(FImage accum, int offset, double nDegrees) {
		FValuePixel maxpix = calculateHorizontalProjection(accum).maxPixel();
		if (maxpix.x == -1 && maxpix.y == -1)
			return Double.MIN_VALUE;
		return (maxpix.x + offset) * (nDegrees / accum.getWidth());
	}

	public double calculatePrevailingAngle(float minTheta, float maxTheta) {
		if (minTheta > maxTheta) {
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}

		if (minTheta >= 0) {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage f = accum.extractROI(mt, 0, xt - mt, accum.getHeight());
			return calculatePrevailingAngle(f, mt, (xt - mt) * (360d / getNumberOfSegments()));
		} else {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage a = accum.shiftRight(-mt).extractROI(0, 0, (xt - mt), accum.getHeight());
			return calculatePrevailingAngle(a, mt, (xt - mt) * (360d / getNumberOfSegments()));
		}
	}

	/**
	 * 	返回图像中最好的直线
	 *
	 */
	public Line2d getBestLine() {
		return getBestLine(accum, 0);
	}

	public Line2d getBestLine(FImage accumulatorSpace, int offset) {
		FValuePixel p = accumulatorSpace.maxPixel();

		int theta = p.x + offset;
		int dist = p.y;

		return getLineFromParams(theta, dist, -2000, 2000);
	}

	public Line2d getBestLine(float minTheta, float maxTheta) {
		if (minTheta > maxTheta) {
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}

		if (minTheta >= 0) {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage f = accum.extractROI(mt, 0, xt - mt, accum.getHeight());
			return getBestLine(f, mt);
		} else {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage a = accum.shiftRight(-mt).extractROI(0, 0, (xt - mt), accum.getHeight());
			return getBestLine(a, mt);
		}
	}

	/**
	 * 	返回图像中最佳的前N条直线
	 */
	public List<Line2d> getBestLines(int n, float minTheta, float maxTheta) {
		if (minTheta > maxTheta) {
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}

		if (minTheta >= 0) {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage f = accum.extractROI(mt, 0, xt - mt, accum.getHeight());
			return getBestLines(n, f, mt);
		} else {
			int mt = (int) (minTheta / (360d / getNumberOfSegments()));
			int xt = (int) (maxTheta / (360d / getNumberOfSegments()));
			FImage a = accum.shiftRight(-mt).extractROI(0, 0, (xt - mt), accum.getHeight());
			return getBestLines(n, a, mt);
		}
	}

	public List<Line2d> getBestLines(int n) {
		return getBestLines(n, accum, 0);
	}

	public List<Line2d> getBestLines(int n, FImage accumulatorSpace, int offset) {
		FImage accum2 = accumulatorSpace.clone();
		List<Line2d> lines = new ArrayList<Line2d>();
		for (int i = 0; i < n; i++) {
			FValuePixel p = accum2.maxPixel();
			lines.add(getLineFromParams(p.x + offset, p.y, -2000, 2000));
			accum2.setPixel(p.x, p.y, 0f);
		}

		return lines;
	}

	/**
	 * 从给定的直线的相关参数，会一个给定X方向轴的终点
	 */
	public Line2d getLineFromParams(int theta, int dist, int x1, int x2) {
		if (theta == 0) {
			return new Line2d(new Point2dImpl(dist, -2000), new Point2dImpl(dist, 2000));
		}

		double t = theta * (360d / getNumberOfSegments()) * Math.PI / 180d;
		return new Line2d(new Point2dImpl(x1, (float) (x1 * (-Math.cos(t) / Math.sin(t)) + (dist / Math.sin(t)))),
				new Point2dImpl(x2, (float) (x2 * (-Math.cos(t) / Math.sin(t)) + (dist / Math.sin(t)))));
	}

	/**
	 * 迭代器
	 */
	@Override
	public Iterator<Line2d> iterator() {
		clearIterator();
		checkIteratorSetup();
		return this;
	}

	/**
	 * 	复制迭代器
	 */
	private void checkIteratorSetup() {
		if (iteratorAccum == null)
			iteratorAccum = accum.clone();
	}

	@Override
	public boolean hasNext() {
		return iteratorAccum.maxPixel().value > 0f;
	}

	@Override
	public Line2d next() {
		iteratorCurrentPix = iteratorAccum.maxPixel();
		Line2d l = getBestLine(iteratorAccum, 0);
		iteratorAccum.setPixel(iteratorCurrentPix.x, iteratorCurrentPix.y, 0f);
		return l;
	}

	@Override
	public void remove() {
		iteratorAccum.setPixel(iteratorCurrentPix.x, iteratorCurrentPix.y, 0f);
	}

	/**
	 */
	public void clearIterator() {
		this.iteratorAccum = null;
		this.iteratorCurrentPix = null;
	}

	/**
	 *  查看在计算空间标记的数量
	 */
	public void setNumberOfSegments(int numberOfSegments) {
		this.numberOfSegments = numberOfSegments;
	}

	/**
	 * 	得到计算空间中的直线的数量
	 *
	 */
	public int getNumberOfSegments() {
		return numberOfSegments;
	}
}
