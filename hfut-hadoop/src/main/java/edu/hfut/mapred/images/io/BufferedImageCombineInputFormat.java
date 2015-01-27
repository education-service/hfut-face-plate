package edu.hfut.mapred.images.io;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * BufferedImageWritable的输入格式
 * 
 * 一次性读取多个图像来提升读取速度，适合小图像处理
 * 
 * @author wanggang
 *
 */
public class BufferedImageCombineInputFormat extends CombineFileInputFormat<NullWritable, BufferedImageWritable> {

	public BufferedImageCombineInputFormat() {
		super();
		setMaxSplitSize(67108864);
	}

	@Override
	public RecordReader<NullWritable, BufferedImageWritable> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException {
		return new CombineFileRecordReader<NullWritable, BufferedImageWritable>((CombineFileSplit) split, context,
				BufferedImageCombineRecordReader.class);
	}

	@Override
	protected boolean isSplitable(JobContext context, Path file) {
		return false;
	}

}
