package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;
import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.job.HadoopJob;
import edu.hfut.mapred.images.job.HadoopJobConfiguration;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 人脸数量提取作业
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceCountGrayOther hdfs_image_folder hdfs_output_folder
 *
 * @author wanggang
 *
 */
public class FaceCountGrayOther extends HadoopJob {

	public FaceCountGrayOther() {
		super(FaceCountGrayOtherMapper.class, new HadoopJobConfiguration("<input> <output>\n", 2,
				"FaceCountGrayOther\n"));
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		FaceCountGrayOther myJob = new FaceCountGrayOther();

		myJob.setIFormat(GrayImageInputFormat.class);
		myJob.setOFormat(TextOutputFormat.class);
		myJob.setOKey(Text.class);
		myJob.setOValue(IntWritable.class);

		int exitCode = ToolRunner.run(conf, myJob, args);
		System.exit(exitCode);
	}

	public static class FaceCountGrayOtherMapper extends Mapper<NullWritable, GrayImageWritable, Text, IntWritable> {

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
