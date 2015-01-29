package edu.hfut.fr.image.processing.restoration.inpainting;

import java.util.Collection;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;

/**
 * 修复算法的抽象方法
 *
 * @author jimbo
 */
public abstract class AbstractImageMaskInpainter<IMAGE extends Image<?, IMAGE>> implements Inpainter<IMAGE> {

	/**
	 * mask 图像
	 */
	protected FImage mask;

	@Override
	public void setMask(FImage mask) {
		this.mask = mask;
		initMask();
	}

	@Override
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	@Override
	public void setMask(int width, int height, PixelSet... mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	/**
	 * 初始化mask
	 */
	protected void initMask() {

	};

	@Override
	public final void processImage(IMAGE image) {
		if (mask == null)
			throw new IllegalArgumentException("Mask has not been set");

		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight())
			throw new IllegalArgumentException("Image and mask size do not match");

		performInpainting(image);
	}

	/**
	 * 修复给定图像
	 */
	protected abstract void performInpainting(IMAGE image);

}
