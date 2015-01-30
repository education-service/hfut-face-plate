package edu.hfut.mapred.images.run.fr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.writable.BufferedImageWritable;
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

	/**
	 * 从HDFS中读取序列化形式的中心数据
	 */
	public static HashMap<String, List<BufferedImage>> readCentroids(Configuration conf, Path path) throws IOException {
		System.out.println("开始读取人脸样本库序列化数据......");
		HashMap<String, List<BufferedImage>> faceDB = new HashMap<>();
		FileSystem fs = FileSystem.get(path.toUri(), conf);
		FileStatus[] list = fs.globStatus(new Path(path, "part-*"));
		for (FileStatus status : list) {
			SequenceFile.Reader reader = null;
			try {
				reader = new SequenceFile.Reader(fs, status.getPath(), conf);
				NullWritable key = (NullWritable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
				BufferedImageWritable value = (BufferedImageWritable) ReflectionUtils.newInstance(
						reader.getValueClass(), conf);
				List<BufferedImage> biList = new ArrayList<>();
				while (reader.next(key, value)) {
					biList.add(value.getImage());
				}
				faceDB.put(status.getPath().getName(), biList);
			} finally {
				IOUtils.closeStream(reader);
			}
		}
		System.out.println("读取人脸样本库序列化数据结束......");
		return faceDB;
	}

	@Override
	public int run(String[] args) throws Exception {

		Configuration conf = getConf();
		// 待识别人脸图片目录
		Path input = new Path(conf.get("input"));
		// 人脸识别结果目录
		Path output = new Path(conf.get("output"));
		// 序列化人脸识别样本库目录
		Path facesDBCache = new Path("faces-db-cache");

		/**
		 * 作业1：读取人脸样本库序列化数据库，并输出到数列化文件中
		 */

		//		conf.set("faces.db", args[0]);
		//		Job job = Job.getInstance(conf, "FaceRecognitionDistribution");
		//		job.setJarByClass(getClass());
		//		job.setInputFormatClass(GrayImageInputFormat.class);
		//		job.setOutputFormatClass(TextOutputFormat.class);
		//		job.setMapperClass(FaceRecognitionDistributionMapper.class);
		//		FileInputFormat.addInputPath(job, new Path(args[1]));
		//		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		//		job.setNumReduceTasks(0);
		//		job.setOutputKeyClass(Text.class);
		//		job.setOutputValueClass(Text.class);
		//		return job.waitForCompletion(true) ? 0 : 1;
		return 0;
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
