package edu.hfut.mapred.images.io;

import org.apache.hadoop.fs.FSDataInputStream;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import edu.hfut.mapred.images.writable.ColorImageWritable;

/**
 * 彩色图像记录读操作类
 *
 * @author wanggang
 *
 */
public class ColorImageRecordReader extends ImageRecordReader<ColorImageWritable> {

	@Override
	protected ColorImageWritable createImageWritable() {
		return new ColorImageWritable();
	}

	@Override
	protected ColorImageWritable readImage(FSDataInputStream fsDataInputStream) {
		ColorImageWritable mbfiw = new ColorImageWritable();
		MBFImage mbfi;
		try {
			mbfi = ImageUtilities.readMBF(fsDataInputStream);
		} catch (Exception e) {
			mbfi = null;
		}
		mbfiw.setImage(mbfi);
		return mbfiw;
	}

}
