package edu.hfut.fr.driver.run;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.alignment.MeshWarpAligner;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

public class MeshWarpFaceAligner implements Display {

	/**
	 * 构造函数,采用默认FKE识别脸部方法方法
	 */
	public MeshWarpFaceAligner() {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector();
		MeshWarpAligner faceAligner = new MeshWarpAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			displayF(faceFA);
		}
	}

	public MeshWarpFaceAligner(String file) {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector(file);
		MeshWarpAligner faceAligner = new MeshWarpAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			displayF(faceFA);
		}
	}

	@Override
	public void displayMBF(MBFImage image) {
		DisplayUtilities.display(image);

	}

	@Override
	public void displayF(FImage image) {
		DisplayUtilities.display(image);
	}

}
