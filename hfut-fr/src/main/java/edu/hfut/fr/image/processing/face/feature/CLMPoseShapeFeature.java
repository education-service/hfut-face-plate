package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;

import edu.hfut.fr.image.processing.face.detection.CLMDetectedFace;

/**
 * 描述人脸形态和线条的特征向量
 *
 * @author wanggang
 */
public class CLMPoseShapeFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {

	public static class Extractor implements FacialFeatureExtractor<CLMPoseShapeFeature, CLMDetectedFace> {
		@Override
		public CLMPoseShapeFeature extractFeature(CLMDetectedFace face) {
			return new CLMPoseShapeFeature(face.getPoseShapeParameters());
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
		}
	}

	private DoubleFV fv;

	protected CLMPoseShapeFeature() {
		this(null);
	}

	/**
	 * 构造函数
	 */
	public CLMPoseShapeFeature(DoubleFV fv) {
		this.fv = fv;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		fv = new DoubleFV();
		fv.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		fv.writeBinary(out);
	}

	@Override
	public DoubleFV getFeatureVector() {
		return fv;
	}

}
