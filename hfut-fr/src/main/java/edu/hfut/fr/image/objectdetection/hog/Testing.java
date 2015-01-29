package edu.hfut.fr.image.objectdetection.hog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectIntPair;

import edu.hfut.fr.image.objectdetection.filtering.OpenCVGrouping;

/**
 *  测试类
 *
 * @author wanghao
 */
public class Testing {

	public static void main(String[] args) throws IOException {

		final HOGClassifier classifier = IOUtils.readFromFile(new File("final-classifier.dat"));

		final HOGDetector detector = new HOGDetector(classifier);
		final FImage img = ImageUtilities.readF(new File("/Users/jsh2/Data/INRIAPerson/Test/pos/crop_000006.png"));
		final List<Rectangle> dets = detector.detect(img);

		final List<ObjectIntPair<Rectangle>> fdets = new OpenCVGrouping().apply(dets);

		final MBFImage rgb = img.toRGB();
		for (final Rectangle r : dets) {
			rgb.drawShape(r, RGBColour.RED);
		}
		for (final ObjectIntPair<Rectangle> r : fdets) {
			rgb.drawShape(r.first, RGBColour.GREEN);
		}

		DisplayUtilities.display(rgb);
	}

}
