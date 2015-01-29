package edu.hfut.fr.image.processing.face.tracking.clm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;

import Jama.Matrix;

import com.jsaragih.IO;
import com.jsaragih.Tracker;

import edu.hfut.fr.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import edu.hfut.fr.image.processing.face.tracking.clm.MultiTracker.TrackerVars;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;

/**
 * 基于CLM的人脸跟踪
 *
 *@author jimbo
 */
public class CLMFaceTracker {

	public MultiTracker model = null;

	public int[][] triangles = null;

	public int[][] connections = null;

	public float scale = 1f;

	public boolean fcheck = false;

	public int fpd = -1;

	public int[] wSize1 = { 7 };

	public int[] wSize2 = { 11, 9, 7 };

	public int nIter = 5;

	public double clamp = 3;

	public double fTol = 0.01;

	private boolean failed = true;

	public float searchAreaSize = 1.4f;

	private Float[] connectionColour = RGBColour.WHITE;

	private Float[] pointColour = RGBColour.GREEN;

	private Float[] meshColour = RGBColour.BLACK;

	private Float[] boundingBoxColour = RGBColour.RED;

	private Float[] searchAreaColour = RGBColour.YELLOW;

	public CLMFaceTracker() {
		this.model = new MultiTracker(MultiTracker.load(Tracker.class.getResourceAsStream("face2.tracker")));
		this.triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
		this.connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
	}

	public void track(final MBFImage frame) {
		final FImage im = frame.flatten();

		this.track(im);
	}

	public void track(FImage im) {
		if (this.scale != 1)
			if (this.scale == 0.5f)
				im = ResizeProcessor.halfSize(im);
			else
				im = ResizeProcessor.resample(im, (int) (this.scale * im.width), (int) (this.scale * im.height));

		int[] wSize;
		if (this.failed)
			wSize = this.wSize2;
		else
			wSize = this.wSize1;

		if (this.model.track(im, wSize, this.fpd, this.nIter, this.clamp, this.fTol, this.fcheck, this.searchAreaSize) == 0) {
			this.failed = false;
		} else {
			this.model.frameReset();
			this.failed = true;
		}
	}

	public void reset() {
		this.model.frameReset();
	}

	public void drawModel(final MBFImage image, final boolean drawTriangles, final boolean drawConnections,
			final boolean drawPoints, final boolean drawSearchArea, final boolean drawBounds) {
		for (int fc = 0; fc < this.model.trackedFaces.size(); fc++) {
			final MultiTracker.TrackedFace f = this.model.trackedFaces.get(fc);

			if (drawSearchArea) {
				final Rectangle r = f.lastMatchBounds.clone();
				r.scaleCentroid(this.searchAreaSize);
				image.createRenderer().drawShape(r, RGBColour.YELLOW);
			}

			CLMFaceTracker.drawFaceModel(image, f, drawTriangles, drawConnections, drawPoints, drawSearchArea,
					drawBounds, this.triangles, this.connections, this.scale, this.boundingBoxColour, this.meshColour,
					this.connectionColour, this.pointColour);
		}
	}

