package edu.hfut.mapred.images.run.lpr;

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
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.lpr.run.PlateRecognizer;
import edu.hfut.mapred.images.io.BufferedImageInputFormat;
import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * 车牌识别分布式处理
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar plateRecognitionDistribution hdfs_image_folder hdfs_output_folder
 *
 * @author wanggang
 *
 */
public class PlateRecognitionDistribution extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = Job.getInstance(super.getConf(), "PlateRecognitionDistribution");

		job.setInputFormatClass(BufferedImageInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapperClass(PlateRecognitionMapper.class);
		job.setNumReduceTasks(0);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setJarByClass(PlateRecognitionDistribution.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new PlateRecognitionDistribution(), args);
		System.exit(exitCode);
	}

	public static class PlateRecognitionMapper extends Mapper<NullWritable, BufferedImageWritable, Text, Text> {

		/**
		 * key：图像文件名
		 */
		@Override
		public void map(NullWritable key, BufferedImageWritable value, Context context) throws IOException,
				InterruptedException {
			String plateNumber = PlateRecognizer.recognizeResult(value.getImage());
			if (value.getImage() != null) {
				context.write(new Text(value.getFileName()), new Text(plateNumber));
			}
		}

	}

}
