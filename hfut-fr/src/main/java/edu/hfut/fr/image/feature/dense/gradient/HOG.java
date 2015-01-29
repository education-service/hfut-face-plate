package edu.hfut.fr.image.feature.dense.gradient;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.GradientOrientationHistogramExtractor;
import edu.hfut.fr.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import edu.hfut.fr.image.processing.convolution.FImageGradients;

/**
 * 水平梯度直方图实现类
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Dalal, Navneet", "Triggs, Bill" }, title = "Histograms of Oriented Gradients for Human Detection", year = "2005", booktitle = "Proceedings of the 2005 IEEE Computer Society Conference on Computer Vision and Pattern Recognition (CVPR'05) - Volume 1 - Volume 01", pages = {
		"886", "", "893" }, url = "http://dx.doi.org/10.1109/CVPR.2005.177", publisher = "IEEE Computer Society", series = "CVPR '05", customData = {
		"isbn", "0-7695-2372-2", "numpages", "8", "doi", "10.1109/CVPR.2005.177", "acmid", "1069007", "address",
		"Washington, DC, USA" })
public class HOG implements ImageAnalyser<FImage> {

	GradientOrientationHistogramExtractor extractor;
	protected SpatialBinningStrategy strategy;

	private transient Histogram currentHist;

	/**
	 *通过给定策略提取特定特征
	 */
	public HOG(SpatialBinningStrategy strategy) {
		this(9, true, FImageGradients.Mode.Unsigned, strategy);
	}

	public HOG(int nbins, boolean histogramInterpolation, FImageGradients.Mode orientationMode,
			SpatialBinningStrategy strategy) {
		this.extractor = new GradientOrientationHistogramExtractor(nbins, histogramInterpolation, orientationMode);

		this.strategy = strategy;
	}

	@Override
	public void analyseImage(FImage image) {
		extractor.analyseImage(image);
	}

	/**
	 *分析图像,构建内部数据
	 */
	public void analyseImage(FImage image, FImage edges) {
		extractor.analyseImage(image, edges);
	}

	/**
	 *计算特征向量
	 */
	public Histogram getFeatureVector(Rectangle rectangle) {
		return currentHist = strategy.extract(extractor, rectangle, currentHist);
	}

}
