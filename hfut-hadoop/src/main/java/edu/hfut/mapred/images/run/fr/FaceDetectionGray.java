package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;
import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.io.GrayImageOutputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 灰度图人脸检测作业
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceDetectionGray hdfs_image_folder hdfs_output_folder
 *
 * @author wanggang
 *
 */
public class FaceDetectionGray extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = Job.getInstance(super.getConf(), "FaceDetectionGray");
		job.setJarByClass(getClass());
		job.setInputFormatClass(GrayImageInputFormat.class);
		job.setOutputFormatClass(GrayImageOutputFormat.class);
		job.setMapperClass(FaceDetectionGrayMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(GrayImageWritable.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new FaceDetectionGray(), args);
		System.exit(exitCode);
	}

	public static class FaceDetectionGrayMapper extends
			Mapper<NullWritable, GrayImageWritable, NullWritable, GrayImageWritable> {

		private FImage image;

		@Override
		public void map(NullWritable key, GrayImageWritable value, Context context) throws IOException,
				InterruptedException {
			image = value.getImage();

			if (image != null) {
				FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
				List<DetectedFace> faces = fd.detectFaces(image);
				for (DetectedFace face : faces) {
					image.drawShape(face.getShape(), 3, 1f);
				}
				context.write(NullWritable.get(), value);
			}

		}
	}

}
