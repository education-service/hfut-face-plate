package edu.hfut.fr.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;
import edu.hfut.fr.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import edu.hfut.fr.image.processing.face.tracking.clm.MultiTracker.TrackerVars;

/**
 * CLM检测面部实现类
 *
 * @author wanggang
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Jason M. Saragih", "Simon Lucey", "Jeffrey F. Cohn" }, title = "Face alignment through subspace constrained mean-shifts", year = "2009", booktitle = "IEEE 12th International Conference on Computer Vision, ICCV 2009, Kyoto, Japan, September 27 - October 4, 2009", pages = {
		"1034", "1041" }, publisher = "IEEE", customData = { "doi", "http://dx.doi.org/10.1109/ICCV.2009.5459377",
		"researchr", "http://researchr.org/publication/SaragihLC09", "cites", "0", "citedby", "0" })
public class CLMDetectedFace extends DetectedFace {

	private Matrix shape;
	private Matrix poseParameters;
	private Matrix shapeParameters;
	private Matrix visibility;

	protected CLMDetectedFace() {
	}

	/**
	 * 构造函数
	 */
	public CLMDetectedFace(final TrackedFace face, final FImage image) {
		this(face.redetectedBounds, face.shape.copy(), face.clm._pglobl.copy(), face.clm._plocal.copy(),
				face.clm._visi[face.clm.getViewIdx()].copy(), image);
	}

	public CLMDetectedFace(final Rectangle bounds, final Matrix shape, final Matrix poseParameters,
			final Matrix shapeParameters, final Matrix visibility, final FImage fullImage) {
		super(bounds, fullImage.extractROI(bounds), 1);

		this.poseParameters = poseParameters;
		this.shapeParameters = shapeParameters;
		this.visibility = visibility;

		this.shape = shape;

		final int n = shape.getRowDimension() / 2;
		final double[][] shapeData = shape.getArray();
		for (int i = 0; i < n; i++) {
			shapeData[i][0] -= bounds.x;
			shapeData[i + n][0] -= bounds.y;
		}
	}

	public static List<CLMDetectedFace> convert(final List<TrackedFace> faces, final MBFImage image) {
		final FImage fimage = image.flatten();

		return CLMDetectedFace.convert(faces, fimage);
	}

	public static List<CLMDetectedFace> convert(final List<TrackedFace> faces, final FImage image) {
		final List<CLMDetectedFace> cvt = new ArrayList<CLMDetectedFace>();

		for (final TrackedFace f : faces) {
			cvt.add(new CLMDetectedFace(f, image));
		}

		return cvt;
	}

	public TrackedFace convert() {
		final TrackerVars tv = new TrackerVars();
		tv.clm._pglobl = this.poseParameters.copy();
		tv.clm._plocal = this.shapeParameters.copy();
		tv.shape = this.shape.copy();
		tv.clm._visi[tv.clm.getViewIdx()] = this.visibility.copy();
		return new TrackedFace(this.bounds, tv);
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		super.writeBinary(out);

		IOUtils.write(this.getShape(), out);
		IOUtils.write(this.poseParameters, out);
		IOUtils.write(this.shapeParameters, out);
	}

	@Override
	public byte[] binaryHeader() {
		return "DF".getBytes();
	}

	@Override
	public void readBinary(final DataInput in) throws IOException {
		super.readBinary(in);
		this.shape = IOUtils.read(in);
		this.poseParameters = IOUtils.read(in);
		this.shapeParameters = IOUtils.read(in);
	}

	/**
	 * 返回面部大小
	 */
	public double getScale() {
		return this.poseParameters.get(0, 0);
	}

	public double getPitch() {
		return this.poseParameters.get(1, 0);
	}

	public double getYaw() {
		return this.poseParameters.get(2, 0);
	}

	public double getRoll() {
		return this.poseParameters.get(3, 0);
	}

	public double getTranslationX() {
		return this.poseParameters.get(4, 0);
	}

	public double getTranslationY() {
		return this.poseParameters.get(5, 0);
	}

	/**
	 * 得到描述面部位置点
	 */
	public DoubleFV getPoseParameters() {
		return new DoubleFV(new double[] { this.getPitch(), this.getYaw(), this.getRoll() });
	}

	public DoubleFV getShapeParameters() {
		final int len = this.shapeParameters.getRowDimension();
		final double[] vector = new double[len];

		for (int i = 0; i < len; i++) {
			vector[i] = this.shapeParameters.get(i, 0);
		}

		return new DoubleFV(vector);
	}

	/**
	 * 返回向量
	 */
	public DoubleFV getPoseShapeParameters() {
		final int len = this.shapeParameters.getRowDimension();
		final double[] vector = new double[len + 3];

		vector[0] = this.getPitch();
		vector[1] = this.getYaw();
		vector[2] = this.getRoll();

		for (int i = 3; i < len + 3; i++) {
			vector[i] = this.shapeParameters.get(i, 0);
		}

		return new DoubleFV(vector);
	}

	/**
	 * 得到描述矩阵大小
	 */
	public Matrix getShapeMatrix() {
		return this.shape;
	}

	/**
	 * 得到可视化矩阵
	 */
	public Matrix getVisibility() {
		return this.visibility;
	}

}
