package edu.hfut.fr.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.model.FisherImages;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.pair.IndependentPair;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * FisherFace特征
 *
 * @author wanggang
 */
@Reference(type = ReferenceType.Article, author = { "Belhumeur, Peter N.", "Hespanha, Jo\\~{a}o P.",
		"Kriegman, David J." }, title = "Fisherfaces vs. Fisherfaces: Recognition Using Class Specific Linear Projection", year = "1997", journal = "IEEE Trans. Pattern Anal. Mach. Intell.", pages = {
		"711", "", "720" }, url = "http://dx.doi.org/10.1109/34.598228", month = "July", number = "7", publisher = "IEEE Computer Society", volume = "19", customData = {
		"issn", "0162-8828", "numpages", "10", "doi", "10.1109/34.598228", "acmid", "261512", "address",
		"Washington, DC, USA", "keywords",
		"Appearance-based vision, face recognition, illumination invariance, Fisher's linear discriminant." })
public class FisherFaceFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {

	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<FisherFaceFeature, T>,
			BatchTrainer<IndependentPair<?, T>> {
		FisherImages fisher = null;
		FaceAligner<T> aligner = null;

		public Extractor(int numComponents, FaceAligner<T> aligner) {
			this(new FisherImages(numComponents), aligner);
		}

		public Extractor(FisherImages basis, FaceAligner<T> aligner) {
			this.fisher = basis;
			this.aligner = aligner;
		}

		@Override
		public FisherFaceFeature extractFeature(T face) {
			final FImage patch = aligner.align(face);

			final DoubleFV fv = fisher.extractFeature(patch);

			return new FisherFaceFeature(fv);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			fisher.readBinary(in);

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
			fisher.writeBinary(out);

			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public void train(final List<? extends IndependentPair<?, T>> data) {
			final List<IndependentPair<?, FImage>> patches = new AbstractList<IndependentPair<?, FImage>>() {

				@Override
				public IndependentPair<?, FImage> get(int index) {
					return IndependentPair.pair(data.get(index).firstObject(),
							aligner.align(data.get(index).secondObject()));
				}

				@Override
				public int size() {
					return data.size();
				}

			};

			fisher.train(patches);
		}

		/**
		 * 进行训练
		 */
		public void train(Map<?, ? extends List<T>> data) {
			final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

			for (final Entry<?, ? extends List<T>> e : data.entrySet()) {
				for (final T i : e.getValue()) {
					list.add(IndependentPair.pair(e.getKey(), aligner.align(i)));
				}
			}

			fisher.train(list);
		}

		/**
		 * 进行训练
		 */
		public <KEY> void train(GroupedDataset<KEY, ? extends ListDataset<T>, T> data) {
			final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

			for (final KEY e : data.getGroups()) {
				for (final T i : data.getInstances(e)) {
					if (i != null)
						list.add(IndependentPair.pair(e, aligner.align(i)));
				}
			}

			fisher.train(list);
		}
	}

	private DoubleFV fv;

	protected FisherFaceFeature() {
		this(null);
	}

	/**
	 * 构造函数
	 */
	public FisherFaceFeature(DoubleFV fv) {
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
