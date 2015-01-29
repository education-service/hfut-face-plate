package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.FImage2FloatFV;
import org.openimaj.io.IOUtils;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.alignment.ScalingAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 基于图像的人脸人脸特征
 *
 *@author wanggang
 */
public class FaceImageFeature implements FacialFeature, FeatureVectorProvider<FloatFV> {

	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<FaceImageFeature, T> {
		FaceAligner<T> aligner;

		public Extractor() {
			this(new ScalingAligner<T>());
		}

		public Extractor(FaceAligner<T> aligner) {
			this.aligner = aligner;
		}

		@Override
		public FaceImageFeature extractFeature(T face) {
			FImage faceImage = aligner.align(face);
			FloatFV feature = FImage2FloatFV.INSTANCE.extractFeature(faceImage);

			return new FaceImageFeature(feature);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}
	}

	private FloatFV feature;

	/**
	 * 构造函数
	 */
	public FaceImageFeature(FloatFV feature) {
		this.feature = feature;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		feature = new FloatFV();
		feature.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		feature.writeBinary(out);
	}

	@Override
	public FloatFV getFeatureVector() {
		return feature;
	}

}
