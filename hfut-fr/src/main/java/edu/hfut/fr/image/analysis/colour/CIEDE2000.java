package edu.hfut.fr.image.analysis.colour;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.combiner.ImageCombiner;

/**
 * CIE 2000 颜色差分方程实习和计算两幅图像之间的颜色差异图
 *
 * @author   wanggang
 */
public class CIEDE2000 implements ImageCombiner<MBFImage, MBFImage, FImage> {

	/**
	 * 计算两个颜色差异
	 */
	public static double calculateDeltaE(double[] lab1, double[] lab2) {
		return calculateDeltaE(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
	}

	public static float calculateDeltaE(float[] lab1, float[] lab2) {
		return (float) calculateDeltaE(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
	}

	public static float calculateDeltaE(Float[] lab1, Float[] lab2) {
		return (float) calculateDeltaE(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
	}

	public static double calculateDeltaE(double L1, double a1, double b1, double L2, double a2, double b2) {
		double Lmean = (L1 + L2) / 2.0; //ok
		double C1 = Math.sqrt(a1 * a1 + b1 * b1); //ok
		double C2 = Math.sqrt(a2 * a2 + b2 * b2); //ok
		double Cmean = (C1 + C2) / 2.0; //ok

		double G = (1 - Math.sqrt(Math.pow(Cmean, 7) / (Math.pow(Cmean, 7) + Math.pow(25, 7)))) / 2; //ok
		double a1prime = a1 * (1 + G); //ok
		double a2prime = a2 * (1 + G); //ok

		double C1prime = Math.sqrt(a1prime * a1prime + b1 * b1); //ok
		double C2prime = Math.sqrt(a2prime * a2prime + b2 * b2); //ok
		double Cmeanprime = (C1prime + C2prime) / 2; //ok

		double h1prime = Math.atan2(b1, a1prime) + 2 * Math.PI * (Math.atan2(b1, a1prime) < 0 ? 1 : 0);
		double h2prime = Math.atan2(b2, a2prime) + 2 * Math.PI * (Math.atan2(b2, a2prime) < 0 ? 1 : 0);
		double Hmeanprime = ((Math.abs(h1prime - h2prime) > Math.PI) ? (h1prime + h2prime + 2 * Math.PI) / 2
				: (h1prime + h2prime) / 2); //ok

		double T = 1.0 - 0.17 * Math.cos(Hmeanprime - Math.PI / 6.0) + 0.24 * Math.cos(2 * Hmeanprime) + 0.32
				* Math.cos(3 * Hmeanprime + Math.PI / 30) - 0.2 * Math.cos(4 * Hmeanprime - 21 * Math.PI / 60); //ok

		double deltahprime = ((Math.abs(h1prime - h2prime) <= Math.PI) ? h2prime - h1prime
				: (h2prime <= h1prime) ? h2prime - h1prime + 2 * Math.PI : h2prime - h1prime - 2 * Math.PI); //ok

		double deltaLprime = L2 - L1; //ok
		double deltaCprime = C2prime - C1prime; //ok
		double deltaHprime = 2.0 * Math.sqrt(C1prime * C2prime) * Math.sin(deltahprime / 2.0); //ok
		double SL = 1.0 + ((0.015 * (Lmean - 50) * (Lmean - 50)) / (Math.sqrt(20 + (Lmean - 50) * (Lmean - 50)))); //ok
		double SC = 1.0 + 0.045 * Cmeanprime; //ok
		double SH = 1.0 + 0.015 * Cmeanprime * T; //ok

		double deltaTheta = (30 * Math.PI / 180)
				* Math.exp(-((180 / Math.PI * Hmeanprime - 275) / 25) * ((180 / Math.PI * Hmeanprime - 275) / 25));
		double RC = (2 * Math.sqrt(Math.pow(Cmeanprime, 7) / (Math.pow(Cmeanprime, 7) + Math.pow(25, 7))));
		double RT = (-RC * Math.sin(2 * deltaTheta));

		double KL = 1;
		double KC = 1;
		double KH = 1;

		double deltaE = Math.sqrt(((deltaLprime / (KL * SL)) * (deltaLprime / (KL * SL)))
				+ ((deltaCprime / (KC * SC)) * (deltaCprime / (KC * SC)))
				+ ((deltaHprime / (KH * SH)) * (deltaHprime / (KH * SH)))
				+ (RT * (deltaCprime / (KC * SC)) * (deltaHprime / (KH * SH))));

		return deltaE;
	}

	/**
	 * 计算两幅图像之间的视差图
	 */
	public static FImage makeDisparityMap(MBFImage im1, MBFImage im2) {
		if (im1.colourSpace != ColourSpace.CIE_Lab) {
			im1 = ColourSpace.convert(im1, ColourSpace.CIE_Lab);
		}

		if (im2.colourSpace != ColourSpace.CIE_Lab) {
			im2 = ColourSpace.convert(im2, ColourSpace.CIE_Lab);
		}

		FImage disparity = new FImage(im1.getWidth(), im1.getHeight());
		for (int y = 0; y < disparity.height; y++) {
			for (int x = 0; x < disparity.width; x++) {
				disparity.pixels[y][x] = calculateDeltaE(im1.getPixel(x, y), im2.getPixel(x, y));
			}
		}

		return disparity;
	}

	@Override
	public FImage combine(MBFImage image1, MBFImage image2) {
		return makeDisparityMap(image2, image2);
	}

}
