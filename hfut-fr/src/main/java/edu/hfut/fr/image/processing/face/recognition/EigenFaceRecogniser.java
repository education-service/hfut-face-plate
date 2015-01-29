package edu.hfut.fr.image.processing.face.recognition;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparator;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.basic.KNNAnnotator;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.feature.EigenFaceFeature.Extractor;

/**
 * 基于Eigenfaces实现人脸识别.
 *
 * @author jimbo
 */
public class EigenFaceRecogniser<FACE extends DetectedFace, PERSON> extends
		LazyFaceRecogniser<FACE, PERSON, Extractor<FACE>> {

	protected EigenFaceRecogniser() {
	}

	/**
	 * 构造函数
	 */
	public EigenFaceRecogniser(Extractor<FACE> extractor, FaceRecogniser<FACE, PERSON> internalRecogniser) {
		super(extractor, internalRecogniser);
	}

	/**
	 * 构造函数
	 */
	public EigenFaceRecogniser(Extractor<FACE> extractor, IncrementalAnnotator<FACE, PERSON> annotator) {
		this(extractor, AnnotatorFaceRecogniser.create(annotator));
	}

	/**
	 * 生成识别器
	 */
	public static <FACE extends DetectedFace, PERSON> EigenFaceRecogniser<FACE, PERSON> create(int numComponents,
			FaceAligner<FACE> aligner, int k, DoubleFVComparator compar, float threshold) {
		final Extractor<FACE> extractor = new Extractor<FACE>(numComponents, aligner);
		final FVProviderExtractor<DoubleFV, FACE> extractor2 = FVProviderExtractor.create(extractor);

		final KNNAnnotator<FACE, PERSON, DoubleFV> knn = KNNAnnotator.create(extractor2, compar, k, threshold);

		return new EigenFaceRecogniser<FACE, PERSON>(extractor, knn);
	}

	/**
	 * 生成识别器
	 */
	public static <FACE extends DetectedFace, PERSON> EigenFaceRecogniser<FACE, PERSON> create(int numComponents,
			FaceAligner<FACE> aligner, int k, DoubleFVComparator compar) {
		final Extractor<FACE> extractor = new Extractor<FACE>(numComponents, aligner);
		final FVProviderExtractor<DoubleFV, FACE> extractor2 = FVProviderExtractor.create(extractor);

		final KNNAnnotator<FACE, PERSON, DoubleFV> knn = KNNAnnotator.create(extractor2, compar, k);

		return new EigenFaceRecogniser<FACE, PERSON>(extractor, knn);
	}

	@Override
	protected void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset) {
		extractor.train(DatasetAdaptors.asList(dataset));
	}

	@Override
	public String toString() {
		return String.format("EigenFaceRecogniser[extractor=%s; recogniser=%s]", this.extractor,
				this.internalRecogniser);
	}

}
