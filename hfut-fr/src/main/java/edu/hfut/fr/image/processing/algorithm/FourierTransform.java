package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.image.FImage;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 *   图像数据傅里叶变化
 *
 *@author wanghao
 */
public class FourierTransform {

	private FImage phase;
	private FImage magnitude;
	private boolean centre;

	/**
	 * 傅里叶变化的函数
	 */
	public FourierTransform(FImage image, boolean centre) {
		this.centre = centre;

		process(image);
	}

	public FourierTransform(FImage magnitude, FImage phase, boolean centre) {
		this.centre = centre;
		this.magnitude = magnitude;
		this.phase = phase;
	}

	/**
	 *准备输入数据
	 */
	public static float[][] prepareData(FImage input, int rs, int cs, boolean centre) {
		return prepareData(input.pixels, rs, cs, centre);
	}

	public static float[] prepareData1d(FImage input, int rs, int cs, boolean centre) {
		return prepareData1d(input.pixels, rs, cs, centre);
	}

	public static float[][] prepareData(float[][] input, int rs, int cs, boolean centre) {
		float[][] prepared = new float[rs][cs * 2];

		if (centre) {
			for (int r = 0; r < Math.min(rs, input.length); r++) {
				for (int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r][c * 2] = input[r][c] * (1 - 2 * ((r + c) % 2));
				}
			}
		} else {
			for (int r = 0; r < Math.min(rs, input.length); r++) {
				for (int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r][c * 2] = input[r][c];
				}
			}
		}

		return prepared;
	}

	/**
	 *将1维数据作为输入数据
	 */
	public static float[] prepareData1d(float[][] input, int rs, int cs, boolean centre) {
		float[] prepared = new float[rs * cs * 2];

		if (centre) {
			for (int r = 0; r < Math.min(rs, input.length); r++) {
				for (int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r * 2 * cs + 2 * c] = input[r][c] * (1 - 2 * ((r + c) % 2));
				}
			}
		} else {
			for (int r = 0; r < Math.min(rs, input.length); r++) {
				for (int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r * 2 * cs + 2 * c] = input[r][c];
				}
			}
		}

		return prepared;
	}

	/**
	 *从准备数据中抽取实际特征
	 */
	public static void unprepareData(float[][] prepared, FImage output, boolean centre) {
		unprepareData(prepared, output.pixels, centre);
	}

	public static void unprepareData(float[] prepared, FImage output, boolean centre) {
		unprepareData(prepared, output.pixels, centre);
	}

	public static void unprepareData(float[][] prepared, float[][] output, boolean centre) {
		int rs = output.length;
		int cs = output[0].length;

		if (centre) {
			for (int r = 0; r < rs; r++) {
				for (int c = 0; c < cs; c++) {
					output[r][c] = prepared[r][c * 2] * (1 - 2 * ((r + c) % 2));
				}
			}
		} else {
			for (int r = 0; r < rs; r++) {
				for (int c = 0; c < cs; c++) {
					output[r][c] = prepared[r][c * 2];
				}
			}
		}
	}

	public static void unprepareData(float[] prepared, float[][] output, boolean centre) {
		int rs = output.length;
		int cs = output[0].length;

		if (centre) {
			for (int r = 0; r < rs; r++) {
				for (int c = 0; c < cs; c++) {
					output[r][c] = prepared[r * 2 * cs + 2 * c] * (1 - 2 * ((r + c) % 2));
				}
			}
		} else {
			for (int r = 0; r < rs; r++) {
				for (int c = 0; c < cs; c++) {
					output[r][c] = prepared[r * 2 * cs + 2 * c];
				}
			}
		}
	}

	private void process(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();

		phase = new FImage(cs, rs);
		magnitude = new FImage(cs, rs);

		FloatFFT_2D fft = new FloatFFT_2D(rs, cs);
		float[][] prepared = prepareData(image.pixels, rs, cs, centre);

		fft.complexForward(prepared);

		for (int y = 0; y < rs; y++) {
			for (int x = 0; x < cs; x++) {
				float re = prepared[y][x * 2];
				float im = prepared[y][1 + x * 2];

				phase.pixels[y][x] = (float) Math.atan2(im, re);
				magnitude.pixels[y][x] = (float) Math.sqrt(re * re + im * im);
			}
		}
	}

	/**
	 *图像转化
	 */
	public FImage inverse() {
		int cs = magnitude.getCols();
		int rs = magnitude.getRows();

		FloatFFT_2D fft = new FloatFFT_2D(rs, cs);
		float[][] prepared = new float[rs][cs * 2];
		for (int y = 0; y < rs; y++) {
			for (int x = 0; x < cs; x++) {
				float p = phase.pixels[y][x];
				float m = magnitude.pixels[y][x];

				float re = (float) (m * Math.cos(p));
				float im = (float) (m * Math.sin(p));

				prepared[y][x * 2] = re;
				prepared[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(prepared, true);

		FImage image = new FImage(cs, rs);
		unprepareData(prepared, image, centre);

		return image;
	}

	public FImage getLogNormalisedMagnitude() {
		FImage im = magnitude.clone();

		for (int y = 0; y < im.height; y++) {
			for (int x = 0; x < im.width; x++) {
				im.pixels[y][x] = (float) Math.log(im.pixels[y][x] + 1);
			}
		}

		return im.normalise();
	}

	/**
	* 返回解析图像
	 */
	public FImage getPhase() {
		return phase;
	}

	public FImage getMagnitude() {
		return magnitude;
	}

	public boolean isCentre() {
		return centre;
	}

}
