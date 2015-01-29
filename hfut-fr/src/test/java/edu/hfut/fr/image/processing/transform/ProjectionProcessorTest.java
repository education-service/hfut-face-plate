package edu.hfut.fr.image.processing.transform;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import Jama.Matrix;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;

public class ProjectionProcessorTest {

	MBFImage image = null;

	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readMBF(this.getClass().getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
	}

	@Test
	public void testSingleImage() {
		double rot = 90 * (Math.PI / 180);
		Matrix rotationMatrix = Matrix.constructWithCopy(new double[][] { { Math.cos(rot), -Math.sin(rot), 0 },
				{ Math.sin(rot), Math.cos(rot), 0 }, { 0, 0, 1 }, });

		ProjectionProcessor<Float[], MBFImage> process = new ProjectionProcessor<Float[], MBFImage>();
		process.setMatrix(rotationMatrix);
		image.accumulateWith(process);
		DisplayUtilities.display(process.performProjection().process(new ResizeProcessor(300, 300)));
	}

	public static void main(String[] args) throws IOException {
		ProjectionProcessorTest t = new ProjectionProcessorTest();
		t.setup();
		t.testSingleImage();
	}

}
