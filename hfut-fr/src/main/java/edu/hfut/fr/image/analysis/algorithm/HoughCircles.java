package edu.hfut.fr.image.analysis.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntFloatProcedure;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.util.queue.BoundedPriorityQueue;

/**
 * 圆的霍夫变换拟合的实现
 *
 * @author wanghao
 */
public class HoughCircles implements ImageAnalyser<FImage> {

	Logger logger = Logger.getLogger(HoughCircles.class);

	/**
	 * 带有权重的圆
	 *
	 */
	public static class WeightedCircle extends Circle implements Comparable<WeightedCircle> {
		/**
		 * The weight
		 */
		public float weight;

		/**
		 * 通过给定的几何参数和权重来构建圆
		 */
		public WeightedCircle(float x, float y, float radius, float weight) {
			super(x, y, radius);
			this.weight = weight;
		}

		@Override
		public int compareTo(WeightedCircle o) {
			return Float.compare(o.weight, this.weight);
		}
	}

	protected int minRad;
	protected int maxRad;
	protected TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>> radmap;
	private float[][] cosanglemap;
	private float[][] sinanglemap;
	private int nRadius;
	private int nDegree;
	private int radIncr;

	/**
	 * 通过给定的参数进行构造
	 */
	public HoughCircles(int minRad, int maxRad, int radIncrement, int nDegree) {
		super();
		this.minRad = minRad;
		if (this.minRad <= 0)
			this.minRad = 1;
		this.maxRad = maxRad;
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		this.radIncr = radIncrement;
		this.nRadius = (maxRad - minRad) / this.radIncr;
		this.nDegree = nDegree;
		this.cosanglemap = new float[nRadius][nDegree];
		this.sinanglemap = new float[nRadius][nDegree];
		for (int radIndex = 0; radIndex < this.nRadius; radIndex++) {
			for (int angIndex = 0; angIndex < nDegree; angIndex++) {
				final double ang = angIndex * (2 * PI / nDegree);
				final double rad = minRad + (radIndex * this.radIncr);
				this.cosanglemap[radIndex][angIndex] = (float) (rad * cos(ang));
				this.sinanglemap[radIndex][angIndex] = (float) (rad * sin(ang));
			}
		}
	}

	@Override
	public void analyseImage(FImage image) {
		final int height = image.getHeight();
		final int width = image.getWidth();
		this.radmap = new TIntObjectHashMap<TIntObjectHashMap<TIntFloatHashMap>>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.pixels[y][x] == 1) {
					for (int rad = 0; rad < nRadius; rad++) {
						final int actualrad = (rad * this.radIncr) + this.minRad;
						final float radiusWeight = 1f / this.nDegree;
						for (int ang = 0; ang < nDegree; ang++) {
							final int x0 = round(x + this.cosanglemap[rad][ang]);
							final int y0 = round(y + this.sinanglemap[rad][ang]);

							TIntObjectHashMap<TIntFloatHashMap> xMap = this.radmap.get(actualrad);
							if (xMap == null) {
								this.radmap.put(actualrad, xMap = new TIntObjectHashMap<TIntFloatHashMap>());
							}
							TIntFloatHashMap yMap = xMap.get(x0);
							if (yMap == null) {
								xMap.put(x0, yMap = new TIntFloatHashMap());
							}
							yMap.adjustOrPutValue(y0, radiusWeight, radiusWeight);
						}
					}
				}
			}
		}
		logger.debug("Done analysing the image!");
	}

	/**
	 * 得到N个执行后最好结果的圆
	 *
	 */
	public List<WeightedCircle> getBest(int n) {
		final BoundedPriorityQueue<WeightedCircle> bpq = new BoundedPriorityQueue<WeightedCircle>(n);
		this.radmap.forEachEntry(new TIntObjectProcedure<TIntObjectHashMap<TIntFloatHashMap>>() {

			@Override
			public boolean execute(final int radius, TIntObjectHashMap<TIntFloatHashMap> b) {
				b.forEachEntry(new TIntObjectProcedure<TIntFloatHashMap>() {

					@Override
					public boolean execute(final int x, TIntFloatHashMap b) {
						b.forEachEntry(new TIntFloatProcedure() {

							@Override
							public boolean execute(int y, float weightedCount) {
								bpq.offer(new WeightedCircle(x, y, radius, weightedCount));
								return true;
							}
						});
						return true;
					}
				});
				return true;
			}
		});

		return bpq.toOrderedList();
	}

}
