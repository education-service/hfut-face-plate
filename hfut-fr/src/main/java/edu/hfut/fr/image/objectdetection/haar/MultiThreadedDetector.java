package edu.hfut.fr.image.objectdetection.haar;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.Parallel.IntRange;

import edu.hfut.fr.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * 多线程检测器
 *
 * @author wanghao
 */
public class MultiThreadedDetector extends Detector {

	private ThreadPoolExecutor threadPool;

	/**
	 * 构造函数
	 */
	public MultiThreadedDetector(StageTreeClassifier cascade, float scaleFactor, int smallStep, int bigStep,
			ThreadPoolExecutor threadPool) {
		super(cascade, scaleFactor, smallStep, bigStep);

		if (threadPool == null)
			threadPool = GlobalExecutorPool.getPool();

		this.threadPool = threadPool;
	}

	public MultiThreadedDetector(StageTreeClassifier cascade, float scaleFactor) {
		this(cascade, scaleFactor, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP, null);
	}

	public MultiThreadedDetector(StageTreeClassifier cascade) {
		this(cascade, DEFAULT_SCALE_FACTOR, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP, null);
	}

	@Override
	protected void detectAtScale(final SummedSqTiltAreaTable sat, final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight,
			final List<Rectangle> results) {
		Parallel.forRange(startY, stopY, 1, new Operation<IntRange>() {
			@Override
			public void perform(IntRange range) {
				for (int iy = range.start; iy < range.stop; iy += range.incr) {
					final int y = Math.round(iy * ystep);

					for (int ix = startX, xstep = 0; ix < stopX; ix += xstep) {
						final int x = Math.round(ix * ystep);

						final int result = cascade.classify(sat, x, y);

						if (result > 0) {
							synchronized (results) {
								results.add(new Rectangle(x, y, windowWidth, windowHeight));
							}
						}

						xstep = result == 0 ? smallStep : bigStep;
					}
				}
			}
		}, threadPool);
	}

}
