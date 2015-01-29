package edu.hfut.fr.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * 稀释部分的连续化
 *
 * @author Jimbo
 */
public class SequentialThin implements ConnectedComponentProcessor, ImageProcessor<FImage> {

	protected HitAndMiss hitAndMiss;
	protected int niter = -1;

	/**
	 * 构造函数
	 */
	public SequentialThin(StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
	}

	/**
	 * 构造函数
	 */
	public SequentialThin(int niter, StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
		this.niter = niter;
	}

	@Override
	public void process(ConnectedComponent cc) {
		for (int i = niter; i != 0; i--) {
			hitAndMiss.process(cc);

			if (hitAndMiss.outputPixels.size() == 0)
				break;

			cc.getPixels().removeAll(hitAndMiss.outputPixels);
		}
	}

	@Override
	public void processImage(FImage image) {
		for (int i = niter; i != 0; i--) {
			FImage newImage = image.process(hitAndMiss, true);

			int count = 0;
			for (int y = 0; y < newImage.height; y++) {
				for (int x = 0; x < newImage.width; x++) {
					if (newImage.pixels[y][x] == 1) {
						count++;
						image.pixels[y][x] = 0;
					}
				}
			}

			if (count == 0)
				break;
		}
	}

}
