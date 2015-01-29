package edu.hfut.fr.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;

/**
 * 确认面部检测器
 *
 * @author wanggang
 */
public class IdentityFaceDetector<IMAGE extends Image<?, IMAGE>> implements FaceDetector<DetectedFace, IMAGE> {

	@Override
	public void readBinary(DataInput in) throws IOException {
	}

	@Override
	public byte[] binaryHeader() {
		return IdentityFaceDetector.class.getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// Do nothing
	}

	@Override
	public List<DetectedFace> detectFaces(IMAGE image) {
		DetectedFace face = null;
		final Object oimage = image;

		if (oimage instanceof FImage)
			face = new DetectedFace(image.getBounds(), ((FImage) (oimage)), 1);
		else if (oimage instanceof MBFImage)
			face = new DetectedFace(image.getBounds(), ((MBFImage) (oimage)).flatten(), 1);
		else
			throw new RuntimeException("unsupported image type");

		final List<DetectedFace> faces = new ArrayList<DetectedFace>(1);
		faces.add(face);

		return faces;
	}

	@Override
	public String toString() {
		return "Identity Face Detector";
	}

}
