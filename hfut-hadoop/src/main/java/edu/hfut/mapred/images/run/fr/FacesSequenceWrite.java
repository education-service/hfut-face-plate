package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.io.GrayImageInputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;

/**
 * 读取人脸样本库数据，并输出到数列化文件中
 *
 * 运行命令：
 * bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar facesSequenceWrite -D facesdb=faces_db_folder -D facesdbseq=faces_db_seq_folder
 *
 * @author wanggang
 *
 */
public class FacesSequenceWrite extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		Configuration conf = getConf();
		// 人脸识别样本库目录
		Path facesDB = new Path(conf.get("facesdb"));
		// 序列化人脸识别样本库目录
		Path facesDBSeqOutput = new Path(conf.get("facesdbseq"));

		FileSystem fs = FileSystem.get(URI.create(conf.get("facesdb")), conf);
		FileStatus[] status = fs.listStatus(facesDB);
		Path[] listedPaths = FileUtil.stat2Paths(status);
		for (Path p : listedPaths) {
			Configuration iterConf = new Configuration();
			Path iterInput = new Path(String.format("%s/%s", facesDB.getName(), p.getName()));
			Path iterOutput = new Path(String.format("%s/%s", facesDBSeqOutput.getName(), p.getName()));
			Job iterJob = new Job(iterConf);
			iterJob.setJobName("FacesSequenceOutput-" + p.getName());
			iterJob.setJarByClass(getClass());
			iterJob.setInputFormatClass(GrayImageInputFormat.class);
			iterJob.setOutputFormatClass(SequenceFileOutputFormat.class);
			iterJob.setMapperClass(FacesSequenceOutputMapper.class);
			FileInputFormat.addInputPath(iterJob, iterInput);
			FileOutputFormat.setOutputPath(iterJob, iterOutput);
			iterJob.setNumReduceTasks(0);
			iterJob.setOutputKeyClass(NullWritable.class);
			iterJob.setOutputValueClass(GrayImageWritable.class);

			if (!iterJob.waitForCompletion(true)) {
				System.err.println("ERROR: Iteration path " + p.getName() + " failed!");
				System.exit(1);
			}

			System.out.println("Successful: Iteration path " + p.getName() + " OK!");
		}

		return 0;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new FacesSequenceWrite(), args);
		System.exit(exitCode);
	}

	public static class FacesSequenceOutputMapper extends
			Mapper<NullWritable, GrayImageWritable, NullWritable, GrayImageWritable> {

		@Override
		public void map(NullWritable key, GrayImageWritable value, Context context) throws IOException,
				InterruptedException {

			if (value.getImage() != null) {
				context.write(NullWritable.get(), value);
			}

		}

	}

}
