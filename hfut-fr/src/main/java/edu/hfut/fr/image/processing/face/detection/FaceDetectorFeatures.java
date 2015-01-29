package edu.hfut.fr.image.processing.face.detection;

import java.util.List;
import java.util.Set;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.MultidimensionalIntFV;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * 从检测人脸提取的特征列表
 *
 *@author wanggang
 */
public enum FaceDetectorFeatures {

	COUNT {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			return new MultidimensionalIntFV(new int[] { faces.size() }, 1);
		}
	},
	BLOBS {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				final Set<Pixel> pixels = getConnectedComponent(df).pixels;

				final int[] fv = new int[pixels.size() * 2];

				int j = 0;
				for (final Pixel p : pixels) {
					fv[j++] = p.x;
					fv[j++] = p.y;
				}

				fvs[i++] = fv;
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	BOX {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				fvs[i++] = new int[] { (int) df.getBounds().x, (int) df.getBounds().y, (int) df.getBounds().width,
						(int) df.getBounds().height };
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	ORIBOX {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final int[][] fvs = new int[faces.size()][];
			int i = 0;

			for (final DetectedFace df : faces) {
				final Polygon p = getConnectedComponent(df).calculateOrientatedBoundingBox().asPolygon();

				final int[] fv = new int[p.getVertices().size() * 2];

				for (int j = 0, k = 0; j < fv.length; j += 2, k++) {
					fv[j] = (int) p.getVertices().get(k).getX();
					fv[j + 1] = (int) p.getVertices().get(k).getY();
				}

				fvs[i++] = fv;
			}

			return new MultidimensionalIntFV(fvs);
		}
	},
	AREA {
		@Override
		public <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img) {
			final double[] fv = new double[faces.size()];
			final double area = img.getWidth() * img.getHeight();
			int i = 0;

			for (final DetectedFace df : faces) {
				fv[i++] = getConnectedComponent(df).calculateArea() / area;
			}

			return new DoubleFV(fv);
		}
	};

	protected ConnectedComponent getConnectedComponent(DetectedFace df) {
		if (df instanceof CCDetectedFace) {
			return ((CCDetectedFace) df).connectedComponent;
		} else {
			return new ConnectedComponent(df.getBounds());
		}
	}

	/**
	 * 检测特征向量抽象方法
	 */
	public abstract <T extends Image<?, T>> FeatureVector getFeatureVector(List<? extends DetectedFace> faces, T img);

}
