package edu.hfut.fr.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;

import Jama.Matrix;

import com.jsaragih.IO;
import com.jsaragih.Tracker;

import edu.hfut.fr.image.processing.face.detection.CLMDetectedFace;
import edu.hfut.fr.image.processing.face.tracking.clm.MultiTracker;

/**
 * 为绘制边框做准备
 *
 * @author jimbo
 */
public class CLMDetectedFaceRenderer implements DetectedFaceRenderer<CLMDetectedFace> {

	private int[][] triangles;
	private boolean drawTriangles = true;
	private boolean drawConnections = true;
	private boolean drawPoints = true;
	private boolean drawBounds = true;
	private int[][] connections;
	private float scale = 1f;
	private Float[] boundingBoxColour = RGBColour.RED;
	private Float[] pointColour = RGBColour.BLUE;
	private Float[] meshColour = RGBColour.GREEN;
	private Float[] connectionColour = RGBColour.YELLOW;
	private int thickness;

	public CLMDetectedFaceRenderer() {
		this.triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
		connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
	}

	@Override
	public void drawDetectedFace(MBFImage image, int thickness, CLMDetectedFace f) {
		this.thickness = thickness;
		drawFaceModel(image, f.getShapeMatrix(), f.getVisibility(), f.getBounds());
	}

	public void drawDetectedFace(MBFImage image, MultiTracker.TrackedFace f) {
		drawFaceModel(image, f.shape, f.clm._visi[f.clm.getViewIdx()], f.lastMatchBounds);
	}

	private void drawFaceModel(MBFImage image, Matrix shape, Matrix visi, Rectangle bounds) {
		final int n = shape.getRowDimension() / 2;

		if (drawBounds && bounds != null)
			image.createRenderer().drawShape(bounds, boundingBoxColour);

		if (drawTriangles) {
			for (int i = 0; i < triangles.length; i++) {
				if (visi.get(triangles[i][0], 0) == 0 || visi.get(triangles[i][1], 0) == 0
						|| visi.get(triangles[i][2], 0) == 0)
					continue;

				final Triangle t = new Triangle(new Point2dImpl((float) (shape.get(triangles[i][0], 0) + bounds.x)
						/ scale, (float) (shape.get(triangles[i][0] + n, 0) + bounds.y) / scale), new Point2dImpl(
						(float) (shape.get(triangles[i][1], 0) + bounds.x) / scale, (float) (shape.get(triangles[i][1]
								+ n, 0) + bounds.y)
								/ scale), new Point2dImpl((float) (shape.get(triangles[i][2], 0) + bounds.x) / scale,
						(float) (shape.get(triangles[i][2] + n, 0) + bounds.y) / scale));
				image.drawShape(t, thickness, meshColour);
			}
		}

		if (drawConnections) {
			for (int i = 0; i < connections[0].length; i++) {
				if (visi.get(connections[0][i], 0) == 0 || visi.get(connections[1][i], 0) == 0)
					continue;

				image.drawLine(
						new Point2dImpl((float) (shape.get(connections[0][i], 0) + bounds.x) / scale, (float) (shape
								.get(connections[0][i] + n, 0) + bounds.y) / scale),
						new Point2dImpl((float) (shape.get(connections[1][i], 0) + bounds.x) / scale, (float) (shape
								.get(connections[1][i] + n, 0) + bounds.y) / scale), thickness, connectionColour);
			}
		}

		if (drawPoints) {
			for (int i = 0; i < n; i++) {
				if (visi.get(i, 0) == 0)
					continue;

				image.drawPoint(
						new Point2dImpl(((float) shape.get(i, 0) + bounds.x) / scale,
								((float) shape.get(i + n, 0) + bounds.y) / scale), pointColour, thickness);
			}
		}
	}

	public static void render(MBFImage mbf, int thickness, CLMDetectedFace face) {
		final CLMDetectedFaceRenderer render = new CLMDetectedFaceRenderer();
		render.drawDetectedFace(mbf, thickness, face);
	}

}
