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
 * 击中/击不中变换
 *
 * @author Jimbo
 */
public class HitAndMiss implements ConnectedComponentProcessor, KernelProcessor<Float, FImage> {

	/**
	 * 击中/击不中变换
	 */
	public final static HitAndMiss CONVEX_CORNERS;
	static {
		CONVEX_CORNERS = new HitAndMiss(new StructuringElement(new Pixel[] { new Pixel(0, -1), new Pixel(0, 0),
				new Pixel(1, 0) }, new Pixel[] { new Pixel(-1, 0), new Pixel(-1, 1), new Pixel(0, 1) }),
				new StructuringElement(new Pixel[] { new Pixel(0, -1), new Pixel(0, 0), new Pixel(-1, 0) },
						new Pixel[] { new Pixel(1, 0), new Pixel(1, 1), new Pixel(0, 1) }), new StructuringElement(
						new Pixel[] { new Pixel(-1, 0), new Pixel(0, 0), new Pixel(0, 1) }, new Pixel[] {
								new Pixel(0, -1), new Pixel(1, -1), new Pixel(1, 0) }), new StructuringElement(
						new Pixel[] { new Pixel(0, 1), new Pixel(0, 0), new Pixel(1, 0) }, new Pixel[] {
								new Pixel(-1, -1), new Pixel(0, -1), new Pixel(-1, 0) }));
	}

	protected Set<Pixel> outputPixels = new HashSet<Pixel>();
	protected StructuringElement[] elements;
	protected int cx;
	protected int cy;
	protected int sw = 0;
	protected int sh = 0;

	/**
	 * 构造函数
	 */
	public HitAndMiss(StructuringElement... ses) {
		this.elements = ses;

		for (StructuringElement se : ses) {
			int[] sz = se.size();

			if (sw < sz[0])
				sw = sz[0];
			if (sh < sz[1])
				sh = sz[1];
		}
		cx = sw / 2;
		cy = sw / 2;
	}

	@Override
	public void process(ConnectedComponent cc) {
		outputPixels.clear();

		for (StructuringElement element : elements) {
			Set<Pixel> pixels = cc.getPixels();
			int[] se_size = element.size();
			Rectangle cc_bb = cc.calculateRegularBoundingBox();
			for (int j = (int) (cc_bb.y - se_size[1]); j <= cc_bb.y + se_size[1] + cc_bb.height; j++) {
				for (int i = (int) (cc_bb.x - se_size[0]); i <= cc_bb.x + se_size[0] + cc_bb.width; i++) {
					Pixel p = new Pixel(i, j);

					if (element.matches(p, pixels)) {
						outputPixels.add(p);
					}
				}
			}
		}
	}

	/**
	 * 获取进行击中/击不中变换的像素点
	 */
	public Set<Pixel> getPixels() {
		return outputPixels;
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

		for (StructuringElement element : elements) {
			int count = 0;
			for (Pixel p : element.positive) {
				int px = cx - p.x;
				int py = cy - p.y;
				if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 1)
					count++;
			}

			for (Pixel p : element.negative) {
				int px = cx - p.x;
				int py = cy - p.y;
				if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 0)
					count++;
			}

			if (count == element.positive.size() + element.negative.size())
				return 1f;
		}

		return 0f;
	}

}
