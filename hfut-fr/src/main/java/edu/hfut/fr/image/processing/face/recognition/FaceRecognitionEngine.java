package edu.hfut.fr.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

import edu.hfut.fr.image.processing.face.detection.DatasetFaceDetector;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;

/**
 * 人脸识别引擎
 *
 * @author jimbo
 */
public class FaceRecognitionEngine<FACE extends DetectedFace, PERSON> implements ReadWriteableBinary {

	private static final Logger logger = Logger.getLogger(FaceRecognitionEngine.class);

	protected FaceDetector<FACE, FImage> detector;
	protected FaceRecogniser<FACE, PERSON> recogniser;

	protected FaceRecognitionEngine() {
	}

	/**
	 * 构造函数
	 */
	public FaceRecognitionEngine(final FaceDetector<FACE, FImage> detector,
			final FaceRecogniser<FACE, PERSON> recogniser) {
		this.detector = detector;
		this.recogniser = recogniser;
	}

	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON> FaceRecognitionEngine<FACE, PERSON> create(
			final FaceDetector<FACE, FImage> detector, final FaceRecogniser<FACE, PERSON> recogniser) {
		return new FaceRecognitionEngine<FACE, PERSON>(detector, recogniser);
	}

	public FaceDetector<FACE, FImage> getDetector() {
		return this.detector;
	}

	public FaceRecogniser<FACE, PERSON> getRecogniser() {
		return this.recogniser;
	}

	/**
	 * 保存识别器
	 */
	public void save(final File file) throws IOException {
		IOUtils.writeBinaryFull(file, this);
	}

	/**
	 * 获取识别器
	 */
	public static <O extends DetectedFace, P> FaceRecognitionEngine<O, P> load(final File file) throws IOException {
		final FaceRecognitionEngine<O, P> engine = IOUtils.read(file);

		return engine;
	}

	/**
	 * 训练识别器
	 */
	public void train(final GroupedDataset<PERSON, ListDataset<FImage>, FImage> dataset) {
		final GroupedDataset<PERSON, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset,
				this.detector);
		this.recogniser.train(faceDataset);
	}

	/**
	 * 使用单独的对象进行训练
	 */
	public FACE train(final PERSON person, final FImage image) {
		final List<FACE> faces = this.detector.detectFaces(image);

		if (faces == null || faces.size() == 0) {
			FaceRecognitionEngine.logger.warn("no face detected");
			return null;
		} else if (faces.size() == 1) {
			this.recogniser.train(AnnotatedObject.create(faces.get(0), person));
			return faces.get(0);
		} else {
			FaceRecognitionEngine.logger.warn("More than one face found. Choosing biggest.");

			final FACE face = DatasetFaceDetector.getBiggest(faces);
			this.recogniser.train(AnnotatedObject.create(face, person));
			return face;
		}
	}

	/**
	 * 训练识别器
	 */
	public FACE train(final FACE face, final PERSON person) {
		this.recogniser.train(AnnotatedObject.create(face, person));
		return face;
	}

	/**
	 * 检测并识别给定图像中的人脸
	 */
	public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df)));
		}

		return results;
	}

	/**
	 * 检测并识别图像中出现的人脸
	 *
	 */
	public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser.annotateBest(df)));
		}

		return results;
	}

	/**
	 * 检测并识别图像中出现的人脸
	 *
	 */
	public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image,
			final Set<PERSON> restrict) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df,
					restrict)));
		}

		return results;
	}

	/**
	 * 检测并识别图像中出现的人脸
	 */
	public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image,
			final Set<PERSON> restrict) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser.annotateBest(df,
					restrict)));
		}

		return results;
	}

	@Override
	public void readBinary(final DataInput in) throws IOException {
		final String detectorClass = in.readUTF();
		this.detector = IOUtils.newInstance(detectorClass);
		this.detector.readBinary(in);

		final String recogniserClass = in.readUTF();
		this.recogniser = IOUtils.newInstance(recogniserClass);
		this.recogniser.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "FaRE".getBytes();
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		out.writeUTF(this.detector.getClass().getName());
		this.detector.writeBinary(out);

		out.writeUTF(this.recogniser.getClass().getName());
		this.recogniser.writeBinary(out);
	}

}
