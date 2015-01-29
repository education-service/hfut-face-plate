package edu.hfut.fr.image.objectdetection.filtering;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Shape;

/**
 * 最大检测对象类
 *
 * @author wanghao
 */
public class MaxSizeFilter<T extends Shape> implements DetectionFilter<T, T> {

	@Override
	public List<T> apply(List<T> input) {
		T shape = input.get(0);
		double maxSize = shape.calculateArea();

		for (int i = 1; i < input.size(); i++) {
			final T s = input.get(i);
			final double size = s.calculateArea();

			if (size > maxSize) {
				maxSize = size;
				shape = s;
			}
		}

		final List<T> ret = new ArrayList<T>(1);
		ret.add(shape);
		return ret;
	}

}
