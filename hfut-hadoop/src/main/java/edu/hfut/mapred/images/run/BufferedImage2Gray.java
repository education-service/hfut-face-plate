package edu.hfut.mapred.images.run;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import edu.hfut.mapred.images.job.HadoopJob;
import edu.hfut.mapred.images.job.HadoopJobConfiguration;
import edu.hfut.mapred.images.writable.BufferedImageWritable;

/**
 * 缓冲图像灰度化作业
 *
 * @author wanggang
 *
 */
public class BufferedImage2Gray extends HadoopJob {

	public BufferedImage2Gray() {
		super(BufferedImage2GrayMapper.class, new HadoopJobConfiguration("<input> <output>\n", 2, "BufferedImage2Gray"));
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int exitCode = ToolRunner.run(conf, new BufferedImage2Gray(), args);
		System.exit(exitCode);
	}

	public static class BufferedImage2GrayMapper extends
			Mapper<NullWritable, BufferedImageWritable, NullWritable, BufferedImageWritable> {

		private Graphics g;
		private BufferedImage grayImage;

		@Override
		protected void map(NullWritable key, BufferedImageWritable value, Context context) throws IOException,
				InterruptedException {

			BufferedImage colorImage = value.getImage();

			if (colorImage != null) {
				grayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(),
						BufferedImage.TYPE_BYTE_GRAY);
				g = grayImage.getGraphics();
				g.drawImage(colorImage, 0, 0, null);
				g.dispose();
				BufferedImageWritable biw = new BufferedImageWritable(grayImage, value.getFileName(), value.getFormat());
				context.write(NullWritable.get(), biw);
			}
		}

	}

}
