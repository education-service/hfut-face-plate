package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;
import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 人脸检测并计数作业
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceCountGray hdfs_image_folder hdfs_output_folder
 *
 * @author wanggang
 *
 */
public class FaceCountGray extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = Job.getInstance(super.getConf(), "FaceCountGray");
		job.setJarByClass(getClass());
		job.setInputFormatClass(GrayImageInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapperClass(FaceCountGrayMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new FaceCountGray(), args);
		System.exit(exitCode);
	}

	public static class FaceCountGrayMapper extends Mapper<NullWritable, GrayImageWritable, Text, IntWritable> {

		private FImage image;
		private final Text fileName = new Text();
		private final IntWritable faceCount = new IntWritable();

		@Override
		public void map(NullWritable key, GrayImageWritable value, Context context) throws IOException,
				InterruptedException {
			image = value.getImage();

			if (image != null) {
				FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
				List<DetectedFace> faces = fd.detectFaces(image);
				faceCount.set(faces.size());
				fileName.set(value.getFileName() + "." + value.getFormat());
				context.write(fileName, faceCount);
			}

		}
	}

}
