package edu.hfut.mapred.images.io;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * BufferedImage输出格式
 * 写入HDFS系统中，每个图像作为一个独立的文件写入
 * 
 * @author wanggang
 *
 */
public class BufferedImageOutputFormat extends FileOutputFormat<NullWritable, BufferedImageWritable> {

	@Override
	public RecordWriter<NullWritable, BufferedImageWritable> getRecordWriter(TaskAttemptContext taskAttemptContext)
			throws IOException, InterruptedException {
		return new BufferedImageRecordWriter(taskAttemptContext);
	}

}
