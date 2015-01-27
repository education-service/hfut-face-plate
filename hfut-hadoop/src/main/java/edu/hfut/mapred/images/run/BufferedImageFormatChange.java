package edu.hfut.mapred.images.run;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.job.HadoopJob;
import edu.hfut.mapred.images.job.HadoopJobConfiguration;
import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * 图片格式转换作业
 * 
 * @author wanggang
 *
 */
public class BufferedImageFormatChange extends HadoopJob {

	public BufferedImageFormatChange() {
		super(Img2ChangeFormatMapper.class, new HadoopJobConfiguration("<input> <output> <format>\n", 3,
				"Hadoop Img2PNG  job"));
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new BufferedImageFormatChange(), args);
		System.exit(exitCode);
	}

	@Override
	protected void preprocess(String[] args, Job job) {
		Configuration conf = job.getConfiguration();
		conf.set("format", args[2]);
	}

	public static class Img2ChangeFormatMapper extends
			Mapper<NullWritable, BufferedImageWritable, NullWritable, BufferedImageWritable> {

		@Override
		public void map(NullWritable key, BufferedImageWritable value, Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			value.setFormat(conf.get("format"));
			context.write(NullWritable.get(), value);
		}

	}

}
