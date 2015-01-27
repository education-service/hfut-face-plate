package edu.hfut.mapred.images.run;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.io.BufferedImageCombineInputFormat;
import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * 缓冲图像序列化输出作业
 *
 * @author wanggang
 *
 */
public class BufferedImageSequenceOutput extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = Job.getInstance(super.getConf(), "BufferedImageSequenceOutput");
		job.setJarByClass(getClass());
		job.setInputFormatClass(BufferedImageCombineInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapperClass(BufferedImageSequenceOutputMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BufferedImageWritable.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new BufferedImageSequenceOutput(), args);
		System.exit(exitCode);
	}

	public static class BufferedImageSequenceOutputMapper extends
			Mapper<NullWritable, BufferedImageWritable, Text, BufferedImageWritable> {

		@Override
		public void map(NullWritable key, BufferedImageWritable value, Context context) throws IOException,
				InterruptedException {

			if (value.getImage() != null) {
				context.write(new Text(value.getFileName()), value);
			}

		}

	}

}
