package edu.hfut.fr.image.analysis.watershed.feature;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.IntValuePixel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.util.QuadraticEquation;

import Jama.Matrix;

/**
 * 计算特征值
 *
 * @author wanghao
 */
public class MomentFeature implements ComponentFeature {

	int n = 0;
	double mx = 0;
	double my = 0;
	double Mx2 = 0;
	double My2 = 0;
	double sxy = 0;
	double sx = 0;
	double sy = 0;

	@Override
	public void merge(ComponentFeature f) {
		MomentFeature mf = (MomentFeature) f;

		double dx = mf.mx - mx;
		double dy = mf.my - my;

		mx = (n * mx + mf.n * mf.mx) / (n + mf.n);
		my = (n * my + mf.n * mf.my) / (n + mf.n);

		Mx2 += mf.Mx2 + dx * dx * n * mf.n / (n + mf.n);
		My2 += mf.My2 + dy * dy * n * mf.n / (n + mf.n);

		n += mf.n;
		sxy += mf.sxy;
		sx += mf.sx;
		sy += mf.sy;
	}

	@Override
	public void addSample(IntValuePixel p) {
		n++;
		double dx = p.x - mx;
		double dy = p.y - my;

		mx += dx / n;
		my += dy / n;

		Mx2 += dx * (p.x - mx);
		My2 += dy * (p.y - my);

		sx += p.x;
		sy += p.y;
		sxy += p.x * p.y;
	}

	/**
	 * 获得计算像素值
	 */
	public double n() {
		return n;
	}

	/**
	 * 获得u11值
	 */
	public double u11() {
		return (sxy - sx * sy / n) / n;
	}

	/**
	 * 获得u20特征值
	 */
	public double u20() {
		return Mx2 / n;
	}

	/**
	 * 获得u02值
	 */
	public double u02() {
		return My2 / n;
	}

	/**
	 * 获得m10特征值
	 */
	public double m10() {
		return mx;
	}

	/**
	 * 获得m01值
	 */
	public double m01() {
		return my;
	}

	@Override
	public MomentFeature clone() {
		try {
			return (MomentFeature) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * 获得ellipse对象
	 */

	public Ellipse getEllipse() {
		return getEllipse(1);
	}

	public Ellipse getEllipse(float sf) {
		float u = (float) m10();
		float v = (float) m01();
		Matrix sm = new Matrix(new double[][] { { u20(), u11() }, { u11(), u02() } });
		return EllipseUtilities.ellipseFromCovariance(u, v, sm, sf);
	}

	/**
	 * 获得circle对象
	 */
	public Circle getCircle(float sf) {
		Ellipse e = getEllipse(sf);
		Point2d p = e.calculateCentroid();
		return new Circle(p.getX(), p.getY(), (float) (e.getMajor() + e.getMinor()) / 2);
	}

	/**
	 * 创建旋转矩阵
	 */
	public Polygon getEllipseBoundingBox(float sf) {
		return this.getEllipse(sf).calculateOrientedBoundingBox();
	}

	/**
	 * 获得特征初始值
	 */
	public double getOrientation() {
		double xx = u20();
		double xy = u11();
		double yy = u02();

		return 0.5 * Math.atan2(2 * xy, xx - yy);
	}

	protected double[] getEllipseBoundingRectsData(double sf) {
		double xx = u20();
		double xy = u11();
		double yy = u02();

		double theta = 0.5 * Math.atan2(2 * xy, xx - yy);

		double trace = xx + yy;
		double det = (xx * yy) - (xy * xy);
		double[] eigval = QuadraticEquation.solveGeneralQuadratic(1, -1 * trace, det);

		double a = 1.0 + Math.sqrt(Math.abs(eigval[1])) * sf * 4;
		double b = 1.0 + Math.sqrt(Math.abs(eigval[0])) * sf * 4;

		double[] data = { Math.max(a, b), Math.min(a, b), theta };

		return data;
	}

	/**
	 * 提取ellipsePatch特征
	 */
	public FImage extractEllipsePatch(FImage image, double sf) {
		double[] data = getEllipseBoundingRectsData(sf);
		double height = data[1], width = data[0], ori = data[2];

		int sx = (int) Math.rint(width);
		int sy = (int) Math.rint(height);

		FImage patch = new FImage(sx, sy);

		for (int y = 0; y < sy; y++) {
			for (int x = 0; x < sx; x++) {
				double xbar = x - sx / 2.0;
				double ybar = y - sy / 2.0;

				double xx = (xbar * Math.cos(ori) - ybar * Math.sin(ori)) + mx;
				double yy = (xbar * Math.sin(ori) + ybar * Math.cos(ori)) + my;

				patch.setPixel(x, y, image.getPixelInterp(xx, yy));
			}
		}

		return patch;
	}

}
