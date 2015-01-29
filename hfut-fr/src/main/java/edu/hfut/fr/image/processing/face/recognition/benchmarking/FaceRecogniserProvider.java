package edu.hfut.fr.image.processing.face.recognition.benchmarking;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.recognition.FaceRecogniser;

/**
 * 人脸识别器
 *
 * @author jimbo
 */
public interface FaceRecogniserProvider<FACE extends DetectedFace, PERSON> {

	/**
	 * 创建一个人脸识别器
	 */
	public abstract FaceRecogniser<FACE, PERSON> create(
			GroupedDataset<PERSON, ? extends ListDataset<FACE>, FACE> dataset);

}
