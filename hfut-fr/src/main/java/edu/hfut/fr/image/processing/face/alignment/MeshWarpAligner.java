package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;
import edu.hfut.fr.image.processing.transform.PiecewiseMeshWarp;

/**
 * Meshwarp矫正类
 *
 * @author wanggang
 */
public class MeshWarpAligner implements FaceAligner<KEDetectedFace> {

	private static final String[][] DEFAULT_MESH_DEFINITION = { { "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_MIDDLE" },
			{ "EYE_LEFT_LEFT", "EYE_LEFT_RIGHT", "NOSE_LEFT" }, { "EYE_RIGHT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_RIGHT" },
			{ "EYE_LEFT_RIGHT", "NOSE_LEFT", "NOSE_MIDDLE" }, { "EYE_RIGHT_LEFT", "NOSE_RIGHT", "NOSE_MIDDLE" },
			{ "MOUTH_LEFT", "MOUTH_RIGHT", "NOSE_MIDDLE" }, { "MOUTH_LEFT", "NOSE_LEFT", "NOSE_MIDDLE" },
			{ "MOUTH_RIGHT", "NOSE_RIGHT", "NOSE_MIDDLE" }, { "MOUTH_LEFT", "NOSE_LEFT", "EYE_LEFT_LEFT" },
			{ "MOUTH_RIGHT", "NOSE_RIGHT", "EYE_RIGHT_RIGHT" },

	};

	private static final Point2d P0 = new Point2dImpl(0, 0);
	private static final Point2d P1 = new Point2dImpl(80, 0);
	private static final Point2d P2 = new Point2dImpl(80, 80);
	private static final Point2d P3 = new Point2dImpl(0, 80);

	private static FacialKeypoint[] canonical = loadCanonicalPoints();

	String[][] meshDefinition = DEFAULT_MESH_DEFINITION;

	FImage mask;

	/**
	 * 默认构造方法
	 */
	public MeshWarpAligner() {
		this(DEFAULT_MESH_DEFINITION);
	}

	public MeshWarpAligner(String[][] meshDefinition) {
		this.meshDefinition = meshDefinition;

		final List<Pair<Shape>> mesh = createMesh(canonical);

		mask = new FImage((int) P2.getX(), (int) P2.getY());
		mask.fill(1f);
		mask = mask.processInplace(new PiecewiseMeshWarp<Float, FImage>(mesh));
	}

	private static FacialKeypoint[] loadCanonicalPoints() {
		final FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];

		for (int i = 0; i < points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(2 * AffineAligner.Pmu[0][i] - 40, 2 * AffineAligner.Pmu[1][i] - 40);
		}

		return points;
	}

	protected FacialKeypoint[] getActualPoints(FacialKeypoint[] keys, Matrix tf0) {
		final FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];

		for (int i = 0; i < points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(
					FacialKeypoint.getKeypoint(keys, FacialKeypointType.valueOf(i)).position.transform(tf0));
		}

		return points;
	}

	protected List<Pair<Shape>> createMesh(FacialKeypoint[] det) {
		final List<Pair<Shape>> shapes = new ArrayList<Pair<Shape>>();

		for (final String[] vertDefs : meshDefinition) {
			final Polygon p1 = new Polygon();
			final Polygon p2 = new Polygon();

			for (final String v : vertDefs) {
				p1.getVertices().add(lookupVertex(v, det));
				p2.getVertices().add(lookupVertex(v, canonical));
			}
			shapes.add(new Pair<Shape>(p1, p2));
		}

		return shapes;
	}

	private Point2d lookupVertex(String v, FacialKeypoint[] pts) {
		if (v.equals("P0"))
			return P0;
		if (v.equals("P1"))
			return P1;
		if (v.equals("P2"))
			return P2;
		if (v.equals("P3"))
			return P3;

		return FacialKeypoint.getKeypoint(pts, FacialKeypointType.valueOf(v)).position;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final float scalingX = P2.getX() / descriptor.getFacePatch().width;
		final float scalingY = P2.getY() / descriptor.getFacePatch().height;
		final Matrix tf0 = TransformUtilities.scaleMatrix(scalingX, scalingY);
		final Matrix tf = tf0.inverse();

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		final FImage smallpatch = FKEFaceDetector.extractPatch(J, tf, 80, 0);

		return getWarpedImage(descriptor.getKeypoints(), smallpatch, tf0);
	}

	protected FImage getWarpedImage(FacialKeypoint[] kpts, FImage patch, Matrix tf0) {
		final FacialKeypoint[] det = getActualPoints(kpts, tf0);
		final List<Pair<Shape>> mesh = createMesh(det);

		final FImage newpatch = patch.process(new PiecewiseMeshWarp<Float, FImage>(mesh));

		return newpatch;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		int sz = in.readInt();
		meshDefinition = new String[sz][];
		for (int i = 0; i < meshDefinition.length; i++) {
			sz = in.readInt();
			meshDefinition[i] = new String[sz];
			for (int j = 0; j < meshDefinition[i].length; j++)
				meshDefinition[i][j] = in.readUTF();
		}

		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(meshDefinition.length);
		for (final String[] def : meshDefinition) {
			out.writeInt(def.length);
			for (final String s : def)
				out.writeUTF(s);
		}

		ImageUtilities.write(mask, "png", out);
	}

}
