package edu.hfut.fr.image.processing.face.feature.comparison;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.comparator.DistanceComparator;

import edu.hfut.fr.image.processing.face.feature.FacialFeature;

/**
 * 比较器的接口
 *
 * @author jimbo
 */
public interface FacialFeatureComparator<T extends FacialFeature> extends DistanceComparator<T>, ReadWriteableBinary {

}
