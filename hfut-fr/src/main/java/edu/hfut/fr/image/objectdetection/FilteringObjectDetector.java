package edu.hfut.fr.image.objectdetection;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

import edu.hfut.fr.image.objectdetection.filtering.DetectionFilter;

/**
 * 过滤对象检测器
 *
 * @author wanghao
 */
public class FilteringObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT, FILTERED_OBJECT> implements
		ObjectDetector<IMAGE, FILTERED_OBJECT> {

	private ObjectDetector<IMAGE, DETECTED_OBJECT> detector;
	private DetectionFilter<DETECTED_OBJECT, FILTERED_OBJECT> filter;

	/**
	 *　构造函数
	 */
	public FilteringObjectDetector(ObjectDetector<IMAGE, DETECTED_OBJECT> detector,
			DetectionFilter<DETECTED_OBJECT, FILTERED_OBJECT> filter) {
		super();
		this.detector = detector;
		this.filter = filter;
	}

	@Override
	public List<FILTERED_OBJECT> detect(IMAGE image) {
		return filter.apply(detector.detect(image));
	}

	@Override
	public void setROI(Rectangle roi) {
		detector.setROI(roi);
	}

}
