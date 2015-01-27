package edu.hfut.mapred.images.io;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.openimaj.image.ImageUtilities;

import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 灰度图像写操作类
 *
 * @author wanggang
 *
 */
public class GrayImageRecordWriter extends ImageRecordWriter<GrayImageWritable> {

	GrayImageRecordWriter(TaskAttemptContext taskAttemptContext) {
		super(taskAttemptContext);
	}

	@Override
	protected void writeImage(GrayImageWritable image, FSDataOutputStream imageFile) throws IOException {
		ImageUtilities.write(image.getImage(), image.getFormat(), (OutputStream) imageFile);
	}

}
