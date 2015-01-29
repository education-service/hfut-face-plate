package edu.hfut.fr.driver.run;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * 面部矫正实例对比
 *
 * @author wanghao
 */
public class FaceAlignerInstance implements Display {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		/*
		 * Affine 面部矫正
		 */
		AffineFaceAligner aff = new AffineFaceAligner();

		/*
		 * MeshWarp 面部矫正
		 */
		MeshWarpFaceAligner mesh = new MeshWarpFaceAligner();

		/*
		 * RotateScaleFaceAligner 面部矫正
		 */
		RotateScaleFaceAligner rotate = new RotateScaleFaceAligner();

		/*
		 *维度面部矫正,需要自己设置参数
		 */
		ScalingFaceAligner scal = new ScalingFaceAligner();

	}

	@Override
	public void displayMBF(MBFImage image) {
		//
	}

	@Override
	public void displayF(FImage image) {
		//
	}

}
