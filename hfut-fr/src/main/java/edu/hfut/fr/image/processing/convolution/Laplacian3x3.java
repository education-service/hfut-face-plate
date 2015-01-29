package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;

/**
 * 3*3内核拉普拉斯卷积操作
 *
 * @author wanghao
 */
public class Laplacian3x3 extends FConvolution {

	public Laplacian3x3() {
		super(new FImage(new float[][] { { 0, -1, 0 }, { -1, 4, -1 }, { 0, -1, 0 } }));
	}

}
