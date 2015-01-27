package edu.hfut.lpr.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import edu.hfut.lpr.analysis.HoughTransformation;
import edu.hfut.lpr.analysis.Photo;

/**
 * Hough变换工具类
 *
 * @author wanggang
 *
 */
public class HoughTool {

	/**
	 * 测试函数
	 */
	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		FileInputStream fis = new FileInputStream(file);
		Photo p = new Photo(fis);
		HoughTransformation hough = p.getHoughTransformation();
		Photo transformed = new Photo(hough.render(HoughTransformation.RENDER_TRANSFORMONLY,
				HoughTransformation.COLOR_HUE));

		transformed.saveImage(args[1]);

		p.close();
		transformed.close();
	}

	public HoughTool() {
		//
	}

}