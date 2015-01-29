package edu.hfut.fr.image.objectdetection;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 检测对象监视器借口
 *
 * @author wanghao
 */
public interface ObjectDetector<IMAGE extends Image<?, IMAGE>, DETECTED_OBJECT> {

	/**
	 * 检测对象，返回detected_object对象队列
	 */
	public List<DETECTED_OBJECT> detect(IMAGE image);

	public void setROI(Rectangle roi);

}
