package edu.hfut.fr.image.processing.restoration.inpainting;

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.processing.morphology.StructuringElement;

/**
 * 基于 FMM-based 的图像修复算法
 *
 * @author wanghao
 */
public class TeleaInpainting<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends AbstractFMMInpainter<IMAGE> {

	protected Set<Pixel> region;

	/**
	 * 构造函数
	 */
	public TeleaInpainting(int radius) {
		region = StructuringElement.disk(radius).positive;
	}

	@Override
	protected void inpaint(int x, int y, IMAGE image) {
		if (image instanceof FImage)
			inpaint(x, y, (FImage) image);
		else if (image instanceof MBFImage)
			inpaint(x, y, (MBFImage) image);
		else
			throw new UnsupportedOperationException("Image type not supported!");
	}

	protected void inpaint(int x, int y, FImage input) {
		final int width = input.getWidth();
		final int height = input.getHeight();
		final float gradx_u = gradX(timeMap.pixels, x, y);
		final float grady_u = gradY(timeMap.pixels, x, y);

		float accum = 0;
		float norm = 0;

		for (final Pixel p : region) {
			final int xx = p.x + x;
			final int yy = p.y + y;

			if (xx <= 1 || xx >= width - 1 || yy <= 1 || yy >= height - 1)
				continue;
			if (flag[yy][xx] != KNOWN)
				continue;

			final int rx = x - xx;
			final int ry = y - yy;

			final float geometricDistance = (float) (1. / ((rx * rx + ry * ry) * Math.sqrt((rx * rx + ry * ry))));

			final float levelsetDistance = (float) (1. / (1 + Math.abs(timeMap.pixels[yy][xx] - timeMap.pixels[y][x])));

			float direction = Math.abs(rx * gradx_u + ry * grady_u);
			if (direction < 0.000001f)
				direction = 0.000001f;

			final float weight = geometricDistance * levelsetDistance * direction;

			accum += weight * input.pixels[yy][xx];
			norm += weight;
		}

		input.pixels[y][x] = accum / norm;
	}

	protected void inpaint(int x, int y, MBFImage input) {
		final int width = input.getWidth();
		final int height = input.getHeight();
		final float gradx_u = gradX(timeMap.pixels, x, y);
		final float grady_u = gradY(timeMap.pixels, x, y);

		final int nbands = input.numBands();
		final float accum[] = new float[nbands];
		float norm = 0;

		for (final Pixel p : region) {
			final int xx = p.x + x;
			final int yy = p.y + y;

			if (xx <= 1 || xx >= width - 1 || yy <= 1 || yy >= height - 1)
				continue;
			if (flag[yy][xx] != KNOWN)
				continue;

			final int rx = x - xx;
			final int ry = y - yy;

			final float geometricDistance = (float) (1. / ((rx * rx + ry * ry) * Math.sqrt((rx * rx + ry * ry))));

			final float levelsetDistance = (float) (1. / (1 + Math.abs(timeMap.pixels[yy][xx] - timeMap.pixels[y][x])));

			float direction = Math.abs(rx * gradx_u + ry * grady_u);
			if (direction < 0.000001f)
				direction = 0.000001f;

			final float weight = geometricDistance * levelsetDistance * direction;

			for (int i = 0; i < nbands; i++)
				accum[i] += weight * input.getBand(i).pixels[yy][xx];
			norm += weight;
		}

		for (int i = 0; i < nbands; i++)
			input.getBand(i).pixels[y][x] = accum[i] / norm;
	}

	private float gradX(float[][] img, int x, int y) {
		float grad;

		if (flag[y][x + 1] != UNKNOWN) {
			if (flag[y][x - 1] != UNKNOWN)
				grad = (img[y][x + 1] - img[y][x - 1]) * 0.5f;
			else
				grad = (img[y][x + 1] - img[y][x]);
		} else {
			if (flag[y][x - 1] != UNKNOWN)
				grad = (img[y][x] - img[y][x - 1]);
			else
				grad = 0;
		}

		return grad;
	}

	private float gradY(float[][] img, int x, int y) {
		float grad;

		if (flag[y + 1][x] != UNKNOWN) {
			if (flag[y - 1][x] != UNKNOWN)
				grad = (img[y + 1][x] - img[y - 1][x]) * 0.5f;
			else
				grad = (img[y + 1][x] - img[y][x]);
		} else {
			if (flag[y - 1][x] != UNKNOWN)
				grad = (img[y][x] - img[y - 1][x]);
			else
				grad = 0;
		}

		return grad;
	}

}
