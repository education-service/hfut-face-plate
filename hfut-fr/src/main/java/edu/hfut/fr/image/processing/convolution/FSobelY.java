package edu.hfut.fr.image.processing.convolution;

/**
 * 返回sigma高斯的Y的偏导数的内核
 *
 * @author wanghao
 */
public class FSobelY extends FConvolution {

	public FSobelY() {
		super(FSobelMagnitude.KERNEL_Y);
	}

}
