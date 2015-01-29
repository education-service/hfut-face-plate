package edu.hfut.fr.image.processing.face.detection.keypoints;

import org.openimaj.image.FImage;

import edu.hfut.fr.image.analysis.algorithm.SummedAreaTable;

/**
 * Masked Haar 级联
 *
 *@author wanggang
 */
class MaskedHaarCascade {

	static FImage maskedHaarCascade(final SummedAreaTable integralImage, final int wh, final int ww, final int[][] H,
			final double[][] TA, final boolean[][] M) {
		final int ih = integralImage.data.height - 1;
		final int iw = integralImage.data.width - 1;

		final int nf = H.length;
		final int nh = H[0].length;

		final int[] HI = new int[nf * nh];
		for (int h = 0, dp = 0; h < nh; h++) {
			for (int f = 0; f < nf; f++, dp++) {
				if (H[f][h] != 0) {
					int sign, ind;
					if (H[f][h] < 0) {
						sign = -1;
						ind = -H[f][h];
					} else {
						sign = 1;
						ind = H[f][h];
					}

					int x = (ind - 1) / (wh + 1);
					int y = ind - 1 - x * (wh + 1);

					HI[dp] = (x * (ih + 1) + y + 1) * sign;
				}
			}
		}

		final FImage Q = new FImage(iw, ih);
		Q.fill(Float.NEGATIVE_INFINITY);

		final int x1 = ww / 2, x2 = iw - 1 - ww / 2;
		final int y1 = wh / 2, y2 = ih - 1 - wh / 2;
		final int coloff = ih + y1 - y2 - 1;

		final float[][] iimg = integralImage.data.pixels;
		final float[][] Qpixels = Q.pixels;

		final float[] iimgF = new float[integralImage.data.width * integralImage.data.height];
		for (int y = 0, i = 0; y < iimg[0].length; y++)
			for (int x = 0; x < iimg.length; x++, i++)
				iimgF[i] = iimg[x][y];

		int II = -1;
		for (int x = x1; x <= x2; x++, II += coloff + 1) {
			for (int y = y1; y <= y2; y++, II++) {
				if (M[y][x]) {
					int hp = 0; // HI;
					int tap = 0;
					float q = 0;

					for (int h = 0; h < nh; h++, tap++) {
						if (HI[hp] == 0) {

							if (q < TA[1][tap]) {
								q = Float.NEGATIVE_INFINITY;
								break;
							}
							if (h + 1 == nh) {
								q -= TA[1][tap];
								break;
							}

							q = 0;
							hp += nf;
						} else {
							float s = 0;

							for (int f = 0; f < nf; f++, hp++) {
								if (HI[hp] == 0) {
									hp += nf - f;
									break;
								}

								if (HI[hp] < 0) {
									final int index = II - HI[hp];
									s -= iimgF[index];
								} else {
									final int index = II + HI[hp];
									s += iimgF[index];
								}
							}

							if (s >= TA[0][tap]) {
								q += TA[1][tap];
							} else {
								q -= TA[1][tap];
							}
						}
					}
					Qpixels[y][x] = q;
				}
			}
		}

		return Q;
	}

}
