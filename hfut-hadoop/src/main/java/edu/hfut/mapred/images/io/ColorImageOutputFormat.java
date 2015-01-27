package edu.hfut.mapred.images.io;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.hfut.mapred.images.writable.ColorImageWritable;

/**
 * 彩色图像输出格式
 * 写入到HDFS中，每个图像作为单独的文件写入，并且包含图像的文件名和格式
 *
 * @author wanggang
 *
 */
public class ColorImageOutputFormat extends FileOutputFormat<NullWritable, ColorImageWritable> {

	@Override
	public RecordWriter<NullWritable, ColorImageWritable> getRecordWriter(TaskAttemptContext taskAttemptContext)
			throws IOException, InterruptedException {
		return new ColorImageRecordWriter(taskAttemptContext);
	}

}
