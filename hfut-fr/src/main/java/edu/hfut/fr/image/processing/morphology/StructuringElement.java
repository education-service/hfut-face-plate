package edu.hfut.fr.image.processing.morphology;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.pixel.Pixel;

/**
 * 机构化组件
 *
 * @author Jimbo
 */
public class StructuringElement {

	public final static StructuringElement BOX;

	public final static StructuringElement CROSS;

	public final static StructuringElement HPIT;

	static {
		BOX = new StructuringElement();
		BOX.positive.add(new Pixel(-1, -1));
		BOX.positive.add(new Pixel(0, -1));
		BOX.positive.add(new Pixel(1, -1));
		BOX.positive.add(new Pixel(-1, 0));
		BOX.positive.add(new Pixel(0, 0));
		BOX.positive.add(new Pixel(1, 0));
		BOX.positive.add(new Pixel(-1, 1));
		BOX.positive.add(new Pixel(0, 1));
		BOX.positive.add(new Pixel(1, 1));

		CROSS = new StructuringElement();
		CROSS.positive.add(new Pixel(0, -1));
		CROSS.positive.add(new Pixel(-1, 0));
		CROSS.positive.add(new Pixel(0, 0));
		CROSS.positive.add(new Pixel(1, 0));
		CROSS.positive.add(new Pixel(0, 1));

		HPIT = new StructuringElement();
		HPIT.positive.add(new Pixel(-1, 0));
		HPIT.positive.add(new Pixel(1, 0));
	}

	public Set<Pixel> positive = new HashSet<Pixel>();

	public Set<Pixel> negative = new HashSet<Pixel>();

	public StructuringElement() {

	}

	/**
	 * 结构化组件的构造函数
	 */
	public StructuringElement(Set<Pixel> positive, Set<Pixel> negative) {
		if (positive != null)
			this.positive.addAll(positive);
		if (negative != null)
			this.negative.addAll(negative);
	}

	/**
	 * 结构化组件的构造函数
	 */
	public StructuringElement(Pixel[] positive, Pixel[] negative) {
		if (positive != null)
			this.positive.addAll(Arrays.asList(positive));
		if (negative != null)
			this.negative.addAll(Arrays.asList(negative));
	}

	/**
	 * 获取结构话组件的大小
	 */
	public int[] size() {
		int xmin = Integer.MAX_VALUE;
		int xmax = -Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = -Integer.MAX_VALUE;

		for (final Pixel p : positive) {
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}
		for (final Pixel p : negative) {
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}

		return new int[] { 1 + xmax - xmin, 1 + ymax - ymin, xmin, ymin };
	}

	/**
	 * 获取结构化组件
	 */
	public static StructuringElement parseElement(String ele, int cx, int cy) {
		final String[] lines = ele.split("\\n");
		final int height = lines.length;
		final int width = lines[0].length();

		final StructuringElement se = new StructuringElement();

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				final char c = lines[j].charAt(i);

				if (c == '*') {
					se.positive.add(new Pixel(i - cx, j - cy));
				} else if (c == 'o') {
					se.negative.add(new Pixel(i - cx, j - cy));
				}
			}
		}

		return se;
	}

	@Override
	public String toString() {
		final int[] sz = size();
		String s = "";

		for (int j = 0; j < sz[1]; j++) {
			for (int i = 0; i < sz[0]; i++) {
				final Pixel p = new Pixel(i + sz[2], j + sz[3]);

				if (positive.contains(p))
					s += "*";
				else if (negative.contains(p))
					s += "o";
				else
					s += ".";
			}
			s += "\n";
		}

		return s;
	}

	/**
	 * 确定测量位置
	 *
	 */
	public boolean matches(Pixel p, Set<Pixel> pixels) {
		return (intersect(p, pixels).size() == countActive());
	}

	Set<Pixel> intersect(Pixel p, Set<Pixel> pixels) {
		final Set<Pixel> intersect = new HashSet<Pixel>();

		for (final Pixel sep : positive) {
			final Pixel imp = new Pixel(p.x + sep.x, p.y + sep.y);

			if (pixels.contains(imp))
				intersect.add(imp);
		}

		for (final Pixel sep : negative) {
			final Pixel imp = new Pixel(p.x + sep.x, p.y + sep.y);

			if (!pixels.contains(imp))
				intersect.add(imp);
		}

		return intersect;
	}

	/**
	 * 计算结构化组件的像素个数
	 *
	 */
	public int countActive() {
		return positive.size() + negative.size();
	}

	/**
	 * 根据给定的半径建立圆状的结构化组件
	 *
	 *
	 */
	public static StructuringElement disk(int radius) {
		final StructuringElement se = new StructuringElement();
		final int r2 = radius * radius;

		for (int j = -radius; j <= radius; j++) {
			final int j2 = j * j;
			for (int i = -radius; i <= radius; i++) {
				if ((i * i + j2) <= r2) {
					se.positive.add(new Pixel(i, j));
				}
			}
		}

		return se;
	}

}
