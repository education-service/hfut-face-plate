package edu.hfut.mapred.images.io;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.FSDataInputStream;

import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * BufferedImage图像记录读操作类
 * 
 * Key - NullWritable
 * Value - BufferedImageWritable
 * 
 * @author wanggang
 *
 */
public class BufferedImageRecordReader extends ImageRecordReader<BufferedImageWritable> {

	@Override
	protected BufferedImageWritable createImageWritable() {
		return new BufferedImageWritable();
	}

	@Override
	protected BufferedImageWritable readImage(FSDataInputStream fsDataInputStream) {
		BufferedImageWritable biw = new BufferedImageWritable();
		BufferedImage bi;
		try {
			bi = ImageIO.read(fsDataInputStream);
		} catch (Exception e) {
			bi = null;
		}
		biw.setImage(bi);
		return biw;
	}

}
