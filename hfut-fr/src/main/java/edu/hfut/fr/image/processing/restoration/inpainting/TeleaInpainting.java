package edu.hfut.fr.image.processing.restoration.inpainting;

import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.processing.morphology.StructuringElement;

/**
 * Implementation of Alexandru Telea's FMM-based inpainting algorithm. The
 * {@link AbstractFMMInpainter} is extended with a method to inpaint pixels
 * based on the neighbours and explicitly taking into account the image
 * gradients in the neighbourhood in order to preserve sharp details and smooth
 * zones.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
@Reference(type = ReferenceType.Article, author = { "Telea, Alexandru" }, title = "An Image Inpainting Technique Based on the Fast Marching Method.", year = "2004", journal = "J. Graphics, GPU, & Game Tools", pages = {
		"23", "34" }, url = "http://dblp.uni-trier.de/db/journals/jgtools/jgtools9.html#Telea04", number = "1", volume = "9", customData = {
		"biburl", "http://www.bibsonomy.org/bibtex/2b0bf54e265d011a8e1fe256e6fcf556b/dblp", "doi",
		"http://dx.doi.org/10.1080/10867651.2004.10487596", "keywords", "dblp" })
public class TeleaInpainting<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends AbstractFMMInpainter<IMAGE> {
	protected Set<Pixel> region;

	/**
	 * Construct the inpainting operator with the given radius.
	 *
	 * @param radius
	 *            the radius for selecting how many pixels are used to make
	 *            estimates.
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

			// geometric distance.
			final float geometricDistance = (float) (1. / ((rx * rx + ry * ry) * Math.sqrt((rx * rx + ry * ry))));

			// levelset distance.
			final float levelsetDistance = (float) (1. / (1 + Math.abs(timeMap.pixels[yy][xx] - timeMap.pixels[y][x])));

			// Dot product of final displacement and gradient vectors.
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

			// geometric distance.
			final float geometricDistance = (float) (1. / ((rx * rx + ry * ry) * Math.sqrt((rx * rx + ry * ry))));

			// levelset distance.
			final float levelsetDistance = (float) (1. / (1 + Math.abs(timeMap.pixels[yy][xx] - timeMap.pixels[y][x])));

			// Dot product of final displacement and gradient vectors.
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
