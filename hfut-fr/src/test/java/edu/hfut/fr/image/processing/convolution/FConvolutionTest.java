package edu.hfut.fr.image.processing.convolution;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.image.FImage;

public class FConvolutionTest {

	@Test
	public void testConsistency() {
		FImage kernel = new FImage(3, 3);
		kernel.addInplace(1f);

		FImage kernelRow = new FImage(3, 1);
		FImage kernelCol = new FImage(1, 3);

		kernelRow.addInplace(3f);
		kernelCol.addInplace(3f);

		FImage im = new FImage(10, 10);
		im.addInplace(1f);

		FConvolution conAutoSep = new FConvolution(kernel);
		FConvolution conBrute = new FConvolution(kernel);
		FConvolution conAutoRow = new FConvolution(kernelRow);
		FConvolution conAutoCol = new FConvolution(kernelCol);

		conBrute.setBruteForce(true);

		assertTrue(im.process(conAutoSep).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoRow).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conAutoCol).equalsThresh(im.multiply(9f), 0.001f));
		assertTrue(im.process(conBrute).extractROI(1, 1, im.width - 3, im.height - 3)
				.equalsThresh(im.multiply(9f).extractROI(1, 1, im.width - 3, im.height - 3), 0.001f));
	}

}
