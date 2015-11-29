package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.image.FImage;

import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 人脸识别分布式处理
 *
 * 注意：需要根据实际需求实现识别部件
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceRecognitionDistribution -D input=hdfs_faces_folder
 * -D output=hdfs_output_folder -D facesdbseq=hdfs_faces_db_seq_folder
 *
 * @author wanggang
 *
 */
public class FaceRecognitionDistribution extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		Configuration conf = getConf();
		// 待识别人脸图片目录
		Path input = new Path(conf.get("input"));
		// 人脸识别结果目录
		Path output = new Path(conf.get("output"));

		Job job = Job.getInstance(conf, "FaceRecognitionDistribution");
		job.setJarByClass(getClass());
		job.setInputFormatClass(GrayImageInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapperClass(FaceRecognitionDistributionMapper.class);
		FileInputFormat.addInputPath(job, input);
		FileOutputFormat.setOutputPath(job, output);
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

	/**
	 * 从HDFS中读取序列化形式的中心数据
	 */
	public static HashMap<String, List<FImage>> readFaceSamples(Configuration conf, Path facesDBSeq) throws IOException {
		System.out.println("开始读取人脸样本库序列化数据......");
		HashMap<String, List<FImage>> faceDB = new HashMap<>();
		// 序列化人脸识别样本库目录
		FileSystem fs = FileSystem.get(facesDBSeq.toUri(), conf);
		FileStatus[] statuses = fs.listStatus(facesDBSeq);
		Path[] listedPaths = FileUtil.stat2Paths(statuses);
		// 循环每个子目录
		for (Path path : listedPaths) {
			Path iterPath = new Path(String.format("%s/%s", facesDBSeq.getName(), path.getName()));
			FileStatus[] list = fs.globStatus(new Path(iterPath, "part-m-*"));
			// 循环每个子目录下面的每个文件
			List<FImage> biList = new ArrayList<>();
			for (FileStatus status : list) {
				SequenceFile.Reader reader = null;
				try {
					reader = new SequenceFile.Reader(fs, status.getPath(), conf);
					NullWritable key = (NullWritable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
					GrayImageWritable value = (GrayImageWritable) ReflectionUtils.newInstance(reader.getValueClass(),
							conf);
					if (reader.next(key, value)) {
						biList.add(value.getImage());
					}
				} finally {
					IOUtils.closeStream(reader);
				}
			}
			faceDB.put(path.getName(), biList);
		}
		System.out.println("读取人脸样本库序列化数据结束......");
		return faceDB;
	}

}
