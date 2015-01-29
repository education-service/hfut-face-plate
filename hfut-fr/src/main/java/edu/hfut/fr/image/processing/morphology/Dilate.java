package edu.hfut.fr.image.processing.morphology;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 膨胀部分
 *
 * @author Jimbo
 */
public class Dilate implements ConnectedComponentProcessor, KernelProcessor<Float, FImage> {

	protected StructuringElement element;
	protected int cx;
	protected int cy;
	protected int sw;
	protected int sh;

	/**
	 * 构造函数
	 */
	public Dilate(StructuringElement se) {
		this.element = se;

		final int[] sz = se.size();
		sw = sz[0];
		sh = sz[1];
		cx = sw / 2;
		cy = sh / 2;
	}

	/**
	 * 构造函数
	 */
	public Dilate() {
		this(StructuringElement.BOX);
	}

	@Override
	public void process(ConnectedComponent cc) {
		final Rectangle cc_bb = cc.calculateRegularBoundingBox();

		final Set<Pixel> newPixels = new HashSet<Pixel>();
		for (int j = (int) (cc_bb.y - sh); j <= cc_bb.y + sh + cc_bb.height; j++) {
			for (int i = (int) (cc_bb.x - sw); i <= cc_bb.x + sw + cc_bb.width; i++) {
				final Pixel p = new Pixel(i, j);

				if (element.intersect(p, cc.getPixels()).size() >= 1) {
					newPixels.add(p);
				}
			}
		}

		cc.getPixels().addAll(newPixels);
	}

	@Override
	public int getKernelHeight() {
		return sh;
	}

	@Override
	public int getKernelWidth() {
		return sw;
	}

	@Override
	public Float processKernel(FImage patch) {
		for (final Pixel p : element.positive) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 1) {
				return 1f;
			}
		}

		for (final Pixel p : element.negative) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 0)
				return 1f;
		}

		return patch.pixels[cy][cx];
	}

	/**
	 * 将图像膨胀指定次数
	 *
	 * @param img
	 *            图像
	 * @param times
	 *            图像的膨胀次数
	 */
	public static void dilate(FImage img, int times) {
		final Dilate d = new Dilate();
		for (int i = 0; i < times; i++)
			img.processInplace(d);
	}

}
