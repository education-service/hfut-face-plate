package edu.hfut.fr.image.analysis.algorithm;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * 对灰度图像进行欧氏距离的变化
 *
 * @author wanghao
 */
public class EuclideanDistanceTransform implements ImageAnalyser<FImage> {

	FImage distances;
	int[][] indices;

	@Override
	public void analyseImage(FImage image) {
		if (distances == null || distances.height != image.height || distances.width != distances.height) {
			distances = new FImage(image.width, image.height);
			indices = new int[image.height][image.width];
		}

		squaredEuclideanDistance(image, distances, indices);
	}

	/**
	 *
	 *  返回转化后的图像距离
	 *
	 */
	public FImage getDistances() {
		return distances;
	}

	public int[][] getIndices() {
		return indices;
	}

	protected static void DT1D(float[] f, float[] d, int[] v, int[] l, float[] z) {
		int k = 0;

		v[0] = 0;
		z[0] = -Float.MAX_VALUE;
		z[1] = Float.MAX_VALUE;

		for (int q = 1; q < f.length; q++) {
			float s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);

			while (s <= z[k]) {
				k--;
				s = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k + 1] = Float.MAX_VALUE;
		}

		k = 0;
		for (int q = 0; q < f.length; q++) {
			while (z[k + 1] < q)
				k++;

			d[q] = (q - v[k]) * (q - v[k]) + f[v[k]];
			l[q] = v[k];
		}
	}

	/**
	 * 计算二进制图像的平方欧氏距离
	 */
	public static void squaredEuclideanDistanceBinary(FImage image, FImage distances, int[][] indices) {
		float[] f = new float[Math.max(image.height, image.width)];
		float[] d = new float[f.length];
		int[] v = new int[f.length];
		int[] l = new int[f.length];
		float[] z = new float[f.length + 1];

		for (int x = 0; x < image.width; x++) {
			for (int y = 0; y < image.height; y++) {
				f[y] = image.pixels[y][x] == 0 ? Float.MAX_VALUE : 0;
			}

			DT1D(f, d, v, l, z);
			for (int y = 0; y < image.height; y++) {
				distances.pixels[y][x] = d[y];
				indices[y][x] = (l[y] * image.width) + x; //this is now row-major
			}
		}

		for (int y = 0; y < image.height; y++) {
			DT1D(distances.pixels[y], d, v, l, z);

			for (int x = 0; x < image.width; x++)
				l[x] = indices[y][l[x]];

			for (int x = 0; x < image.width; x++) {
				distances.pixels[y][x] = d[x];
				indices[y][x] = l[x];
			}
		}
	}

	public static void squaredEuclideanDistance(FImage image, FImage distances, int[][] indices) {
		float[] f = new float[Math.max(image.height, image.width)];
		float[] d = new float[f.length];
		int[] v = new int[f.length];
		int[] l = new int[f.length];
		float[] z = new float[f.length + 1];

		for (int x = 0; x < image.width; x++) {
			for (int y = 0; y < image.height; y++) {
				f[y] = Float.isInfinite(image.pixels[y][x]) ? (image.pixels[y][x] > 0 ? Float.MAX_VALUE
						: -Float.MAX_VALUE) : image.pixels[y][x];
			}

			DT1D(f, d, v, l, z);
			for (int y = 0; y < image.height; y++) {
				distances.pixels[y][x] = d[y];
				indices[y][x] = (l[y] * image.width) + x; //this is now row-major
			}
		}

		for (int y = 0; y < image.height; y++) {
			DT1D(distances.pixels[y], d, v, l, z);

			for (int x = 0; x < image.width; x++)
				l[x] = indices[y][l[x]];

			for (int x = 0; x < image.width; x++) {
				distances.pixels[y][x] = d[x];
				indices[y][x] = l[x];
			}
		}
	}

	/**
	 * 测试距离的转化
	 */
	public static void main(String args[]) throws IOException {
		FImage i = ImageUtilities.readF(new File("/Users/ss/Desktop/tache.jpg"));
		EuclideanDistanceTransform etrans = new EuclideanDistanceTransform();
		i.inverse();
		for (int x = 0; x < i.width; x++)
			for (int y = 0; y < i.height; y++)
				if (i.pixels[y][x] == 1.0f)
					i.setPixel(x, y, Float.MAX_VALUE);
		DisplayUtilities.display(i);
		i.analyseWith(etrans);
		i = etrans.getDistances();
		i.normalise();
		DisplayUtilities.display(i);
	}

}
