package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;

/**
 * 标准微分内核
 *
 *@author wanghao
 */
public class BasicDerivativeKernels {

	static class DxKernel extends FConvolution {
		public DxKernel() {
			super(new FImage(new float[][] { { -0.5f, 0, 0.5f } }));
		}
	}

	static class DyKernel extends FConvolution {
		public DyKernel() {
			super(new FImage(new float[][] { { -0.5f }, { 0 }, { 0.5f } }));
		}
	}

	static class DxxKernel extends FConvolution {
		public DxxKernel() {
			super(new FImage(new float[][] { { 1, -2, 1 } }));
		}
	}

	static class DxyKernel extends FConvolution {
		public DxyKernel() {
			super(new FImage(new float[][] { { 0.25f, 0, -0.25f }, { 0, 0, 0 }, { -0.25f, 0, 0.25f } }));
		}
	}

	static class DyyKernel extends FConvolution {
		public DyyKernel() {
			super(new FImage(new float[][] { { 1 }, { -2 }, { 1 } }));
		}
	}

	static class DxxxxKernel extends FConvolution {
		public DxxxxKernel() {
			super(new FImage(new float[][] { { 1, -4, 6, -4, 1 } }));
		}
	}

	static class DyyyyKernel extends FConvolution {
		public DyyyyKernel() {
			super(new FImage(new float[][] { { 1 }, { -4 }, { 6 }, { -4 }, { 1 } }));
		}
	}

	static class DxxyyKernel extends FConvolution {
		public DxxyyKernel() {
			super(new FImage(new float[][] { { 1f, -2f, 1f }, { -2f, 4f, -2f }, { 1f, -2f, 1f } }));
		}
	}

	/**
	 * 逼近低阶sigma一阶导数高斯
	 */
	public static final FConvolution DX_KERNEL = new DxKernel();

	public static final FConvolution DY_KERNEL = new DyKernel();

	/**
	 * 逼近低阶sigma二阶导数高斯
	 */
	public static final FConvolution DXX_KERNEL = new DxxKernel();

	public static final FConvolution DXY_KERNEL = new DxyKernel();

	public static final FConvolution DYY_KERNEL = new DyyKernel();

	/**
	 * 逼近低阶sigma高斯四阶导数
	 */
	public static final FConvolution DXXXX_KERNEL = new DxxxxKernel();

	public static final FConvolution DXXYY_KERNEL = new DxxyyKernel();

	public static final FConvolution DYYYY_KERNEL = new DyyyyKernel();

}
