package edu.hfut.fr.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

import edu.hfut.fr.image.processing.convolution.FImageGradients;

/**
 * 抓取梯度方向直方图
 *
 * @author wanggang
 */
public class GradientOrientationHistogramExtractor extends SATWindowedExtractor implements ImageAnalyser<FImage> {

	private FImageGradients.Mode orientationMode;
	private boolean histogramInterpolation;

	/**
	 *构建梯度直方图的解释器
	 */
	public GradientOrientationHistogramExtractor(int nbins, boolean histogramInterpolation,
			FImageGradients.Mode orientationMode) {
		super(nbins);

		this.histogramInterpolation = histogramInterpolation;
		this.orientationMode = orientationMode;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImage[] magnitudes = new FImage[nbins];

		for (int i = 0; i < nbins; i++)
			magnitudes[i] = new FImage(image.width, image.height);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, magnitudes, histogramInterpolation,
				orientationMode);

		computeSATs(magnitudes);
	}

	/**
	 *返回图像的边缘
	 */
	public void analyseImage(FImage image, FImage edges) {
		final FImage[] magnitudes = new FImage[nbins];

		for (int i = 0; i < nbins; i++)
			magnitudes[i] = new FImage(image.width, image.height);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, magnitudes, histogramInterpolation,
				orientationMode);

		for (int i = 0; i < nbins; i++)
			magnitudes[i].multiplyInplace(edges);

		computeSATs(magnitudes);
	}

}
