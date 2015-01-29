package edu.hfut.fr.image.processing.face.recognition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.RestrictedAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * 人脸识别器的抽象函数
 *
 * @author jimbo
 */
public abstract class FaceRecogniser<FACE extends DetectedFace, PERSON> extends IncrementalAnnotator<FACE, PERSON>
		implements RestrictedAnnotator<FACE, PERSON>, ReadWriteableBinary {

	protected FaceRecogniser() {
	}

	/**
	 * 识别给定图像
	 */
	@Override
	public abstract List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict);

	/**
	 * 识别给定图像
	 */
	public ScoredAnnotation<PERSON> annotateBest(FACE object, Collection<PERSON> restrict) {
		final List<ScoredAnnotation<PERSON>> pot = annotate(object, restrict);

		if (pot == null || pot.size() == 0)
			return null;

		Collections.sort(pot);

		return pot.get(0);
	}

	/**
	 * 识别给定图像
	 */
	@Override
	public abstract List<ScoredAnnotation<PERSON>> annotate(FACE object);

	/**
	 * 识别给定图像
	 */
	public ScoredAnnotation<PERSON> annotateBest(FACE object) {
		final List<ScoredAnnotation<PERSON>> pot = annotate(object);

		if (pot == null || pot.size() == 0)
			return null;

		return pot.get(0);
	}

	/**
	 * getAnnotation方法的简单调用
	 */
	public Set<PERSON> listPeople() {
		return this.getAnnotations();
	}

}
