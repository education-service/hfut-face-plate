package edu.hfut.fr.image.objectdetection.hog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Evaluate实现类
 *
 * @author wanghao
 */
public class Evaluate {

	public static void main(String[] args) throws IOException {
		final HOGClassifier classifier = IOUtils.readFromFile(new File("initial-classifier.dat"));
		final HOGDetector detector = new HOGDetector(classifier);

		for (float thresh = 0; thresh < 1; thresh += 0.1) {
			detector.threshold = thresh;

			final ListDataset<FImage> neg = new VFSListDataset<FImage>("/Users/jsh2/Data/INRIAPerson/Test/neg",
					ImageUtilities.FIMAGE_READER);
			final ListDataset<FImage> pos = new VFSListDataset<FImage>("/Users/jsh2/Data/INRIAPerson/Test/pos",
					ImageUtilities.FIMAGE_READER);

			int falsePositives = 0;
			int trueNegatives = 0;
			for (final FImage i : neg) {
				final List<Rectangle> rectangles = detector.detect(i);
				if (rectangles.size() > 0)
					falsePositives++;
				else
					trueNegatives++;
			}

			int falseNegatives = 0;
			int truePositives = 0;
			for (final FImage i : pos) {
				final List<Rectangle> rectangles = detector.detect(i);
				if (rectangles.size() > 0)
					truePositives++;
				else
					falseNegatives++;
			}

			final double missRate = (double) falseNegatives / (double) (truePositives + falseNegatives);
			System.out.format("%f\t%d\t%d\t%d\t%d\t%f\n", thresh, truePositives, trueNegatives, falsePositives,
					falseNegatives, missRate);
		}
	}

}
