package edu.hfut.fr.image.processing.extraction;

import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

public class TestOrientedPolygonExtractionProcessor {

	public void test90Degree() throws IOException {
		final FImage img = ImageUtilities.readF(TestOrientedPolygonExtractionProcessor.class
				.getResourceAsStream("/edu/hfut/fr/image/data/bird.png"));
		final Rectangle r = new Rectangle(320, 100, 60, 170);
		final Polygon p = r.asPolygon();
		final Polygon prot = p.clone();
		final Point2d center = prot.calculateCentroid();
		prot.rotate(center, Math.PI / 3);

		final OrientedPolygonExtractionProcessor opep = new OrientedPolygonExtractionProcessor(prot, 0.f);
		img.process(opep);
	}

}
