package edu.hfut.mapred.images.io;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 灰度图像输入格式
 * 每个Map读取一个图像，但不适合小图像处理
 *
 * @author wanggang
 *
 */
public class GrayImageInputFormat extends ImageInputFormat<NullWritable, GrayImageWritable> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RecordReader createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
			throws IOException, InterruptedException {
		return new GrayImageRecordReader();
	}

}