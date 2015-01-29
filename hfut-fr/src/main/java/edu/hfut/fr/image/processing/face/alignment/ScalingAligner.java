package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;

/**
 * 缩放矫正
 *
 *@author wanggang
 */
public class ScalingAligner<T extends DetectedFace> implements FaceAligner<T> {

	private int width;
	private int height;

	/**
	 * 默认构造
	 */
	public ScalingAligner() {
		this(100, 100);
	}

	public ScalingAligner(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public FImage align(DetectedFace face) {
		return ResizeProcessor.resample(face.getFacePatch(), width, height);
	}

	@Override
	public FImage getMask() {
		return null;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		width = in.readInt();
		height = in.readInt();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
	}

}
