package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.model.EigenImages;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.training.BatchTrainer;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * EigenFaces特征
 *@author wangang
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Turk, M.A.", "Pentland, A.P." }, title = "Face recognition using eigenfaces", year = "1991", booktitle = "Computer Vision and Pattern Recognition, 1991. Proceedings CVPR '91., IEEE Computer Society Conference on", pages = {
		"586 ", "591" }, month = "jun", number = "", volume = "", customData = {
		"keywords",
		"eigenfaces;eigenvectors;face images;face recognition system;face space;feature space;human faces;two-dimensional recognition;unsupervised learning;computerised pattern recognition;eigenvalues and eigenfunctions;",
		"doi", "10.1109/CVPR.1991.139758" })
public class EigenFaceFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {

	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<EigenFaceFeature, T>,
			BatchTrainer<T> {
		EigenImages eigen = null;
		FaceAligner<T> aligner = null;

		public Extractor(int numComponents, FaceAligner<T> aligner) {
			this(new EigenImages(numComponents), aligner);
		}

		public Extractor(EigenImages basis, FaceAligner<T> aligner) {
			this.eigen = basis;
			this.aligner = aligner;
		}

		@Override
		public EigenFaceFeature extractFeature(T face) {
			final FImage patch = aligner.align(face);

			final DoubleFV fv = eigen.extractFeature(patch);

			return new EigenFaceFeature(fv);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			eigen.readBinary(in);

			final String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			eigen.writeBinary(out);

			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public void train(final List<? extends T> data) {
			final List<FImage> patches = new AbstractList<FImage>() {

				@Override
				public FImage get(int index) {
					return aligner.align(data.get(index));
				}

				@Override
				public int size() {
					return data.size();
				}

			};

			eigen.train(patches);
		}

		/**
		 * 根据数据集进行训练
		 */
		public void train(final Dataset<? extends T> data) {
			train(DatasetAdaptors.asList(data));
		}

		@Override
		public String toString() {
			return String.format("EigenFaceFeature.Extractor[aligner=%s]", this.aligner);
		}
	}

	private DoubleFV fv;

	protected EigenFaceFeature() {
		this(null);
	}

	/**
	 * 构造函数
	 */
	public EigenFaceFeature(DoubleFV fv) {
		this.fv = fv;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		fv = new DoubleFV();
		fv.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		fv.writeBinary(out);
	}

	@Override
	public DoubleFV getFeatureVector() {
		return fv;
	}

}
