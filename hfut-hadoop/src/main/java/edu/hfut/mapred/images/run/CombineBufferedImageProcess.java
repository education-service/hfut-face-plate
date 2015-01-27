package edu.hfut.mapred.images.run;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.io.BufferedImageCombineInputFormat;

/**
 * 基于BufferedImage图像表示来运行只有Map的作业。
 *
 * 每个Map接受一个图像，并基于ImageProcessor（通过命令行指定实现类）来处理，
 * 该作业使用BufferedImageCombineInputFormat输入格式，适合小文件处理。
 *
 * @author wanggang
 *
 */
public class CombineBufferedImageProcess extends BufferedImageProcess implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		Job job = basicSetup(args);

		job.setJobName("CombineBufferedImageProcess job");
		job.setInputFormatClass(BufferedImageCombineInputFormat.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new CombineBufferedImageProcess(), args);
		System.exit(exitCode);
	}

}
