package edu.hfut.fr.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.RestrictedAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 人脸识别器的一个上层类
 *
 * @author jimbo
 */
public class AnnotatorFaceRecogniser<FACE extends DetectedFace, PERSON> extends FaceRecogniser<FACE, PERSON> {

	protected IncrementalAnnotator<FACE, PERSON> annotator;

	protected AnnotatorFaceRecogniser() {
	}

	/**
	 * 构造函数
	 */
	public AnnotatorFaceRecogniser(IncrementalAnnotator<FACE, PERSON> annotator) {
		this.annotator = annotator;
	}

	/**
	 * 生成一个申明器的静态方法
	 */
	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON> AnnotatorFaceRecogniser<FACE, PERSON> create(
			IncrementalAnnotator<FACE, PERSON> annotator) {
		return new AnnotatorFaceRecogniser<FACE, PERSON>(annotator);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		annotator = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "FREC".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(annotator, out);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict) {
		if (annotator instanceof RestrictedAnnotator) {
			return ((RestrictedAnnotator<FACE, PERSON>) annotator).annotate(object, restrict);
		}

		final List<ScoredAnnotation<PERSON>> pot = annotator.annotate(object);

		if (pot == null || pot.size() == 0)
			return null;

		final List<ScoredAnnotation<PERSON>> toKeep = new ArrayList<ScoredAnnotation<PERSON>>();

		for (final ScoredAnnotation<PERSON> p : pot) {
			if (restrict.contains(p.annotation))
				toKeep.add(p);
		}

		return toKeep;
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object) {
		return annotator.annotate(object);
	}

	@Override
	public void train(Annotated<FACE, PERSON> annotedImage) {
		annotator.train(annotedImage);
	}

	@Override
	public void train(Iterable<? extends Annotated<FACE, PERSON>> data) {
		annotator.train(data);
	}

	@Override
	public Set<PERSON> getAnnotations() {
		return annotator.getAnnotations();
	}

	@Override
	public void reset() {
		annotator.reset();
	}

	@Override
	public String toString() {
		return String.format("AnnotatorFaceRecogniser[recogniser=%s]", this.annotator);
	}

}
