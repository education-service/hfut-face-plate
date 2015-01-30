package edu.hfut.lpr.frame;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * 图像文件过滤器
 *
 * 只能处理的图像格式：jpg,bmp,gif,png
 *
 * @author wanggang
 *
 */
public class ImageFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String name = f.getName();
		return accept(name);
	}

	public static boolean accept(String name) {
		int lastIndex = name.lastIndexOf('.');
		if (lastIndex < 0) {
			return false;
		}
		String type = name.substring(lastIndex + 1, name.length()).toLowerCase();
		return type.equals("bmp") || type.equals("jpg") || type.equals("jpeg") || type.equals("png")
				|| type.equals("gif");
	}

	@Override
	public String getDescription() {
		return "images (*.jpg, *.bmp, *.gif, *.png)";
	}

}