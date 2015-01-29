package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;

import edu.hfut.fr.image.processing.face.detection.CLMDetectedFace;

/**
 * 3D图像 特征向量描述
 *
 *@author wanggang
 */
public class CLMPoseFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {

	public static class Extractor implements FacialFeatureExtractor<CLMPoseFeature, CLMDetectedFace> {
		@Override
		public CLMPoseFeature extractFeature(CLMDetectedFace face) {
			return new CLMPoseFeature(face.getPoseParameters());
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

	protected CLMPoseFeature() {
		this(null);
	}

	/**
	 * 构造函数
	 */
	public CLMPoseFeature(DoubleFV fv) {
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
