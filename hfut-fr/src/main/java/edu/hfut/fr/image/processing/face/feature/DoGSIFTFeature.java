package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.shape.Rectangle;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 人脸的 DoG-SIFT 特征
 *
 *@author wanggang
 */
public class DoGSIFTFeature implements FacialFeature {

	public static class Extractor implements FacialFeatureExtractor<DoGSIFTFeature, DetectedFace> {

		public Extractor() {

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

		@Override
		public DoGSIFTFeature extractFeature(DetectedFace face) {
			DoGSIFTFeature feature = new DoGSIFTFeature();
			feature.initialise(face);
			return feature;
		}
	}

	protected LocalFeatureList<Keypoint> keys;
	protected Rectangle bounds;

	protected void initialise(DetectedFace face) {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		keys = engine.findFeatures(face.getFacePatch());
		bounds = face.getFacePatch().getBounds();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		keys = MemoryLocalFeatureList.readNoHeader(in, Keypoint.class);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		keys.writeBinary(out);
	}

	public LocalFeatureList<Keypoint> getKeys() {
		return keys;
	}

	public Rectangle getBounds() {
		return bounds;
	}

}
