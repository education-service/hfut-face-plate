package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.FImage2DoubleFV;

import edu.hfut.mapred.images.writable.GrayImageWritable;

public class FaceRecognitionDistributionMapper extends Mapper<NullWritable, GrayImageWritable, Text, Text> {

	private static HashMap<String, List<FImage>> faceSamples;

	@Override
	protected void setup(Context context) throws IOException {
		Configuration conf = context.getConfiguration();
		// 序列化人脸识别样本库目录
		Path facesDBSeq = new Path(conf.get("facesdbseq"));
		faceSamples = FaceRecognitionDistribution.readFaceSamples(conf, facesDBSeq);
	}

	@Override
	public void map(NullWritable key, GrayImageWritable value, Context context) throws IOException,
			InterruptedException {

		String name = recognize(value.getImage());
		context.write(new Text(value.getFileName()), new Text(name));

	}

	/**
	 * 计算出识别的库名称（人脸文件名称）
	 */
	public static String recognize(FImage image) {

		HashMap<String, Double> distances = new HashMap<>();

		DoubleFV feature = FImage2DoubleFV.INSTANCE.extractFeature(image);
		for (Entry<String, List<FImage>> childFold : faceSamples.entrySet()) {
			double distance = 0.0f;
			for (FImage imagec : childFold.getValue()) {
				DoubleFV featuretmp = FImage2DoubleFV.INSTANCE.extractFeature(imagec);
				distance += distance(featuretmp.getVector(), feature.getVector(), feature.getVector().length);
			}
			distances.put(childFold.getKey(), distance);
		}

		double minDistance = Double.MAX_VALUE;
		String minIndex = "";
		for (Entry<String, Double> tmp : distances.entrySet()) {
			if (tmp.getValue() < minDistance) {
				minDistance = tmp.getValue();
				minIndex = tmp.getKey();
			}
		}

		return minIndex;
	}

	public static double distance(double[] v1, double[] v2, int dim) {
		float dis = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += Math.pow(Math.abs(v1[i] - v2[i]), 2);
		}
		return Math.sqrt(dis);
	}

}
