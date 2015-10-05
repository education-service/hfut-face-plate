package edu.hfut.fr.no.demo;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 *  图像显示接口
 *
 * @author wanghao
 */
public interface Display {

	public void displayMBF(MBFImage image);

	public void displayF(FImage image);

}
