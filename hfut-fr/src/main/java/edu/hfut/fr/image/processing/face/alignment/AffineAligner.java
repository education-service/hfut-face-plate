package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import Jama.Matrix;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 面部图像矫正化
 *
 * @author wanggang
 */

public class AffineAligner implements FaceAligner<KEDetectedFace> {

	protected final static float[][] Pmu = {
			{ 25.0347f, 34.1802f, 44.1943f, 53.4623f, 34.1208f, 39.3564f, 44.9156f, 31.1454f, 47.8747f },
			{ 34.1580f, 34.1659f, 34.0936f, 33.8063f, 45.4179f, 47.0043f, 45.3628f, 53.0275f, 52.7999f } };

	final static int CANONICAL_SIZE = 80;

	int facePatchWidth = 80;
	int facePatchHeight = 80;
	float facePatchBorderPercentage = 0.225f;

	private FImage mask;

	public AffineAligner() {
		this(loadDefaultMask());
	};

	/**
	 * 默认构造函数
	 */
	public AffineAligner(FImage mask) {
		this.mask = mask;
	}

	public AffineAligner(FImage mask, float facePatchBorderPercentage) {
		this.mask = mask;
		this.facePatchBorderPercentage = facePatchBorderPercentage;
		this.facePatchHeight = mask.height;
		this.facePatchWidth = mask.width;
	}

	public AffineAligner(int facePatchWidth, int facePatchHeight, float facePatchBorderPercentage) {
		this.mask = new FImage(facePatchWidth, facePatchHeight);
		mask.fill(1f);
		this.facePatchBorderPercentage = facePatchBorderPercentage;
		this.facePatchWidth = facePatchWidth;
		this.facePatchHeight = facePatchHeight;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final int facePatchSize = Math.max(facePatchWidth, facePatchHeight);
		final double size = facePatchSize + 2.0 * facePatchSize * facePatchBorderPercentage;
		final double sc = CANONICAL_SIZE / size;

		final Matrix T = estimateAffineTransform(descriptor);
		T.set(0, 0, T.get(0, 0) * sc);
		T.set(1, 1, T.get(1, 1) * sc);
		T.set(0, 1, T.get(0, 1) * sc);
		T.set(1, 0, T.get(1, 0) * sc);

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), T);
		final FImage bigPatch = FKEFaceDetector.extractPatch(J, T, (int) size,
				(int) (facePatchSize * facePatchBorderPercentage));

		return bigPatch.extractCenter(facePatchWidth, facePatchHeight)
				.extractROI(0, 0, facePatchWidth, facePatchHeight).multiplyInplace(mask);
	}

	/**
	 * 返回矫正矩阵
	 */
	public static Matrix estimateAffineTransform(KEDetectedFace face) {
		return estimateAffineTransform(face.getKeypoints());
	}

	protected static Matrix estimateAffineTransform(FacialKeypoint[] pts) {
		float emin = Float.POSITIVE_INFINITY;
		Matrix T = null;

		for (int c = 0; c < 9; c++) {
			final Matrix A = new Matrix(8, 3);
			final Matrix B = new Matrix(8, 3);
			for (int i = 0, j = 0; i < 9; i++) {
				if (i != 8 - c) {
					A.set(j, 0, Pmu[0][i]);
					A.set(j, 1, Pmu[1][i]);
					A.set(j, 2, 1);
					B.set(j, 0, pts[i].position.x);
					B.set(j, 1, pts[i].position.y);
					B.set(j, 2, 1);
					j++;
				}
			}

			final Matrix Tc = A.solve(B).transpose();

			final Matrix P1 = Tc.times(A.transpose());
			final Matrix D = P1.minus(B.transpose());

			float e = 0;
			for (int cc = 0; cc < D.getColumnDimension(); cc++) {
				float colsum = 0;
				for (int rr = 0; rr < D.getRowDimension(); rr++) {
					colsum += D.get(rr, cc) * D.get(rr, cc);
					;
				}
				e += Math.sqrt(colsum);
			}

			if (e < emin) {
				emin = e;
				T = Tc;
			}
		}

		return T;
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
		facePatchWidth = in.readInt();
		facePatchHeight = in.readInt();
		facePatchBorderPercentage = in.readFloat();
		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(facePatchWidth);
		out.writeInt(facePatchHeight);
		out.writeFloat(facePatchBorderPercentage);
		ImageUtilities.write(mask, "png", out);
	}

}
