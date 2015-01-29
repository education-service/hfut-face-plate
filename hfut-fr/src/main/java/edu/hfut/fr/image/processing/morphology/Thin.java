package edu.hfut.fr.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * 稀释部分
 *
 * @author Jimbo
 */
public class Thin implements ConnectedComponentProcessor, ImageProcessor<FImage> {

	protected HitAndMiss hitAndMiss;

	/**
	 * 稀释操作的构造函数
	 */
	public Thin(StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
	}

	@Override
	public void process(ConnectedComponent cc) {
		hitAndMiss.process(cc);

		cc.getPixels().removeAll(hitAndMiss.outputPixels);
	}

	@Override
	public void processImage(FImage image) {
		FImage newImage = image.process(hitAndMiss, true);

		for (int y = 0; y < newImage.height; y++) {
			for (int x = 0; x < newImage.width; x++) {
				if (newImage.pixels[y][x] == 1) {
					image.pixels[y][x] = 0;
				}
			}
		}
	}

}
