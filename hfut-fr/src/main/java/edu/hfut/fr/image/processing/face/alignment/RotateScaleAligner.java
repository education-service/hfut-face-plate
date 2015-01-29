package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 使用旋转和缩放矫正
 *
 *@author wanggang
 */
public class RotateScaleAligner implements FaceAligner<KEDetectedFace> {

	private static final FImage DEFAULT_MASK = loadDefaultMask();

	private int eyeDist = 68;
	private int eyePaddingLeftRight = 6;
	private int eyePaddingTop = 20;

	private FImage mask = DEFAULT_MASK;

	/**
	 * 默认构造.
	 */
	public RotateScaleAligner() {
	}

	public RotateScaleAligner(int targetSize) {
		final int canonicalSize = 2 * eyePaddingLeftRight + eyeDist;

		final double sf = targetSize / canonicalSize;

		eyeDist = (int) (eyeDist * sf);
		eyePaddingLeftRight = (targetSize - eyeDist) / 2;
		eyePaddingTop = (int) (eyePaddingTop * sf);
	}

	public RotateScaleAligner(FImage mask) {
		this.mask = mask;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final FacialKeypoint lefteye = descriptor.getKeypoint(FacialKeypointType.EYE_LEFT_LEFT);
		final FacialKeypoint righteye = descriptor.getKeypoint(FacialKeypointType.EYE_RIGHT_RIGHT);

		final float dx = righteye.position.x - lefteye.position.x;
		final float dy = righteye.position.y - lefteye.position.y;

		final float rotation = (float) Math.atan2(dy, dx);
		final float scaling = (float) (eyeDist / Math.sqrt(dx * dx + dy * dy));

		final float tx = lefteye.position.x - eyePaddingLeftRight / scaling;
		final float ty = lefteye.position.y - eyePaddingTop / scaling;

		final Matrix tf0 = TransformUtilities.scaleMatrix(scaling, scaling)
				.times(TransformUtilities.translateMatrix(-tx, -ty))
				.times(TransformUtilities.rotationMatrixAboutPoint(-rotation, lefteye.position.x, lefteye.position.y));
		final Matrix tf = tf0.inverse();

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		return FKEFaceDetector.extractPatch(J, tf, 2 * eyePaddingLeftRight + eyeDist, 0);
	}

	private static FImage loadDefaultMask() {
		try {
			return ImageUtilities.readF(FaceAligner.class.getResourceAsStream("affineMask.png"));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		eyeDist = in.readInt();
		eyePaddingLeftRight = in.readInt();
		eyePaddingTop = in.readInt();

		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(eyeDist);
		out.writeInt(eyePaddingLeftRight);
		out.writeInt(eyePaddingTop);

		ImageUtilities.write(mask, "png", out);
	}

}
