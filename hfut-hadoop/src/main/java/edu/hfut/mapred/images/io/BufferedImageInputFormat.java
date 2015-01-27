package edu.hfut.mapred.images.io;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * BufferedImage输入格式
 * 每个Map读取一个图像，所以不适合小图像处理
 * 
 * @author wanggang
 *
 */
public class BufferedImageInputFormat extends ImageInputFormat<NullWritable, BufferedImageWritable> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RecordReader createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
			throws IOException, InterruptedException {
		return new BufferedImageRecordReader();
	}

}
