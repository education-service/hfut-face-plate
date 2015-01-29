package edu.hfut.fr.image.analysis.algorithm;

import java.util.LinkedHashSet;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * FloodFill实现类
 *
 * @author wanggang
 */
public class FloodFill<I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> implements
		ImageAnalyser<I> {

	FImage flooded;
	Pixel startPixel;
	float threshold;

	/**
	 * 构造函数
	 */
	public FloodFill(int x, int y, float threshold) {
		this.startPixel = new Pixel(x, y);
		this.threshold = threshold;
	}

	public FloodFill(Pixel startPixel, float threshold) {
		this.startPixel = startPixel;
		this.threshold = threshold;
	}

	@Override
	public void analyseImage(I image) {
		flooded = floodFill((Image<?, ?>) image, startPixel, threshold);
	}

	public FImage getFlooded() {
		return flooded;
	}

	protected static <T> boolean accept(Image<T, ?> image, Pixel n, T initial, float threshold) {
		if (image instanceof FImage) {
			return Math.abs((Float) initial - (Float) image.getPixel(n.x, n.y)) < threshold;
		} else if (image instanceof MBFImage) {
			Float[] finit = (Float[]) initial;
			Float[] fpix = (Float[]) image.getPixel(n.x, n.y);
			float accum = 0;

			for (int i = 0; i < finit.length; i++)
				accum += (finit[i] - fpix[i]) * (finit[i] - fpix[i]);

			return Math.sqrt(accum) < threshold;
		} else {
			throw new RuntimeException("unsupported image type");
		}
	}

	public static <T> FImage floodFill(Image<T, ?> image, int startx, int starty, float threshold) {
		return floodFill(image, new Pixel(startx, starty), threshold);
	}

	public static <T> FImage floodFill(Image<T, ?> image, Pixel start, float threshold) {
		FImage output = new FImage(image.getWidth(), image.getHeight());

		//		设定queue为空队列
		LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();

		T initial = image.getPixel(start.x, start.y);

		//		将节点加入队列中
		queue.add(start);

		//		 对队列中每个元素进行处理
		while (queue.size() > 0) {
			//Pixel n = queue.poll();
			Pixel n = queue.iterator().next();
			queue.remove(n);

			//			颜色与目标颜色能够匹配
			if (accept(image, n, initial, threshold)) {
				int e = n.x, w = n.x;
				while (w > 0 && accept(image, new Pixel(w - 1, n.y), initial, threshold))
					w--;

				while (e < image.getWidth() - 1 && accept(image, new Pixel(e + 1, n.y), initial, threshold))
					e++;

				for (int i = w; i <= e; i++) {
					output.pixels[n.y][i] = 1;

					int north = n.y - 1;
					int south = n.y + 1;
					if (north >= 0 && accept(image, new Pixel(i, north), initial, threshold)
							&& output.pixels[north][i] != 1)
						queue.add(new Pixel(i, north));
					if (south < image.getHeight() && accept(image, new Pixel(i, south), initial, threshold)
							&& output.pixels[south][i] != 1)
						queue.add(new Pixel(i, south));
				}
				//			持续循环直到队列为空
			}
		}
		//			返回
		return output;
	}

}
