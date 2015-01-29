package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;

import edu.hfut.fr.image.processing.face.detection.CLMDetectedFace;
import edu.hfut.fr.image.processing.face.detection.CLMFaceDetector.Configuration;
import edu.hfut.fr.image.processing.face.tracking.clm.CLMFaceTracker;
import edu.hfut.fr.image.processing.transform.PiecewiseMeshWarp;

/**
 * 姿势矫正器
 *
 * @author wanggang
 */
public class CLMAligner implements FaceAligner<CLMDetectedFace> {

	private Configuration config;
	private int size = 100;
	private transient List<Triangle> referenceTriangles;
	private transient FImage mask;

	public CLMAligner() {
		config = new Configuration();
		loadReference();
	}

	public CLMAligner(int size) {
		this.size = size;
		config = new Configuration();
		loadReference();
	}

	private void loadReference() {
		referenceTriangles = CLMFaceTracker.getTriangles(config.referenceShape, null, this.config.triangles);

		mask = new FImage(size, size);

		for (final Triangle t : referenceTriangles) {
			t.scale(0.3f * size);
			t.translate(0.5f * size, 0.45f * size);

			mask.drawShapeFilled(t, 1f);
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		config = IOUtils.read(in);
		loadReference();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(config, out);
	}

	@Override
	public FImage align(CLMDetectedFace face) {
		if (face == null)
			return null;

		final List<Triangle> triangles = CLMFaceTracker.getTriangles(face.getShapeMatrix(), face.getVisibility(),
				this.config.triangles);
		final List<Pair<Shape>> matches = computeMatches(triangles);

		final PiecewiseMeshWarp<Float, FImage> pmw = new PiecewiseMeshWarp<Float, FImage>(matches);

		return pmw.transform(face.getFacePatch(), size, size);
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	private List<Pair<Shape>> computeMatches(List<Triangle> triangles) {
		final List<Pair<Shape>> mtris = new ArrayList<Pair<Shape>>();

		for (int i = 0; i < triangles.size(); i++) {
			final Triangle t1 = triangles.get(i);
			final Triangle t2 = referenceTriangles.get(i);

			if (t1 != null && t2 != null) {
				mtris.add(new Pair<Shape>(t1, t2));
			}
		}

		return mtris;
	}

}
