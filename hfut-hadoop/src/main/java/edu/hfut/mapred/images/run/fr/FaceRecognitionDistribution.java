package edu.hfut.mapred.images.run.fr;

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

import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 人脸识别分布式处理
 *
 * 注意：需要根据实际需求实现识别部件
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceRecognitionDistribution faces_db_folder hdfs_image_folder hdfs_output_folder
 *
 * @author wanggang
 *
 */
public class FaceRecognitionDistribution extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 3) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Configuration conf = super.getConf();
		conf.set("faces.db", args[0]);
		Job job = Job.getInstance(conf, "FaceRecognitionDistribution");
		job.setJarByClass(getClass());
		job.setInputFormatClass(GrayImageInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapperClass(FaceRecognitionDistributionMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new FaceRecognitionDistribution(), args);
		System.exit(exitCode);
	}

	public static class FaceRecognitionDistributionMapper extends Mapper<NullWritable, GrayImageWritable, Text, Text> {

		private final Text fileName = new Text();
		private final Text faceName = new Text();

		@Override
		public void map(NullWritable key, GrayImageWritable value, Context context) throws IOException,
				InterruptedException {

			Configuration conf = context.getConfiguration();
			String faceDbPath = conf.get("faces.db");
			String name = FaceRecognitionTools.recognizeDataset(faceDbPath, value.getImage());
			fileName.set(value.getFileName());
			faceName.set(name);
			context.write(fileName, faceName);

		}

	}

}
