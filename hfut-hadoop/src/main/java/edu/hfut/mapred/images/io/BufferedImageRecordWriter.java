package edu.hfut.mapred.images.io;

import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * BufferedImage图像记录写操作类
 * 
 * @author wanggang
 *
 */
public class BufferedImageRecordWriter extends ImageRecordWriter<BufferedImageWritable> {

	BufferedImageRecordWriter(TaskAttemptContext taskAttemptContext) {
		super(taskAttemptContext);
	}

	@Override
	protected void writeImage(BufferedImageWritable image, FSDataOutputStream imageFile) throws IOException {
		ImageIO.write(image.getImage(), image.getFormat(), imageFile);
	}

}
