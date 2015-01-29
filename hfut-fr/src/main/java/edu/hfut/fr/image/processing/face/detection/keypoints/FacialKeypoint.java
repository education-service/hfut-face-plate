package edu.hfut.fr.image.processing.face.detection.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * 代表人脸的特征点
 *
 * @author wanggang
 */
public class FacialKeypoint implements ReadWriteableBinary {

	public static enum FacialKeypointType {

		EYE_LEFT_LEFT,

		EYE_LEFT_RIGHT,

		EYE_RIGHT_LEFT,

		EYE_RIGHT_RIGHT,

		NOSE_LEFT,

		NOSE_MIDDLE,

		NOSE_RIGHT,

		MOUTH_LEFT,

		MOUTH_RIGHT,

		EYE_LEFT_CENTER,

		EYE_RIGHT_CENTER,

		NOSE_BRIDGE,

		MOUTH_CENTER;

		public static FacialKeypointType valueOf(int ordinal) {
			return FacialKeypointType.values()[ordinal];
		}
	}

	public FacialKeypointType type;

	public Point2dImpl position;

	public FacialKeypoint() {
		this.type = FacialKeypointType.EYE_LEFT_CENTER;
		position = new Point2dImpl(0, 0);
	}

	public FacialKeypoint(FacialKeypointType type) {
		this.type = type;
		position = new Point2dImpl(0, 0);
	}

	public FacialKeypoint(FacialKeypointType type, Point2d pt) {
		this.type = type;
		position = new Point2dImpl(pt);
	}

	protected void updatePosition(Matrix transform) {
		position = position.transform(transform);
	}

	protected static void updateImagePosition(FacialKeypoint[] kpts, Matrix transform) {
		for (FacialKeypoint k : kpts)
			k.updatePosition(transform);
	}

	/**
	 * 获取人脸特征点
	 */
	public static FacialKeypoint getKeypoint(FacialKeypoint[] pts, FacialKeypointType type) {
		for (FacialKeypoint fk : pts) {
			if (fk.type == type)
				return fk;
		}
		return null;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		type = FacialKeypointType.valueOf(in.readUTF());
		position.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(type.name());
		position.writeBinary(out);
	}

}
