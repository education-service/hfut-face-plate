package edu.hfut.fr.image.objectdetection.filtering;

import java.util.List;

/**
 * 识别队列
 *
 * @author wanghao
 */
public final class IdentityFilter<T> implements DetectionFilter<T, T> {

	@Override
	public List<T> apply(List<T> input) {
		return input;
	}

}
