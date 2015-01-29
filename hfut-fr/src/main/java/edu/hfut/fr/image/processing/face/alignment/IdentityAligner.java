package edu.hfut.fr.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 确认矫正
 *
 *@author wangggang
 */
public class IdentityAligner<T extends DetectedFace> implements FaceAligner<T> {

	@Override
	public FImage align(T face) {
		return face.getFacePatch();
	}

	@Override
	public FImage getMask() {
		return null;
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

	@Override
	public String toString() {
		return "IdentityAligner";
	}

}
