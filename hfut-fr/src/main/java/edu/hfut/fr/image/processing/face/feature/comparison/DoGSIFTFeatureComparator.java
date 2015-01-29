package edu.hfut.fr.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.NullModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.residuals.TransformedSITR2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.UnivariateGaussianNaiveBayesModel;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.math.model.fit.SimpleModelFitting;
import org.openimaj.math.util.distance.ModelDistanceCheck;
import org.openimaj.util.pair.Pair;

import edu.hfut.fr.image.processing.face.feature.DoGSIFTFeature;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.statistics.DataHistogram;
import gov.sandia.cognition.statistics.distribution.MapBasedDataHistogram;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

/**
 * 基于DoGSIFTF特征的比较器
 *
 *@author jimbo
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Ozkan, Derya", "Duygulu, Pinar" }, title = "Finding people frequently appearing in news", year = "2006", booktitle = "Proceedings of the 5th international conference on Image and Video Retrieval", pages = {
		"173", "", "182" }, url = "http://dx.doi.org/10.1007/11788034_18", publisher = "Springer-Verlag", series = "CIVR'06", customData = {
		"isbn", "3-540-36018-2, 978-3-540-36018-6", "location", "Tempe, AZ", "numpages", "10", "doi",
		"10.1007/11788034_18", "acmid", "2164555", "address", "Berlin, Heidelberg" })
public class DoGSIFTFeatureComparator implements FacialFeatureComparator<DoGSIFTFeature> {

	@Override
	public void readBinary(DataInput in) throws IOException {
	}

	@Override
	public byte[] binaryHeader() {
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
	}

	public static ModelDistanceCheck buildDefaultDistanceCheck() {

		final DataHistogram<Boolean> priors = new MapBasedDataHistogram<Boolean>();
		priors.add(true, 3380);
		priors.add(false, 748);
		final Map<Boolean, List<PDF>> conditionals = new HashMap<Boolean, List<PDF>>();
		conditionals.put(true, Arrays.asList(new PDF[] { new PDF(0.029852218270328083, 0.003255709240977441) }));
		conditionals.put(false, Arrays.asList(new PDF[] { new PDF(0.4461058073589149, 0.04829317710091845) }));

		final VectorNaiveBayesCategorizer<Boolean, PDF> bayes = new VectorNaiveBayesCategorizer<Boolean, PDF>(priors,
				conditionals);
		final Model<Double, Boolean> distanceModel = new UnivariateGaussianNaiveBayesModel<Boolean>(bayes);
		final ModelDistanceCheck dc = new ModelDistanceCheck(distanceModel);

		return dc;
	}

	@Override
	public double compare(DoGSIFTFeature query, DoGSIFTFeature target) {
		final Rectangle unit = new Rectangle(0, 0, 1, 1);

		final TransformedSITR2d<NullModel<Point2d>> tte = new TransformedSITR2d<NullModel<Point2d>>(
				TransformUtilities.makeTransform(query.getBounds(), unit), TransformUtilities.makeTransform(
						target.getBounds(), unit));
		final ModelDistanceCheck dt = buildDefaultDistanceCheck();

		final NullModel<Point2d> model = new NullModel<Point2d>();
		final RobustModelFitting<Point2d, Point2d, ?> fitting = new SimpleModelFitting<Point2d, Point2d, NullModel<Point2d>>(
				model, tte, dt);
		final BasicTwoWayMatcher<Keypoint> innerMatcher = new BasicTwoWayMatcher<Keypoint>();
		final ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				innerMatcher, fitting);

		matcher.setModelFeatures(target.getKeys());
		matcher.findMatches(query.getKeys());

		double score = 0;
		for (final Pair<Keypoint> p : matcher.getMatches()) {
			double accum = 0;
			final byte[] v1 = p.firstObject().ivec;
			final byte[] v2 = p.secondObject().ivec;
			for (int i = 0; i < v1.length; i++) {
				final double v1i = (v1[i]);
				final double v2i = (v2[i]);
				accum += (v1i - v2i) * (v1i - v2i);
			}
			score += Math.sqrt(accum);
		}

		if (matcher.getMatches().size() == 0)
			return Math.sqrt(255 * 255 * 128);

		return (score / matcher.getMatches().size());
	}

	@Override
	public boolean isDistance() {
		return true;
	}

}
