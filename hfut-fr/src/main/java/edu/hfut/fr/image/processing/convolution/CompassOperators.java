package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;

/**
 * 指向操作
 *
 *@author wanghao
 */
public class CompassOperators {

	static public class Compass0 extends FConvolution {
		/**
		 * 默认构造函数
		 */
		public Compass0() {
			super(new FImage(new float[][] { { -1, -1, -1 }, { 2, 2, 2 }, { -1, -1, -1 } }));
		}
	}

	static public class Compass45 extends FConvolution {

		public Compass45() {
			super(new FImage(new float[][] { { -1, -1, 2 }, { -1, 2, -1 }, { 2, -1, -1 } }));
		}
	}

	static public class Compass90 extends FConvolution {
		public Compass90() {

			super(new FImage(new float[][] { { -1, 2, -1 }, { -1, 2, -1 }, { -1, 2, -1 } }));
		}
	}

	static public class Compass135 extends FConvolution {
		/**
		 * 默认构造函数
		 */
		public Compass135() {
			super(new FImage(new float[][] { { 2, -1, -1 }, { -1, 2, -1 }, { -1, -1, 2 } }));
		}
	}

}
