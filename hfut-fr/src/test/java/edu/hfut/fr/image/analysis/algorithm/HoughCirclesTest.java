package edu.hfut.fr.image.analysis.algorithm;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Circle;

import edu.hfut.fr.image.analysis.algorithm.HoughCircles.WeightedCircle;
import edu.hfut.fr.image.processing.edges.CannyEdgeDetector;

public class HoughCirclesTest {

	@Test
	public void testCircle() {
		final int imgWidthHeight = 200;

		final FImage circleImage = new FImage(imgWidthHeight, imgWidthHeight);
		final Circle c = new Circle(imgWidthHeight / 2 + 3, imgWidthHeight / 2 + 1, imgWidthHeight / 4);
		circleImage.drawShapeFilled(c, 1f);

		final CannyEdgeDetector det = new CannyEdgeDetector();
		final FImage edgeImage = circleImage.process(det);

		final HoughCircles circ = new HoughCircles(5, imgWidthHeight, 5, 360);
		edgeImage.analyseWith(circ);

		final List<WeightedCircle> best = circ.getBest(1);
		final WeightedCircle b = best.get(0);

		assertTrue(b.equals(c));
	}

}
