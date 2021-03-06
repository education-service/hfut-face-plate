package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.io.wrappers.WriteableListBinary;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;
import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint;
import edu.hfut.fr.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 小块人脸特征
 *
 *@author wanggang
 */
public class FacePatchFeature implements FacialFeature, FeatureVectorProvider<FloatFV> {

	public static class Extractor implements FacialFeatureExtractor<FacePatchFeature, KEDetectedFace> {

		public Extractor() {
		}

		@Override
		public FacePatchFeature extractFeature(KEDetectedFace face) {
			FacePatchFeature f = new FacePatchFeature();
			f.initialise(face);
			return f;
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
		}

		@Override
		public byte[] binaryHeader() {
			return null;
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
		}
	}

	public static class DetectedFacePart extends FacialKeypoint implements ReadWriteableBinary {
		float[] featureVector;
		int featureRadius;

		public DetectedFacePart() {
			super();
		}

		public DetectedFacePart(FacialKeypointType type, Point2d position) {
			super(type, position);
		}

		public FImage getImage() {
			FImage image = new FImage(2 * featureRadius + 1, 2 * featureRadius + 1);

			for (int i = 0, rr = -featureRadius; rr <= featureRadius; rr++) {
				for (int cc = -featureRadius; cc <= featureRadius; cc++) {
					float r2 = rr * rr + cc * cc;

					if (r2 <= featureRadius * featureRadius) { // inside circle
						float value = featureVector[i++];

						image.pixels[rr + featureRadius][cc + featureRadius] = value < -3 ? 0 : value >= 3 ? 1
								: (3f + value) / 6f;
					}
				}
			}

			return image;
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			super.readBinary(in);

			int sz = in.readInt();
			if (sz < 0) {
				featureVector = null;
			} else {
				featureVector = new float[sz];
				for (int i = 0; i < sz; i++)
					featureVector[i] = in.readFloat();
			}

			featureRadius = in.readInt();
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			super.writeBinary(out);

			if (featureVector == null) {
				out.writeInt(-1);
			} else {
				out.writeInt(featureVector.length);
				for (float f : featureVector)
					out.writeFloat(f);
			}

			out.writeInt(featureRadius);
		}
	}

	final static int[][] VP = { { 0 }, // EYE_LEFT_LEFT,
			{ 1 }, // EYE_LEFT_RIGHT,
			{ 2 }, // EYE_RIGHT_LEFT,
			{ 3 }, // EYE_RIGHT_RIGHT,
			{ 4 }, // NOSE_LEFT,
			{ 5 }, // NOSE_MIDDLE,
			{ 6 }, // NOSE_RIGHT,
			{ 7 }, // MOUTH_LEFT,
			{ 8 }, // MOUTH_RIGHT,
			{ 0, 1 }, // EYE_LEFT_CENTER,
			{ 2, 3 }, // EYE_RIGHT_CENTER,
			{ 1, 2 }, // NOSE_BRIDGE,
			{ 7, 8 } }; // MOUTH_CENTER

	protected FloatFV featureVector;

	protected int radius = 7;

	protected float scl = 1;

	protected List<DetectedFacePart> faceParts = new ArrayList<DetectedFacePart>();

	public FacePatchFeature() {
	}

	protected void initialise(KEDetectedFace face) {
		extractFeatures(face);
		this.featureVector = createFeatureVector();
	}

	protected FloatFV createFeatureVector() {
		int length = faceParts.get(0).featureVector.length;
		FloatFV fv = new FloatFV(faceParts.size() * length);

		for (int i = 0; i < faceParts.size(); i++) {
			System.arraycopy(faceParts.get(i).featureVector, 0, fv.values, i * length, length);
		}

		return fv;
	}

	protected void extractFeatures(KEDetectedFace face) {
		Matrix T0 = AffineAligner.estimateAffineTransform(face);
		Matrix T = T0.copy();
		FImage J = FKEFaceDetector.pyramidResize(face.getFacePatch(), T);
		FacialKeypoint[] pts = face.getKeypoints();
		faceParts.clear();

		float pyrScale = (float) (T0.get(0, 2) / T.get(0, 2));

		Point2dImpl[] P0 = new Point2dImpl[VP.length];
		for (int j = 0; j < P0.length; j++) {
			int[] vp = VP[j];
			int vp0 = vp[0];

			P0[j] = new Point2dImpl(0, 0);
			if (vp.length == 1) {
				P0[j].x = pts[vp0].position.x / pyrScale;
				P0[j].y = pts[vp0].position.y / pyrScale;
			} else {
				int vp1 = vp[1];
				P0[j].x = ((pts[vp0].position.x + pts[vp1].position.x) / 2.0f) / pyrScale;
				P0[j].y = ((pts[vp0].position.y + pts[vp1].position.y) / 2.0f) / pyrScale;
			}
		}

		List<Point2dImpl> transformed = new ArrayList<Point2dImpl>();
		List<Pixel> nontransformed = new ArrayList<Pixel>();
		for (int rr = -radius; rr <= radius; rr++) {
			for (int cc = -radius; cc <= radius; cc++) {
				float r2 = rr * rr + cc * cc;
				if (r2 <= radius * radius) { // inside circle
					float px = (float) (cc * scl * T.get(0, 0) + rr * scl * T.get(0, 1));
					float py = (float) (cc * scl * T.get(1, 0) + rr * scl * T.get(1, 1));

					transformed.add(new Point2dImpl(px, py));
					nontransformed.add(new Pixel(cc, rr));
				}
			}
		}

		for (int j = 0; j < VP.length; j++) {
			DetectedFacePart pd = new DetectedFacePart(FacialKeypointType.valueOf(j), new Point2dImpl(P0[j].x
					* pyrScale, P0[j].y * pyrScale));
			faceParts.add(pd);
			pd.featureVector = new float[transformed.size()];

			int n = 0;
			float mean = 0;
			float m2 = 0;

			for (int i = 0; i < transformed.size(); i++) {
				Point2dImpl XYt = transformed.get(i);

				double xt = XYt.x + P0[j].x;
				double yt = XYt.y + P0[j].y;
				float val = J.getPixelInterp(xt, yt);

				pd.featureVector[i] = val;

				n++;
				float delta = val - mean;
				mean = mean + delta / n;
				m2 = m2 + delta * (val - mean);
			}

			float std = (float) Math.sqrt(m2 / (n - 1));
			if (std <= 0)
				std = 1;

			for (int i = 0; i < transformed.size(); i++) {
				pd.featureVector[i] = (pd.featureVector[i] - mean) / std;
			}
		}
	}

	@Override
	public FloatFV getFeatureVector() {
		return this.featureVector;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		featureVector = new FloatFV();
		featureVector.readBinary(in);

		radius = in.readInt();
		scl = in.readFloat();

		new ReadableListBinary<DetectedFacePart>(faceParts) {
			@Override
			protected DetectedFacePart readValue(DataInput in) throws IOException {
				DetectedFacePart v = new DetectedFacePart();
				v.readBinary(in);
				return v;
			}
		}.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		featureVector.writeBinary(out);
		out.writeInt(radius);
		out.writeFloat(scl);

		new WriteableListBinary<DetectedFacePart>(faceParts) {
			@Override
			protected void writeValue(DetectedFacePart v, DataOutput out) throws IOException {
				v.writeBinary(out);
			}
		}.writeBinary(out);
	}

}
