package edu.hfut.fr.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * 关闭部分
 *
 * @author Jimbo
 */
public class Close implements ConnectedComponentProcessor, ImageProcessor<FImage> {

	protected Dilate dilate;
	protected Erode erode;

	/**
	 * 构造函数
	 */
	public Close(StructuringElement se) {
		this.dilate = new Dilate(se);
		this.erode = new Erode(se);
	}

	/**
	 * 构造函数
	 */
	public Close() {
		this(StructuringElement.BOX);
	}

	@Override
	public void process(ConnectedComponent cc) {
		dilate.process(cc);
		erode.process(cc);
	}

	@Override
	public void processImage(FImage image) {
		image.processInplace(dilate, true);
		image.processInplace(erode, true);
	}
}
