package edu.hfut.fr.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * 开启部分
 *
 * @author Jimbo
 */
public class Open implements ConnectedComponentProcessor, ImageProcessor<FImage> {

	protected Erode erode;
	protected Dilate dilate;

	/**
	 * 构造函数
	 */
	public Open(StructuringElement se) {
		this.erode = new Erode(se);
		this.dilate = new Dilate(se);
	}

	/**
	 * 构造函数
	 */
	public Open() {
		this(StructuringElement.BOX);
	}

	@Override
	public void process(ConnectedComponent cc) {
		erode.process(cc);
		dilate.process(cc);
	}

	@Override
	public void processImage(FImage image) {
		image.processInplace(erode, true);
		image.processInplace(dilate, true);
	}

}
