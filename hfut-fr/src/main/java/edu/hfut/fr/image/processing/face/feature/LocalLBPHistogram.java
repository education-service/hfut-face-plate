package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.binarypattern.ExtendedLocalBinaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.io.IOUtils;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.alignment.IdentityAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 局部LBPH特征
 *
 * @author wanggang
 */
public class LocalLBPHistogram implements FacialFeature, FeatureVectorProvider<FloatFV> {

	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<LocalLBPHistogram, T> {
		FaceAligner<T> aligner;
		int blocksX = 25;
		int blocksY = 25;
		int samples = 8;
		int radius = 1;

		public Extractor() {
			this.aligner = new IdentityAligner<T>();
		}

		public Extractor(FaceAligner<T> aligner) {
			this.aligner = aligner;
		}

		public Extractor(FaceAligner<T> aligner, int blocksX, int blocksY, int samples, int radius) {
			this.aligner = aligner;
			this.blocksX = blocksX;
			this.blocksY = blocksY;
			this.samples = samples;
			this.radius = radius;
		}

		@Override
		public LocalLBPHistogram extractFeature(T detectedFace) {
			final LocalLBPHistogram f = new LocalLBPHistogram();

			final FImage face = aligner.align(detectedFace);
			final FImage mask = aligner.getMask();

			f.initialise(face, mask, blocksX, blocksY, samples, radius);

			return f;
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			final String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);

			blocksX = in.readInt();
			blocksY = in.readInt();
			radius = in.readInt();
			samples = in.readInt();
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);

			out.writeInt(blocksX);
			out.writeInt(blocksY);
			out.writeInt(radius);
			out.writeInt(samples);
		}

		@Override
		public String toString() {
			return String.format("LocalLBPHistogram.Factory[blocksX=%d,blocksY=%d,samples=%d,radius=%d]", blocksX,
					blocksY, samples, radius);
		}
	}

	float[][][] histograms;
	transient FloatFV featureVector;

	protected void initialise(FImage face, FImage mask, int blocksX, int blocksY, int samples, int radius) {
		final int[][] pattern = ExtendedLocalBinaryPattern.calculateLBP(face, radius, samples);
		final boolean[][][] maps = UniformBinaryPattern.extractPatternMaps(pattern, samples);

		final int bx = face.width / blocksX;
		final int by = face.height / blocksY;
		histograms = new float[blocksY][blocksX][maps.length];

		// 建立直方图
		for (int p = 0; p < maps.length; p++) {
			for (int y = 0; y < blocksY; y++) {
				for (int x = 0; x < blocksX; x++) {

					for (int j = 0; j < by; j++) {
						for (int i = 0; i < bx; i++) {
							if (maps[p][y * by + j][x * bx + i])
								histograms[y][x][p]++;
						}
					}
				}
			}
		}

		// 标准化
		for (int y = 0; y < blocksY; y++) {
			for (int x = 0; x < blocksX; x++) {
				float count = 0;
				for (int p = 0; p < maps.length; p++) {
					count += histograms[y][x][p];
				}
				for (int p = 0; p < maps.length; p++) {
					histograms[y][x][p] /= count;
				}
			}
		}

		updateFeatureVector();
	}

	protected void updateFeatureVector() {
		featureVector = new FloatFV(histograms.length * histograms[0].length * histograms[0][0].length);

		int i = 0;
		for (int y = 0; y < histograms.length; y++) {
			for (int x = 0; x < histograms[0].length; x++) {
				for (int p = 0; p < histograms[0][0].length; p++) {
					featureVector.values[i] = histograms[y][x][p];
					i++;
				}
			}
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "LBPH".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final int by = in.readInt();
		final int bx = in.readInt();
		final int p = in.readInt();

		histograms = new float[by][bx][p];

		for (int j = 0; j < by; j++) {
			for (int i = 0; i < bx; i++) {
				for (int k = 0; k < p; k++) {
					histograms[j][i][k] = in.readFloat();
				}
			}
		}
		updateFeatureVector();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(histograms.length);
		out.writeInt(histograms[0].length);
		out.writeInt(histograms[0][0].length);

		for (final float[][] hist1 : histograms) {
			for (final float[] hist2 : hist1) {
				for (final float h : hist2) {
					out.writeFloat(h);
				}
			}
		}
	}

	@Override
	public FloatFV getFeatureVector() {
		if (featureVector == null)
			updateFeatureVector();

		return featureVector;
	}

}
