package edu.hfut.fr.image.processing.restoration.inpainting;

import java.util.Collection;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processor.ImageProcessor;

/**
 * 修复算法接口
 *
 * @author jimbo
 */
public interface Inpainter<IMAGE extends Image<?, IMAGE>> extends ImageProcessor<IMAGE> {

	/**
	 * 设置mask
	 */
	public void setMask(FImage mask);

	/**
	 * 设置mask
	 */
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask);

	/**
	 * 设置mask
	 */
	public void setMask(int width, int height, PixelSet... mask);

	/**
	 * 修复给定图像
	 */
	@Override
	public void processImage(IMAGE image);

}
