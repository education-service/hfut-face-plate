package edu.hfut.fr.image.analysis.algorithm.histogram;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.processing.convolution.FImageGradients;
import edu.hfut.fr.image.processing.convolution.FImageGradients.Mode;

public class SATWindowedExtractorTest {

	FImage image;
	SATWindowedExtractor satInterp;
	SATWindowedExtractor sat;
	InterpolatedBinnedWindowedExtractor binnedInterp;
	BinnedWindowedExtractor binned;
	private FImageGradients gradMags;

	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final Mode mode = FImageGradients.Mode.Unsigned;

		final FImage[] interpMags = new FImage[9];
		final FImage[] mags = new FImage[9];
		for (int i = 0; i < 9; i++) {
			interpMags[i] = new FImage(image.width, image.height);
			mags[i] = new FImage(image.width, image.height);
		}

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, interpMags, true, mode);
		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, mags, false, mode);

		satInterp = new SATWindowedExtractor(interpMags);
		sat = new SATWindowedExtractor(mags);

		gradMags = FImageGradients.getGradientMagnitudesAndOrientations(image, mode);
		binnedInterp = new InterpolatedBinnedWindowedExtractor(9, mode.minAngle(), mode.maxAngle(), true);
		binned = new BinnedWindowedExtractor(9, mode.minAngle(), mode.maxAngle());
		gradMags.orientations.analyseWith(binnedInterp);
		gradMags.orientations.analyseWith(binned);
	}

	@Test
	public void testFullHistogram() {
		final Histogram hist1 = binned.computeHistogram(image.getBounds(), gradMags.magnitudes);
		final Histogram hist2 = sat.computeHistogram(image.getBounds());
		assertArrayEquals(hist1.values, hist2.values, 0.5);

		final Histogram hist3 = binnedInterp.computeHistogram(image.getBounds(), gradMags.magnitudes);
		final Histogram hist4 = satInterp.computeHistogram(image.getBounds());

		assertArrayEquals(hist3.values, hist4.values, 0.5);
	}

}