	public static void drawFaceModel(final MBFImage image, final MultiTracker.TrackedFace f,
			final boolean drawTriangles, final boolean drawConnections, final boolean drawPoints,
			final boolean drawSearchArea, final boolean drawBounds, final int[][] triangles, final int[][] connections,
			final float scale, final Float[] boundingBoxColour, final Float[] meshColour,
			final Float[] connectionColour, final Float[] pointColour) {
		final int n = f.shape.getRowDimension() / 2;
		final Matrix visi = f.clm._visi[f.clm.getViewIdx()];

		if (drawBounds && f.lastMatchBounds != null)
			image.createRenderer().drawShape(f.lastMatchBounds, boundingBoxColour);

		if (drawTriangles) {
			for (int i = 0; i < triangles.length; i++) {
				if (visi.get(triangles[i][0], 0) == 0 || visi.get(triangles[i][1], 0) == 0
						|| visi.get(triangles[i][2], 0) == 0)
					continue;

				final Triangle t = new Triangle(new Point2dImpl((float) f.shape.get(triangles[i][0], 0) / scale,
						(float) f.shape.get(triangles[i][0] + n, 0) / scale), new Point2dImpl((float) f.shape.get(
						triangles[i][1], 0) / scale, (float) f.shape.get(triangles[i][1] + n, 0) / scale),
						new Point2dImpl((float) f.shape.get(triangles[i][2], 0) / scale, (float) f.shape.get(
								triangles[i][2] + n, 0) / scale));
				image.drawShape(t, meshColour);
			}
		}

		if (drawConnections) {
			for (int i = 0; i < connections[0].length; i++) {
				if (visi.get(connections[0][i], 0) == 0 || visi.get(connections[1][i], 0) == 0)
					continue;

				image.drawLine(
						new Point2dImpl((float) f.shape.get(connections[0][i], 0) / scale, (float) f.shape.get(
								connections[0][i] + n, 0) / scale),
						new Point2dImpl((float) f.shape.get(connections[1][i], 0) / scale, (float) f.shape.get(
								connections[1][i] + n, 0) / scale), connectionColour);
			}
		}

		if (drawPoints) {
			// 画出点
			for (int i = 0; i < n; i++) {
				if (visi.get(i, 0) == 0)
					continue;

				image.drawPoint(new Point2dImpl((float) f.shape.get(i, 0) / scale, (float) f.shape.get(i + n, 0)
						/ scale), pointColour, 2);
			}
		}
	}

	public int[][] getReferenceTriangles() {
		return this.triangles;
	}

	public int[][] getReferenceConnections() {
		return this.connections;
	}

	public MultiTracker getModelTracker() {
		return this.model;
	}

	public TrackerVars getInitialVars() {
		return this.model.getInitialVars();
	}

	public void initialiseFaceModel(final TrackedFace face) {
		this.model.initShape(face.redetectedBounds, face.shape, face.referenceShape);
	}

	public float getSearchAreaSize() {
		return this.searchAreaSize;
	}

	public void setSearchAreaSize(final float searchAreaSize) {
		this.searchAreaSize = searchAreaSize;
	}

	public Float[] getConnectionColour() {
		return this.connectionColour;
	}

	public void setConnectionColour(final Float[] connectionColour) {
		this.connectionColour = connectionColour;
	}

	public Float[] getPointColour() {
		return this.pointColour;
	}

	public void setPointColour(final Float[] pointColour) {
		this.pointColour = pointColour;
	}

	public Float[] getMeshColour() {
		return this.meshColour;
	}

	public void setMeshColour(final Float[] meshColour) {
		this.meshColour = meshColour;
	}

	public Float[] getBoundingBoxColour() {
		return this.boundingBoxColour;
	}

	public void setBoundingBoxColour(final Float[] boundingBoxColour) {
		this.boundingBoxColour = boundingBoxColour;
	}

	public Float[] getSearchAreaColour() {
		return this.searchAreaColour;
	}

	public void setSearchAreaColour(final Float[] searchAreaColour) {
		this.searchAreaColour = searchAreaColour;
	}

	public List<TrackedFace> getTrackedFaces() {
		return this.model.trackedFaces;
	}

	public List<Triangle> getTriangles(final TrackedFace face) {
		return CLMFaceTracker.getTriangles(face.shape, face.clm._visi[face.clm.getViewIdx()], this.triangles);
	}

	public static List<Triangle> getTriangles(final Matrix shape, final Matrix visi, final int[][] triangles) {
		final int n = shape.getRowDimension() / 2;
		final List<Triangle> tris = new ArrayList<Triangle>();

		for (int i = 0; i < triangles.length; i++) {
			if (visi != null
					&& (visi.get(triangles[i][0], 0) == 0 || visi.get(triangles[i][1], 0) == 0 || visi.get(
							triangles[i][2], 0) == 0)) {
				tris.add(null);
			} else {
				final Triangle t = new Triangle(new Point2dImpl((float) shape.get(triangles[i][0], 0),
						(float) shape.get(triangles[i][0] + n, 0)), new Point2dImpl((float) shape.get(triangles[i][1],
						0), (float) shape.get(triangles[i][1] + n, 0)), new Point2dImpl((float) shape.get(
						triangles[i][2], 0), (float) shape.get(triangles[i][2] + n, 0)));
				tris.add(t);
			}
		}

		return tris;
	}

	public void setRedetectEvery(final int nFrames) {
		this.fpd = nFrames;
	}

}
