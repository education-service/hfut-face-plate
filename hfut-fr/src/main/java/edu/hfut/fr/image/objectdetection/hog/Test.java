package edu.hfut.fr.image.objectdetection.hog;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.util.pair.ObjectDoublePair;

import edu.hfut.fr.image.feature.dense.gradient.HOG;
import edu.hfut.fr.image.feature.dense.gradient.binning.FlexibleHOGStrategy;

/**
 *  测试类
 *
 * @author wanghao
 */
public class Test {

	public static void main(String[] args) throws IOException {

		final FImage img = ImageUtilities.readF(new URL(
				"http://www.di.ens.fr/willow/teaching/recvis10/final_project/detection/car-img1.png"));

		final HOG h = new HOG(new FlexibleHOGStrategy(8, 8, 2));
		h.analyseImage(img);

		final Rectangle r = new Rectangle(47, 92, 30, 30);
		final Histogram f = h.getFeatureVector(r).clone();

		img.drawShape(r, 1f);
		DisplayUtilities.display(img);

		final FImage img2 = ImageUtilities.readF(new URL(
				"http://www.di.ens.fr/willow/teaching/recvis10/final_project/detection/car-img3.png"));
		h.analyseImage(img2);

		final List<ObjectDoublePair<Rectangle>> data = new ArrayList<ObjectDoublePair<Rectangle>>();
		for (int y = 0; y < img2.height - 30; y++) {
			for (int x = 0; x < img2.width - 30; x++) {
				final Rectangle rr = new Rectangle(x, y, 30, 30);
				final Histogram ff = h.getFeatureVector(rr);

				final double c = DoubleFVComparison.EUCLIDEAN.compare(f, ff);
				data.add(ObjectDoublePair.pair(rr, c));
			}
		}

		Collections.sort(data, new Comparator<ObjectDoublePair<Rectangle>>() {
			@Override
			public int compare(ObjectDoublePair<Rectangle> o1, ObjectDoublePair<Rectangle> o2) {
				return Double.compare(o1.second, o2.second);
			}
		});

		for (int i = 0; i < 10; i++) {
			img2.drawShape(data.get(i).first, 1F);
		}
		DisplayUtilities.display(img2);
	}

}
