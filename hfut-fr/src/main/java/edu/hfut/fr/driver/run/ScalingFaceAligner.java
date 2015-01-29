package edu.hfut.fr.driver.run;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.alignment.ScalingAligner;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 维度矫正,要设置参数
 *
 * @author wanghao
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ScalingFaceAligner implements Display {

	/**
	 * 构造函数,采用默认FKE识别脸部方法方法
	 */
	public ScalingFaceAligner() {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector();
		ScalingAligner<KEDetectedFace> faceAligner = new ScalingAligner();
		for (KEDetectedFace face : fkeFacePointDetector.getFaces()) {
			FImage faceFA = faceAligner.align(face);
			displayF(faceFA);
		}
	}

	public ScalingFaceAligner(String file) {
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector(file);
		ScalingAligner<KEDetectedFace> faceAligner = new ScalingAligner();
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
