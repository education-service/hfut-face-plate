package edu.hfut.fr.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;

import edu.hfut.fr.image.processing.face.alignment.FaceAligner;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.feature.FacialFeatureExtractor;

/**
 * 基于LTP 的计算方法
 *
 * @author jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Xiaoyang Tan", "Triggs, B." }, title = "Enhanced Local Texture Feature Sets for Face Recognition Under Difficult Lighting Conditions", year = "2010", journal = "Image Processing, IEEE Transactions on", pages = {
		"1635 ", "1650" }, month = "june ", number = "6", volume = "19", customData = {
		"keywords",
		"CAS-PEAL-R1;Gabor wavelets;PCA;distance transform based matching;extended Yale-B;face recognition;kernel-based feature extraction;local binary patterns;local spatial histograms;local ternary patterns;local texture feature set enhancement;local texture-based face representations;multiple feature fusion;principal component analysis;robust illumination normalization;face recognition;feature extraction;image enhancement;image fusion;image representation;image texture;principal component analysis;wavelet transforms;Algorithms;Biometry;Face;Humans;Image Enhancement;Image Interpretation, Computer-Assisted;Imaging, Three-Dimensional;Lighting;Pattern Recognition, Automated;Reproducibility of Results;Sensitivity and Specificity;Subtraction Technique;",
		"doi", "10.1109/TIP.2010.2042645", "ISSN", "1057-7149" })
public class LtpDtFeature extends AbstractLtpDtFeature {

	public static class Extractor<Q extends DetectedFace> implements FacialFeatureExtractor<LtpDtFeature, Q> {
		LTPWeighting weighting;
		FaceAligner<Q> aligner;

		protected Extractor() {
		}

		public Extractor(FaceAligner<Q> aligner, LTPWeighting weighting) {
			this.aligner = aligner;
			this.weighting = weighting;
		}

		@Override
		public LtpDtFeature extractFeature(Q detectedFace) {
			FImage face = aligner.align(detectedFace);
			FImage mask = aligner.getMask();

			return new LtpDtFeature(face, mask, weighting);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String weightingClass = in.readUTF();
			weighting = IOUtils.newInstance(weightingClass);
			weighting.readBinary(in);

			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(weighting.getClass().getName());
			weighting.writeBinary(out);

			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public String toString() {
			return "LtpDtFeature.Factory[weighting=" + weighting + "]";
		}
	}

	public LtpDtFeature(FImage face, FImage mask, LTPWeighting weighting) {
		super(face.width, face.height, weighting, extractLTPSlicePixels(normaliseImage(face, mask)));
	}

}
