package edu.hfut.fr.image.feature.dense.gradient;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.sampling.QuadtreeSampler;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.BinnedWindowedExtractor;
import edu.hfut.fr.image.analysis.algorithm.histogram.InterpolatedBinnedWindowedExtractor;
import edu.hfut.fr.image.processing.convolution.FImageGradients;
import edu.hfut.fr.image.processing.convolution.FImageGradients.Mode;
import edu.hfut.fr.image.processing.edges.CannyEdgeDetector;

/**
 * 提取水平梯度金字塔直方图
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Bosch, Anna", "Zisserman, Andrew", "Munoz, Xavier" }, title = "Representing shape with a spatial pyramid kernel", year = "2007", booktitle = "Proceedings of the 6th ACM international conference on Image and video retrieval", pages = {
		"401", "", "408" }, url = "http://doi.acm.org/10.1145/1282280.1282340", publisher = "ACM", series = "CIVR '07", customData = {
		"isbn", "978-1-59593-733-9", "location", "Amsterdam, The Netherlands", "numpages", "8", "doi",
		"10.1145/1282280.1282340", "acmid", "1282340", "address", "New York, NY, USA", "keywords",
		"object and video retrieval, shape features, spatial pyramid kernel" })
public class PHOG implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {

	private int nlevels = 3;
	private ImageProcessor<FImage> edgeDetector;
	private Mode orientationMode;

	private BinnedWindowedExtractor histExtractor;
	private Rectangle lastBounds;
	private FImage magnitudes;

	/**
	 * 构造函数
	 */
	public PHOG() {
		this(4, 40, FImageGradients.Mode.Signed);
	}

	/**
	 *通过Canny边缘检查和梯度直方图差值构造函数
	 */
	public PHOG(int nlevels, int nbins, FImageGradients.Mode orientationMode) {
		this(nlevels, nbins, true, orientationMode, new CannyEdgeDetector());
	}

	/**
	 * 制定参数构造
	 */
	public PHOG(int nlevels, int nbins, boolean histogramInterpolation, FImageGradients.Mode orientationMode,
			ImageProcessor<FImage> edgeDetector) {
		this.nlevels = nlevels;
		this.edgeDetector = edgeDetector;
		this.orientationMode = orientationMode;

		if (histogramInterpolation)
			histExtractor = new InterpolatedBinnedWindowedExtractor(nbins, true);
		else
			histExtractor = new BinnedWindowedExtractor(nbins);

		histExtractor.setMax(orientationMode.maxAngle());
		histExtractor.setMin(orientationMode.minAngle());
	}

	@Override
	public void analyseImage(FImage image) {
		lastBounds = image.getBounds();

		final FImageGradients gradMag = FImageGradients.getGradientMagnitudesAndOrientations(image, orientationMode);
		this.magnitudes = gradMag.magnitudes;

		histExtractor.analyseImage(gradMag.orientations);

		if (edgeDetector != null) {
			magnitudes.multiplyInplace(image.process(edgeDetector));
		}
	}

	/**
	 *提取水平梯度金字塔直方图特征向量
	 */
	public Histogram getFeatureVector(Rectangle rect) {
		final QuadtreeSampler sampler = new QuadtreeSampler(rect, nlevels + 1);
		Histogram hist = new Histogram(0);

		for (final Rectangle r : sampler) {
			final Histogram h = histExtractor.computeHistogram(r, magnitudes);
			hist = hist.combine(h);
		}

		hist.normaliseL1();

		return hist;
	}

	@Override
	public Histogram getFeatureVector() {
		return getFeatureVector(lastBounds);
	}

}
