package edu.hfut.fr.no.demo;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.alignment.RotateScaleAligner;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 通过旋转矫正脸部
 *
 * @author wanghao
 */
public class RotateScaleFaceAligner implements Display {

	/**
	 * 构造函数,采用默认FKE识别脸部方法方法
	 */
	public RotateScaleFaceAligner() {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector();
		RotateScaleAligner faceAligner = new RotateScaleAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			displayF(faceFA);
		}
	}

	public RotateScaleFaceAligner(String file) {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector(file);
		RotateScaleAligner faceAligner = new RotateScaleAligner();
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
