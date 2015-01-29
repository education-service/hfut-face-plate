package edu.hfut.fr.image.analysis.watershed.feature;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.IntValuePixel;

/**
 *  像素特征类
 *
 * @author wanghao
 */
public class PixelsFeature extends ConnectedComponent implements ComponentFeature {

	@Override
	public void merge(ComponentFeature f) {
		pixels.addAll(((PixelsFeature) f).pixels);
	}

	@Override
	public void addSample(IntValuePixel p) {
		pixels.add(p);
	}

	@Override
	public PixelsFeature clone() {
		return (PixelsFeature) super.clone();
	}

}
