package edu.hfut.mapred.images.run;

import java.io.IOException;

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
import org.openimaj.image.MBFImage;

import edu.hfut.mapred.images.io.GrayImageOutputFormat;
import edu.hfut.mapred.images.io.ColorImageInputFormat;
import edu.hfut.mapred.images.writable.GrayImageWritable;
import edu.hfut.mapred.images.writable.ColorImageWritable;

/**
 * 彩色图像灰度化作业
 * 
 * @author wanggang
 *
 */
public class ColorImage2Gray extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		Job job = Job.getInstance(super.getConf(), "Color-Images-2-Grayscale");
		job.setJarByClass(getClass());
		job.setInputFormatClass(ColorImageInputFormat.class);
		job.setOutputFormatClass(GrayImageOutputFormat.class);
		job.setMapperClass(Img2GrayOpenIMAJMapper.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(GrayImageWritable.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new ColorImage2Gray(), args);
		System.exit(exitCode);
	}

	public static class Img2GrayOpenIMAJMapper extends
			Mapper<NullWritable, ColorImageWritable, NullWritable, GrayImageWritable> {

		private FImage gray_image;
		private MBFImage color_image;
		private final GrayImageWritable fiw = new GrayImageWritable();

		@Override
		public void map(NullWritable key, ColorImageWritable value, Context context) throws IOException,
				InterruptedException {
			color_image = value.getImage();

			if (color_image != null) {

				gray_image = color_image.flatten();
				fiw.setFormat(value.getFormat());
				fiw.setFileName(value.getFileName());
				fiw.setImage(gray_image);
				context.write(NullWritable.get(), fiw);
			}
		}
	}

}
