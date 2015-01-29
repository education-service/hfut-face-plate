package edu.hfut.fr.image.processing.face.recognition;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparator;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.basic.KNNAnnotator;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.feature.FisherFaceFeature.Extractor;

/**
 * 基于Fisherfaces的人脸识别器
 *
 * @author jimbo
 */
public class FisherFaceRecogniser<FACE extends DetectedFace, PERSON> extends
		LazyFaceRecogniser<FACE, PERSON, Extractor<FACE>> {

	protected FisherFaceRecogniser() {
	}

	/**
	 * 构造函数
	 */
	public FisherFaceRecogniser(Extractor<FACE> extractor, FaceRecogniser<FACE, PERSON> internalRecogniser) {
		super(extractor, internalRecogniser);
	}

	/**
	 * 构造函数
	 */
	public FisherFaceRecogniser(Extractor<FACE> extractor, IncrementalAnnotator<FACE, PERSON> annotator) {
		this(extractor, AnnotatorFaceRecogniser.create(annotator));
	}

	/**
	 * 分类器
	 */
	public static <FACE extends DetectedFace, PERSON> FisherFaceRecogniser<FACE, PERSON> create(int numComponents,
			FaceAligner<FACE> aligner, int k, DoubleFVComparator compar) {
		final Extractor<FACE> extractor = new Extractor<FACE>(numComponents, aligner);
		final FVProviderExtractor<DoubleFV, FACE> extractor2 = FVProviderExtractor.create(extractor);

		final KNNAnnotator<FACE, PERSON, DoubleFV> knn = KNNAnnotator.create(extractor2, compar, k);

		return new FisherFaceRecogniser<FACE, PERSON>(extractor, knn);
	}

	/**
	 * 基于KNN的分类器
	 */
	public static <FACE extends DetectedFace, PERSON> FisherFaceRecogniser<FACE, PERSON> create(int numComponents,
			FaceAligner<FACE> aligner, int k, DoubleFVComparator compar, float threshold) {
		final Extractor<FACE> extractor = new Extractor<FACE>(numComponents, aligner);
		final FVProviderExtractor<DoubleFV, FACE> extractor2 = FVProviderExtractor.create(extractor);

		final KNNAnnotator<FACE, PERSON, DoubleFV> knn = KNNAnnotator.create(extractor2, compar, k, threshold);

		return new FisherFaceRecogniser<FACE, PERSON>(extractor, knn);
	}

	@Override
	protected void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset) {
		extractor.train(dataset);
	}

	@Override
	public String toString() {
		return String.format("FisherFaceRecogniser[extractor=%s; recogniser=%s]", this.extractor,
				this.internalRecogniser);
	}

}
