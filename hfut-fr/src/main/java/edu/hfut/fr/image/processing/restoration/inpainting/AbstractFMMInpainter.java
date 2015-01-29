package edu.hfut.fr.image.processing.restoration.inpainting;

import java.util.PriorityQueue;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.hfut.fr.image.processing.morphology.Dilate;
import edu.hfut.fr.image.processing.morphology.StructuringElement;

/**
 * 基于FMM算法的图像修复抽象
 *
 * @author wanghao
 */
@SuppressWarnings("javadoc")
@References(references = {
		@Reference(type = ReferenceType.Article, author = { "Telea, Alexandru" }, title = "An Image Inpainting Technique Based on the Fast Marching Method.", year = "2004", journal = "J. Graphics, GPU, & Game Tools", pages = {
				"23", "34" }, url = "http://dblp.uni-trier.de/db/journals/jgtools/jgtools9.html#Telea04", number = "1", volume = "9", customData = {
				"biburl", "http://www.bibsonomy.org/bibtex/2b0bf54e265d011a8e1fe256e6fcf556b/dblp", "ee",
				"http://dx.doi.org/10.1080/10867651.2004.10487596", "keywords", "dblp" }),
		@Reference(type = ReferenceType.Inproceedings, author = { "J. A. Sethian" }, title = "A Fast Marching Level Set Method for Monotonically Advancing Fronts", year = "1995", booktitle = "Proc. Nat. Acad. Sci", pages = {
				"1591", "", "1595" }) })
public abstract class AbstractFMMInpainter<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends AbstractImageMaskInpainter<IMAGE> {

	private static final int[][] DELTAS = new int[][] { { 0, -1 }, { -1, 0 }, { 0, 1 }, { 1, 0 } };

	protected static byte KNOWN = 0;

	protected static byte BAND = 1;

	protected static byte UNKNOWN = 2;

	protected byte[][] flag;

	protected FImage timeMap;

	protected PriorityQueue<FValuePixel> heap;

	@Override
	protected void initMask() {
		final FImage outside = mask.process(new Dilate(StructuringElement.CROSS), true);

		flag = new byte[mask.height][mask.width];
		timeMap = new FImage(outside.width, outside.height);

		heap = new PriorityQueue<FValuePixel>(10, FValuePixel.ValueComparator.INSTANCE);

		for (int y = 0; y < mask.height; y++) {
			for (int x = 0; x < mask.width; x++) {
				final int band = (int) (outside.pixels[y][x] - mask.pixels[y][x]);
				flag[y][x] = (byte) ((2 * outside.pixels[y][x]) - band);

				if (flag[y][x] == UNKNOWN)
					timeMap.pixels[y][x] = Float.MAX_VALUE;

				if (band != 0) {
					heap.add(new FValuePixel(x, y, timeMap.pixels[y][x]));
				}
			}
		}
	}

	/**
	 * 解函数步骤
	 */
	protected float solveEikonalStep(int x1, int y1, int x2, int y2) {
		float soln = Float.MAX_VALUE;

		final float t1 = timeMap.pixels[y1][x1];
		final float t2 = timeMap.pixels[y2][x2];

		if (flag[y1][x1] == KNOWN) {
			if (flag[y2][x2] == KNOWN) {
				final float r = (float) Math.sqrt(2 - (t1 - t2) * (t1 - t2));
				float s = (t1 + t2 - r) * 0.5f;

				if (s >= t1 && s >= t2) {
					soln = s;
				} else {
					s += r;

					if (s >= t1 && s >= t2) {
						soln = s;
					}
				}
			} else {
				soln = 1 + t1;
			}
		} else if (flag[y2][x2] == KNOWN) {
			soln = 1 + t2;
		}

		return soln;
	}

	@Override
	public void performInpainting(IMAGE image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		while (!heap.isEmpty()) {
			final FValuePixel pix = heap.poll();
			final int x = pix.x;
			final int y = pix.y;
			flag[y][x] = KNOWN;

			if ((x <= 1) || (y <= 1) || (x >= width - 2) || (y >= height - 2))
				continue;

			for (final int[] p : DELTAS) {
				final int xp = p[0] + x, yp = p[1] + y;

				if (flag[yp][xp] != KNOWN) {
					timeMap.pixels[yp][xp] = Math
							.min(Math.min(
									Math.min(solveEikonalStep(xp - 1, yp, xp, yp - 1),
											solveEikonalStep(xp + 1, yp, xp, yp - 1)),
									solveEikonalStep(xp - 1, yp, xp, yp + 1)), solveEikonalStep(xp + 1, yp, xp, yp + 1));

					if (flag[yp][xp] == UNKNOWN) {
						flag[yp][xp] = BAND;

						heap.offer(new FValuePixel(xp, yp, timeMap.pixels[yp][xp]));

						inpaint(xp, yp, image);
					}
				}
			}
		}
	}

	/**
	 * 修复图像指定像素点
	 */
	protected abstract void inpaint(int x, int y, IMAGE image);

}
