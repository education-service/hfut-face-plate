package edu.hfut.fr.image.analysis.watershed;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.pixel.IntValuePixel;
import org.openimaj.image.pixel.Pixel;

import edu.hfut.fr.image.analysis.watershed.feature.ComponentFeature;
import edu.hfut.fr.image.analysis.watershed.feature.PixelsFeature;

/**
 * 显示区域像素值
 *
 * @author wanghao
 */
public class Component implements Cloneable {

	/** 是否为MSER */
	public boolean isMSER = false;

	/** 特征列表 */
	public ComponentFeature[] features;

	public IntValuePixel pivot;
	private int size = 0;

	/**
	 * 默认构造函数
	 */
	@SuppressWarnings("unchecked")
	public Component(IntValuePixel p, Class<? extends ComponentFeature>... featureClasses) {
		this.pivot = p;

		features = new ComponentFeature[featureClasses.length];
		for (int i = 0; i < featureClasses.length; i++) {
			try {
				features[i] = featureClasses[i].newInstance();
			} catch (final Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	@Override
	public String toString() {
		return "Comp@" + super.hashCode() + "(px:" + size + ",gl:" + pivot.value + ")";
	};

	/**
	 *增加像素值
	 */
	public void accumulate(IntValuePixel p) {
		size++;

		for (final ComponentFeature f : features) {
			f.addSample(p);
		}
	}

	/**
	 *合并区域
	 */
	public void merge(Component p) {
		size += p.size();

		for (int i = 0; i < features.length; i++) {
			features[i].merge(p.features[i]);
		}
	}

	/**
	 *区域大小
	 */
	public int size() {
		return size;
	}

	/**
	 *获得区域像素值
	 */
	public Set<Pixel> getPixels() {
		for (final ComponentFeature f : features) {
			if (f instanceof PixelsFeature)
				return ((PixelsFeature) f).pixels;
		}

		final Set<Pixel> pix = new HashSet<Pixel>(1);
		pix.add(pivot);
		return pix;
	}

	@Override
	public Component clone() {
		Component result;
		try {
			result = (Component) super.clone();
			result.features = new ComponentFeature[features.length];
			for (int i = 0; i < features.length; i++)
				result.features[i] = features[i].clone();

			return result;
		} catch (final CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 *获得特征
	 */
	public ComponentFeature getFeature(int index) {
		if (index >= features.length)
			return null;

		return features[index];
	}

	/**
	 *获得特征匹配
	 */
	@SuppressWarnings("unchecked")
	public <T extends ComponentFeature> T getFeature(Class<T> featureClass) {
		for (final ComponentFeature f : features)
			if (f.getClass().isAssignableFrom(featureClass))
				return (T) f;

		return null;
	}

}
