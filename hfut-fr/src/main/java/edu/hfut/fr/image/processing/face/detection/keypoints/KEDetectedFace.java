package edu.hfut.fr.image.processing.face.detection.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;

/**
 * A K(eypoint)E(nriched) 人脸检测
 *
 * @author wanggang
 */
public class KEDetectedFace extends DetectedFace {

	protected FacialKeypoint[] keypoints;

	public KEDetectedFace() {
		super();
	}

	public KEDetectedFace(Rectangle bounds, FImage patch, FacialKeypoint[] keypoints, float confidence) {
		super(bounds, patch, confidence);

		this.keypoints = keypoints;
	}

	/**
	 * 获取特定类型的特征点
	 */
	public FacialKeypoint getKeypoint(FacialKeypointType type) {
		return FacialKeypoint.getKeypoint(keypoints, type);
	}

	/**
	 * 获取特定类型的特征点
	 */
	public FacialKeypoint getKeypointInterpolated(FacialKeypointType type) {
		final FacialKeypoint kpt = getKeypoint(type);

		if (kpt == null) {
			switch (type) {
			case EYE_LEFT_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.EYE_LEFT_LEFT),
						getKeypoint(FacialKeypointType.EYE_LEFT_RIGHT));
			case EYE_RIGHT_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.EYE_RIGHT_LEFT),
						getKeypoint(FacialKeypointType.EYE_RIGHT_RIGHT));
			case MOUTH_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.MOUTH_LEFT),
						getKeypoint(FacialKeypointType.MOUTH_RIGHT));
			default:
				break;
			}
		}
		return null;
	}

	private FacialKeypoint createInterpolated(FacialKeypointType type, FacialKeypoint left, FacialKeypoint right) {
		if (left == null || right == null)
			return null;

		final float x = right.position.x - left.position.x;
		final float y = right.position.y - left.position.y;

		return new FacialKeypoint(type, new Point2dImpl(x, y));
	}

	public FacialKeypoint[] getKeypoints() {
		return keypoints;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);

		out.writeInt(keypoints.length);
		for (final FacialKeypoint k : keypoints)
			k.writeBinary(out);
	}

	@Override
	public byte[] binaryHeader() {
		return "KEDF".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);

		final int sz = in.readInt();
		keypoints = new FacialKeypoint[sz];
		for (int i = 0; i < sz; i++) {
			keypoints[i] = new FacialKeypoint();
			keypoints[i].readBinary(in);
		}
	}

}
