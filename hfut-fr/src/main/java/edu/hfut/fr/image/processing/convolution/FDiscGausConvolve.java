package edu.hfut.fr.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * matlab discgaussfft 方法实现
 *
 *@author wanghao
 */
public class FDiscGausConvolve implements SinglebandImageProcessor<Float, FImage> {

	private float sigma2;

	/**
	 * 给定变量构造
	 */
	public FDiscGausConvolve(float sigma2) {
		this.sigma2 = sigma2;
	}

	@Override
	public void processImage(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();
		FloatFFT_2D fft = new FloatFFT_2D(rs, cs);
		float[][] prepared = new float[rs][cs * 2];
		for (int r = 0; r < rs; r++) {
			for (int c = 0; c < cs; c++) {
				prepared[r][c * 2] = image.pixels[r][c];
				prepared[r][1 + c * 2] = 0;
			}
		}
		fft.complexForward(prepared);
		for (int y = 0; y < rs; y++) {
			for (int x = 0; x < cs; x++) {
				double xcos = Math.cos(2 * Math.PI * ((float) x / cs));
				double ycos = Math.cos(2 * Math.PI * ((float) y / rs));
				float multiply = (float) Math.exp(sigma2 * (xcos + ycos - 2));
				prepared[y][x * 2] = prepared[y][x * 2] * multiply;
				prepared[y][1 + x * 2] = prepared[y][1 + x * 2] * multiply;
			}
		}
		fft.complexInverse(prepared, true);
		for (int r = 0; r < rs; r++) {
			for (int c = 0; c < cs; c++) {
				image.pixels[r][c] = prepared[r][c * 2];
			}
		}
	}

}
