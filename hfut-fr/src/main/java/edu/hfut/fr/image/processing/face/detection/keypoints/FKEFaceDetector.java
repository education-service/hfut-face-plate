package edu.hfut.fr.image.processing.face.detection.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.hash.HashCodeUtil;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import edu.hfut.fr.image.analysis.pyramid.SimplePyramid;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;
import edu.hfut.fr.image.processing.transform.ProjectionProcessor;

/**
 * F(rontal)K(eypoint)E(nriched)人脸检测器
 *
 *@author wanggang
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Mark Everingham", "Josef Sivic", "Andrew Zisserman" }, title = "Hello! My name is... Buffy - Automatic naming of characters in TV video", year = "2006", booktitle = "In BMVC")
public class FKEFaceDetector implements FaceDetector<KEDetectedFace, FImage> {

	protected FaceDetector<? extends DetectedFace, FImage> faceDetector;
	protected FacialKeypointExtractor facialKeypointExtractor = new FacialKeypointExtractor();
	private float patchScale = 1.0f;

	/**
	 * 默认构造函数
	 */
	public FKEFaceDetector() {
		this(new HaarCascadeDetector(80));
	}

	/**
	 * 构造函数
	 */
	public FKEFaceDetector(int size) {
		this(new HaarCascadeDetector(size));
	}

	/**
	 * 构造函数
	 */
	public FKEFaceDetector(float patchScale) {
		this(new HaarCascadeDetector(80), patchScale);
	}

	/**
	 * 构造函数
	 */
	public FKEFaceDetector(int size, float patchScale) {
		this(new HaarCascadeDetector(size), patchScale);
	}

	/**
	 * 构造函数
	 */
	public FKEFaceDetector(FaceDetector<? extends DetectedFace, FImage> detector) {
		this.faceDetector = detector;
	}

	/**
	 * 构造函数
	 */
	public FKEFaceDetector(FaceDetector<? extends DetectedFace, FImage> detector, float patchScale) {
		this.faceDetector = detector;
		this.patchScale = patchScale;
	}

	/**
	 * 使用金字塔算法改变图片大小
	 */
	public static FImage pyramidResize(FImage image, Matrix transform) {
		final SingularValueDecomposition svd = transform.getMatrix(0, 1, 0, 1).svd();
		final double sv[] = svd.getSingularValues();
		final double scale = ((sv[0] + sv[1]) / 2);

		final int lev = (int) (Math.max(Math.floor(Math.log(scale) / Math.log(1.5)), 0) + 1);
		final double pyramidScale = Math.pow(1.5, (lev - 1));

		final Matrix scaleMatrix = TransformUtilities.scaleMatrix(1 / pyramidScale, 1 / pyramidScale);
		final Matrix newTransform = scaleMatrix.times(transform);
		transform.setMatrix(0, 2, 0, 2, newTransform);

		return image.process(new SimplePyramid<FImage>(1.5f, lev));
	}

	public static FImage extractPatch(FImage image, Matrix transform, int size, int border) {
		final ProjectionProcessor<Float, FImage> pp = new ProjectionProcessor<Float, FImage>();

		pp.setMatrix(transform.inverse());
		image.accumulateWith(pp);

		return pp.performProjection(border, size - border, border, size - border, RGBColour.BLACK[0]);
	}

	@Override
	public List<KEDetectedFace> detectFaces(FImage image) {
		final List<? extends DetectedFace> faces = faceDetector.detectFaces(image);

		final List<KEDetectedFace> descriptors = new ArrayList<KEDetectedFace>(faces.size());
		for (final DetectedFace df : faces) {
			final int canonicalSize = facialKeypointExtractor.getCanonicalImageDimension();
			final Rectangle r = df.getBounds();

			final float scale = (r.width / 2) / ((canonicalSize / 2) - facialKeypointExtractor.model.border);
			float tx = (r.x + (r.width / 2)) - scale * canonicalSize / 2;
			float ty = (r.y + (r.height / 2)) - scale * canonicalSize / 2;

			final Matrix T0 = new Matrix(new double[][] { { scale, 0, tx }, { 0, scale, ty }, { 0, 0, 1 } });
			final Matrix T = (Matrix) T0.clone();

			final FImage subsampled = pyramidResize(image, T);
			final FImage smallpatch = extractPatch(subsampled, T, canonicalSize, 0);

			final FacialKeypoint[] kpts = facialKeypointExtractor.extractFacialKeypoints(smallpatch);

			tx = (r.width / 2) - scale * canonicalSize / 2;
			ty = (r.height / 2) - scale * canonicalSize / 2;
			final Matrix T1 = new Matrix(new double[][] { { scale, 0, tx }, { 0, scale, ty }, { 0, 0, 1 } });
			FacialKeypoint.updateImagePosition(kpts, T1);

			final FacialKeypoint eyeLL = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_LEFT_LEFT);
			final FacialKeypoint eyeRR = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_RIGHT_RIGHT);
			final FacialKeypoint eyeLR = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_LEFT_RIGHT);
			final FacialKeypoint eyeRL = FacialKeypoint.getKeypoint(kpts,
					FacialKeypoint.FacialKeypointType.EYE_RIGHT_LEFT);

			final float eyeSpace = (0.5f * (eyeRR.position.x + eyeRL.position.x))
					- (0.5f * (eyeLR.position.x + eyeLL.position.x));
			final float deltaX = (0.5f * (eyeLR.position.x + eyeLL.position.x)) - eyeSpace;
			r.x = r.x + deltaX;
			r.width = eyeSpace * 3;

			final float eyeVavg = 0.5f * ((0.5f * (eyeRR.position.y + eyeRL.position.y)) + (0.5f * (eyeLR.position.y + eyeLL.position.y)));

			r.height = 1.28f * r.width;
			final float deltaY = eyeVavg - 0.4f * r.height;
			r.y = r.y + deltaY;

			float dx = r.x;
			float dy = r.y;
			r.scaleCentroid(patchScale);
			dx = dx - r.x;
			dy = dy - r.y;
			FacialKeypoint.updateImagePosition(kpts, TransformUtilities.translateMatrix(-deltaX + dx, -deltaY + dy));

			final KEDetectedFace kedf = new KEDetectedFace(r, image.extractROI(r), kpts, df.getConfidence());
			descriptors.add(kedf);
		}

		return descriptors;
	}

	@Override
	public int hashCode() {
		final int hashCode = HashCodeUtil.SEED;
		HashCodeUtil.hash(hashCode, this.faceDetector);
		HashCodeUtil.hash(hashCode, this.facialKeypointExtractor);
		HashCodeUtil.hash(hashCode, this.patchScale);
		return hashCode;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		faceDetector = IOUtils.newInstance(in.readUTF());
		faceDetector.readBinary(in);
		// facialKeypointExtractor;
		this.patchScale = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "FKED".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(faceDetector.getClass().getName());
		faceDetector.writeBinary(out);
		out.writeFloat(patchScale);
	}

	@Override
	public String toString() {
		return String.format("FKEFaceDetector[innerDetector=%s]", faceDetector);
	}

}
