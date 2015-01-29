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
 * 腐蚀部分
 *
 * @author Jimbo
 */
public class Erode implements ConnectedComponentProcessor, KernelProcessor<Float, FImage> {

	protected StructuringElement element;
	protected int cx;
	protected int cy;
	protected int sw;
	protected int sh;

	/**
	 * 构造函数
	 */
	public Erode(StructuringElement se) {
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
	public Erode() {
		this(StructuringElement.BOX);
	}

	@Override
	public void process(ConnectedComponent cc) {
		final Set<Pixel> retain = new HashSet<Pixel>();
		final Set<Pixel> pixels = cc.getPixels();
		final int[] se_size = element.size();
		final Rectangle cc_bb = cc.calculateRegularBoundingBox();
		for (int j = (int) (cc_bb.y - se_size[1]); j <= cc_bb.y + se_size[1] + cc_bb.height; j++) {
			for (int i = (int) (cc_bb.x - se_size[0]); i <= cc_bb.x + se_size[0] + cc_bb.width; i++) {
				final Pixel p = new Pixel(i, j);

				if (element.matches(p, pixels)) {
					retain.add(p);
				}
			}
		}

		cc.getPixels().retainAll(retain);
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
		int count = 0;

		for (final Pixel p : element.positive) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 1)
				count++;
		}

		for (final Pixel p : element.negative) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 0)
				count++;
		}

		return (count == element.positive.size() + element.negative.size() ? patch.pixels[cy][cx] : 0);
	}

	/**
	 * 图像腐蚀指定次数
	 *
	 * @param img
	 *            图像
	 * @param times
	 *            腐蚀次数
	 */
	public static void erode(FImage img, int times) {
		final Erode e = new Erode();
		for (int i = 0; i < times; i++)
			img.processInplace(e);
	}

}
