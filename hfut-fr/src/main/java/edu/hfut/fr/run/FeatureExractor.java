package edu.hfut.fr.run;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;

public class FeatureExractor {

	public static void main(String[] args) throws IOException {

		FImage faceImage1 = ImageUtilities.readF(new File("faces_db/s1/2.pgm"));
		DoubleFV feature1 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage1);

		double[] distances = new double[40];
		for (int s = 1; s < 41; s++) {
			for (int ii = 1; ii < 11; ii++) {
				FImage faceImage2 = ImageUtilities.readF(new File("faces_db/s" + s + "/" + ii + ".pgm"));
				DoubleFV feature2 = FImage2DoubleFV.INSTANCE.extractFeature(faceImage2);
				distances[s - 1] += distance(feature1.getVector(), feature2.getVector(), feature2.getVector().length);
			}
		}

		for (double distance : distances) {
			System.err.println(distance);
		}

	}

	public static double distance(double[] v1, double[] v2, int dim) {
		float dis = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += Math.pow(Math.abs(v1[i] - v2[i]), 2);
		}
		return Math.sqrt(dis);
	}

	public static double distance1(float[] v1, float[] v2, int dim) {
		float dis = 0.0f;
		float v1norm = 0.0f;
		float v2norm = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += v1[i] * v2[i];
			v1norm += Math.sqrt(Math.pow(v1[i], 2));
			v2norm += Math.sqrt(Math.pow(v2[i], 2));
		}
		return dis / (v1norm * v2norm);
	}

}
