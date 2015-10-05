package edu.hfut.fr.no.demo;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 采用Affineface矫正人脸,矫正效果最好,必须使用KED检测数据
 *
 * @author wanghao
 */
public class AffineFaceAligner implements Display {

	/**
	 * 构造函数,采用默认FKE识别脸部方法方法
	 */
	public AffineFaceAligner() {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector();
		AffineAligner faceAligner = new AffineAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			DisplayUtilities.display(faceFA);
		}
	}

	public AffineFaceAligner(String file) {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector(file);
		AffineAligner faceAligner = new AffineAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			DisplayUtilities.display(faceFA);
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
