package edu.hfut.fr.image.processing.convolution.filterbank;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import edu.hfut.fr.image.processing.algorithm.FourierTransform;
import edu.hfut.fr.image.processing.convolution.FConvolution;

/**
 * 过滤器组抽象类
 *
 *@author wanghao
 */
public abstract class FilterBank implements ImageAnalyser<FImage> {

	private FConvolution[] filters;
	protected FImage[] responses;

	private FloatFFT_2D fft;
	private float[][][] preparedFilters;
	private float[][] tmpImage;
	private int paddingX;
	private int paddingY;

	protected FilterBank(FConvolution[] filters) {
		this.filters = filters;

		int maxWidth = 0;
		int maxHeight = 0;
		for (int i = 0; i < filters.length; i++) {
			maxWidth = Math.max(maxWidth, filters[i].kernel.width);
			maxHeight = Math.max(maxHeight, filters[i].kernel.height);
		}
		this.paddingX = (int) Math.ceil(maxWidth / 2);
		this.paddingY = (int) Math.ceil(maxHeight / 2);
	}

	@Override
	public void analyseImage(FImage in) {
		responses = new FImage[filters.length];

		final FImage image = in.padding(paddingX, paddingY);
		final int cols = image.getCols();
		final int rows = image.getRows();

		if (fft == null || preparedFilters == null || preparedFilters[0].length != rows
				|| preparedFilters[0][0].length != 2 * cols) {
			fft = new FloatFFT_2D(rows, cols);
			preparedFilters = new float[filters.length][][];
			tmpImage = new float[rows][cols * 2];

			for (int i = 0; i < preparedFilters.length; i++) {
				final float[][] preparedKernel = FourierTransform.prepareData(filters[i].kernel, rows, cols, false);
				fft.complexForward(preparedKernel);
				preparedFilters[i] = preparedKernel;
			}
		}

		final float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		for (int i = 0; i < preparedFilters.length; i++) {
			responses[i] = convolve(cols, rows, preparedImage, preparedFilters[i]);
			responses[i] = responses[i].extractROI(2 * paddingX, 2 * paddingY, responses[i].width - 2 * paddingX,
					responses[i].height - 2 * paddingY);
		}
	}

	private FImage convolve(final int cols, final int rows, final float[][] preparedImage,
			final float[][] preparedFilter) {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedFilter[y][x * 2];
				final float imKernel = preparedFilter[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				tmpImage[y][x * 2] = re;
				tmpImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(tmpImage, true);

		final FImage out = new FImage(cols, rows);
		FourierTransform.unprepareData(tmpImage, out, false);
		return out;
	}

	/**
	 * 返回分析后的图像
	 */
	public FImage[] getResponseImages() {
		return responses;
	}

	public float[] getResponse(int x, int y) {
		final float[] response = new float[responses.length];

		for (int i = 0; i < response.length; i++)
			response[i] = responses[i].getPixelNative(x, y);

		return response;
	}

	public FloatFV getResponseFV(int x, int y) {
		return new FloatFV(getResponse(x, y));
	}

	public FImage renderFilters(int numFiltersX) {
		final int border = 4;
		final int numFiltersY = (int) Math.ceil((double) filters.length / numFiltersX);
		final int w = (border + filters[0].kernel.width);
		final int width = w * (numFiltersX) + border;
		final int h = (border + filters[0].kernel.height);
		final int height = h * (numFiltersY) + border;

		final FImage image = new FImage(width, height);
		image.fill(1f);

		int count = 0;
		for (int j = 0; j < numFiltersY; j++)
			for (int i = 0; i < numFiltersX && count < filters.length; i++)
				image.drawImage(filters[count++].kernel.clone().normalise(), w * i + border, h * j + border);

		return image;
	}

	public float[][] getResponses() {
		final int width = this.responses[0].width;
		final int height = this.responses[0].height;

		final float[][] resp = new float[width * height][this.responses.length];

		for (int i = 0; i < responses.length; i++) {
			for (int y = 0; y < responses[0].height; y++) {
				for (int x = 0; x < responses[0].width; x++) {
					resp[x + width * y][i] = responses[i].pixels[y][x];
				}
			}
		}

		return resp;
	}

}
