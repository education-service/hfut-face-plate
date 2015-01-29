package edu.hfut.fr.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.cache.GroupedListCache;
import org.openimaj.data.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 只在训练需要的时候进行人脸识别和人脸缓存
 *
 *@author jimbo
 */
abstract class LazyFaceRecogniser<FACE extends DetectedFace, PERSON, EXTRACTOR extends FeatureExtractor<?, FACE>>
		extends FaceRecogniser<FACE, PERSON> {

	EXTRACTOR extractor;
	FaceRecogniser<FACE, PERSON> internalRecogniser;
	GroupedListCache<PERSON, FACE> faceCache;
	boolean isInvalid = true;

	protected LazyFaceRecogniser() {
	}

	/**
	 * 构造函数
	 */
	public LazyFaceRecogniser(EXTRACTOR extractor, FaceRecogniser<FACE, PERSON> internalRecogniser) {
		this.extractor = extractor;
		this.internalRecogniser = internalRecogniser;
		faceCache = new InMemoryGroupedListCache<PERSON, FACE>();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final LazyFaceRecogniser<FACE, PERSON, EXTRACTOR> wrapper = IOUtils.read(in);
		this.extractor = wrapper.extractor;
		this.faceCache = wrapper.faceCache;
		this.internalRecogniser = wrapper.internalRecogniser;
		this.isInvalid = wrapper.isInvalid;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(this, out);
	}

	@Override
	public byte[] binaryHeader() {
		return "BFRec".getBytes();
	}

	@Override
	public void train(Annotated<FACE, PERSON> annotated) {
		faceCache.add(annotated.getAnnotations(), annotated.getObject());
		isInvalid = true;
	}

	@Override
	public void reset() {
		internalRecogniser.reset();
		faceCache.reset();
		isInvalid = true;
	}

	@Override
	public Set<PERSON> getAnnotations() {
		return faceCache.getDataset().getGroups();
	}

	protected abstract void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset);

	private void retrain() {
		if (isInvalid) {
			final GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset = faceCache.getDataset();
			beforeBatchTrain(dataset);
			internalRecogniser.train(dataset);
			isInvalid = false;
		}
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict) {
		retrain();
		return internalRecogniser.annotate(object, restrict);
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object) {
		retrain();
		return internalRecogniser.annotate(object);
	}

}
