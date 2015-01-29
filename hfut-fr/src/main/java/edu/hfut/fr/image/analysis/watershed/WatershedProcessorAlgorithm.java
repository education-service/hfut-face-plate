package edu.hfut.fr.image.analysis.watershed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.IntValuePixel;

import edu.hfut.fr.image.analysis.watershed.event.ComponentStackMergeListener;
import edu.hfut.fr.image.analysis.watershed.feature.ComponentFeature;

/**
 * 最大稳定外部区域注水算法
 *
 * @author wanghao
 */
@SuppressWarnings("unchecked")
public class WatershedProcessorAlgorithm {

	private class BoundaryHeap {
		private BitSet availablePixels;
		private ArrayDeque<IntValuePixel>[] stacks;

		/**
		 *通过指定的等级构造BoundaryHeap
		 * @param sz
		 */
		public BoundaryHeap(int sz) {
			availablePixels = new BitSet(sz);
			stacks = new ArrayDeque[sz];

			for (int i = 0; i < sz; i++)
				stacks[i] = new ArrayDeque<IntValuePixel>();
		}

		/**
		 *增加heap像素值
		 * @param p
		 */
		public void push(IntValuePixel p) {
			final ArrayDeque<IntValuePixel> l = stacks[p.value];
			l.push(p);
			availablePixels.set(p.value);
		}

		/**
		 *返回最低的像素值
		 */
		public IntValuePixel pop() {
			final int l = availablePixels.nextSetBit(0);
			if (l == -1)
				return null;

			final IntValuePixel xx = this.stacks[l].pop();
			if (this.stacks[l].size() == 0)
				availablePixels.set(l, false);
			return xx; // lowest and newest pixel
		}
	}

	/** 浇注开始的像素点t */
	private IntValuePixel startPixel = null;

	/** 记录被访问的像素点 */
	private BitSet accessibleMask = null;

	/** 当前被访问的像素点*/
	private IntValuePixel currentPixel = null;

	/** 处理的过程栈 */
	private ArrayDeque<Component> componentStack = null;

	/** 处理的边界堆 */
	private BoundaryHeap boundaryHeap = null;

	/** 被处理的图像 */
	private int[][] greyscaleImage = null;

	/**
	 * 浇注过程的标志
	 */
	private List<ComponentStackMergeListener> csmListeners = null;

	private Class<? extends ComponentFeature>[] featureClasses;

	/**
	 *  浇注算法的构造函数
	 */
	public WatershedProcessorAlgorithm(int[][] greyscaleImage, IntValuePixel startPixel,
			Class<? extends ComponentFeature>... featureClasses) {
		this.greyscaleImage = greyscaleImage;
		this.startPixel = startPixel;
		this.csmListeners = new ArrayList<ComponentStackMergeListener>();

		this.featureClasses = featureClasses;
	}

	/**
	 * 浇注算法的构造函数 :
	 */
	public WatershedProcessorAlgorithm(FImage bGreyscaleImage, IntValuePixel startPixel,
			Class<? extends ComponentFeature>... featureClasses) {
		this(new int[bGreyscaleImage.getHeight()][bGreyscaleImage.getWidth()], startPixel, featureClasses);

		for (int j = 0; j < bGreyscaleImage.getHeight(); j++) {
			for (int i = 0; i < bGreyscaleImage.getWidth(); i++) {
				greyscaleImage[j][i] = (int) (bGreyscaleImage.pixels[j][i] * 255);
			}
		}
	}

	/**
	 *开始浇注
	 */
	public void startPour() {

		this.currentPixel = startPixel;

		this.currentPixel.value = greyscaleImage[this.startPixel.y][this.startPixel.x];

		this.accessibleMask = new BitSet(this.greyscaleImage.length * this.greyscaleImage[0].length);

		this.componentStack = new ArrayDeque<Component>();

		this.boundaryHeap = new BoundaryHeap(256);

		final Component dummyComponent = new Component(new IntValuePixel(-1, -1, Integer.MAX_VALUE), featureClasses);
		this.componentStack.push(dummyComponent);

		this.processNeighbours();

	}

