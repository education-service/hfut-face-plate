package edu.hfut.fr.image.processing.convolution;

/**
 * 返回siga高斯x偏导数的内核
 *
 * @author wanghao
 */
public class FSobelX extends FConvolution {

	public FSobelX() {
		super(FSobelMagnitude.KERNEL_X);
	}

}
