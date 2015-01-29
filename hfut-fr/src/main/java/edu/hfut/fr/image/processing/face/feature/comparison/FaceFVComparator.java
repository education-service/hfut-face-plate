package edu.hfut.fr.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FVComparator;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.io.IOUtils;

import edu.hfut.fr.image.processing.face.feature.FacialFeature;

/**
 * 人脸特征向量的比较器
 *
 *@author jimbo
 */
public class FaceFVComparator<T extends FacialFeature & FeatureVectorProvider<Q>, Q extends FeatureVector> implements
		FacialFeatureComparator<T> {

	FVComparator<Q> comp;

	/**
	 * 构造函数
	 */
	public FaceFVComparator(FVComparator<Q> comp) {
		this.comp = comp;
	}

	@Override
	public double compare(T query, T target) {
		return comp.compare(query.getFeatureVector(), target.getFeatureVector());
	}

	@Override
	public boolean isDistance() {
		return comp.isDistance();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		comp = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(comp, out);
	}

	@Override
	public String toString() {
		return "FaceFVComparator[distance=" + comp + "]";
	}

}