	/**
	* 处理当前像素点周围4个的像素
	 */
	private void processNeighbours() {

		Component currentComponent = new Component(this.currentPixel, featureClasses);

		componentStack.push(currentComponent);

		final boolean processNeighbours = true;
		while (processNeighbours) {
			boolean toContinue = false;
			final IntValuePixel[] neighbours = getNeighbourPixels_4(this.currentPixel);
			for (final IntValuePixel neighbour : neighbours) {
				if (neighbour == null)
					break;
				final int idx = neighbour.x + neighbour.y * this.greyscaleImage[0].length;

				if (!this.accessibleMask.get(idx)) {
					this.accessibleMask.set(idx);

					if (neighbour.value >= currentPixel.value) {
						this.boundaryHeap.push(neighbour);
					} else {
						this.boundaryHeap.push(currentPixel);
						this.currentPixel = neighbour;
						currentComponent = new Component(this.currentPixel, featureClasses);
						componentStack.push(currentComponent);
						toContinue = true;
						break;
					}
				}
			}

			if (toContinue)
				continue;

			this.componentStack.peek().accumulate(this.currentPixel);

			final IntValuePixel p = this.boundaryHeap.pop();

			if (p == null)
				return;

			if (p.value == currentPixel.value) {
				this.currentPixel = p;
			} else {
				this.currentPixel = p;
				processComponentStack();
			}
		}
	}

	private void processComponentStack() {
		while (this.currentPixel.value > this.componentStack.peek().pivot.value) {
			final Component topOfStack = this.componentStack.pop();

			if (this.currentPixel.value < this.componentStack.peek().pivot.value) {
				topOfStack.pivot = this.currentPixel;
				this.componentStack.push(topOfStack);

				fireComponentStackMergeListener(componentStack.peek());

				return;
			}

			fireComponentStackMergeListener(componentStack.peek(), topOfStack);

			this.componentStack.peek().merge(topOfStack);

		}
	}

	/**
	 *获得领域四个像素值
	 */
	private IntValuePixel[] getNeighbourPixels_4(IntValuePixel pixel) {
		final IntValuePixel[] p = new IntValuePixel[4];
		final int x = pixel.x;
		final int y = pixel.y;

		final int height = this.greyscaleImage.length;
		final int width = this.greyscaleImage[0].length;

		int c = 0;

		if (x < width - 1)
			p[c++] = new IntValuePixel(x + 1, y, greyscaleImage[y][x + 1]);

		if (x > 0)
			p[c++] = new IntValuePixel(x - 1, y, greyscaleImage[y][x - 1]);

		if (y < height - 1)
			p[c++] = new IntValuePixel(x, y + 1, greyscaleImage[y + 1][x]);

		if (y > 0)
			p[c++] = new IntValuePixel(x, y - 1, greyscaleImage[y - 1][x]);

		return p;
	}

	/**
	 *咱家组建栈
	 */
	public void addComponentStackMergeListener(ComponentStackMergeListener csml) {
		csmListeners.add(csml);
	}

	public void removeComponentStackMergeListener(ComponentStackMergeListener csml) {
		csmListeners.remove(csml);
	}

	private void fireComponentStackMergeListener(Component c1, Component c2) {
		for (final ComponentStackMergeListener csm : csmListeners)
			csm.componentsMerged(c1, c2);
	}

	private void fireComponentStackMergeListener(Component c1) {
		for (final ComponentStackMergeListener csm : csmListeners)
			csm.componentPromoted(c1);
	}

	/**
	 *输出帮助函数
	 */
	@SuppressWarnings("unused")
	private String outputArray(Object[] o) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (final Object obj : o) {
			if (!first)
				sb.append(",");
			if (obj == null)
				sb.append("null");
			else
				sb.append(obj.toString());
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

}
