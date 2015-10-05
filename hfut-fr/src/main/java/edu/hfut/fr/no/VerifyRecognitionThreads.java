package edu.hfut.fr.no;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.FImage2DoubleFV;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 人脸识别多线程实现
 *
 * @author wanggang
 *
 */
public class VerifyRecognitionThreads {

	private static final HashMap<String, List<double[]>> corpus = new HashMap<>();

	// 训练库
	private static final String TRAINNING_DB = "Face_DB";
	// 测试库
	private static final String TEST_DB = "Face_Test/";

	static {
		try {
			File[] DbFiles = new File(TRAINNING_DB).listFiles();
			File[] dbfiles = null;
			for (File fdb1 : DbFiles) {
				List<double[]> list = new ArrayList<>();
				dbfiles = fdb1.listFiles();
				for (File fdb2 : dbfiles) {
					list.add(FImage2DoubleFV.INSTANCE.extractFeature(ImageUtilities.readF(fdb2)).getVector());
				}
				corpus.put(fdb1.getName(), list);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

		// 申请线程池
		final ThreadPoolExecutor pool = ApplyThreadPool.getThreadPoolExector(16);
		// 出现错误时平滑关闭
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				pool.shutdown();
			}
		}));
		// 循环测试每个人脸
		File[] TestFiles = new File(TEST_DB).listFiles();
		for (File file : TestFiles) {
			pool.execute(new VerifyRunnable(file.getName()));
		}

		// 线程结束60秒后关闭线程池
		try {
			pool.awaitTermination(20, TimeUnit.SECONDS);
			pool.shutdown();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

	public static class VerifyRunnable implements Runnable {

		private String fileName;

		public VerifyRunnable(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public void run() {
			try {
				MBFImage mbimage = ImageUtilities.readMBF(new File(TEST_DB + fileName));
				FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
				List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(mbimage));
				AffineAligner faceAligner = new AffineAligner();
				// 找出检测中的最大图像块即为人脸
				int max = Integer.MIN_VALUE;
				KEDetectedFace maxFace = null;
				for (KEDetectedFace face : faces) {
					if (face.getFacePatch().getHeight() > max) {
						max = face.getFacePatch().getHeight();
						maxFace = face;
					}
				}
				if (maxFace == null) {
					System.out.println("the " + fileName + "*******->identity is null");
					return;
				}
				FImage fimage = faceAligner.align(maxFace);
				DoubleFV fv1 = FImage2DoubleFV.INSTANCE.extractFeature(fimage);
				double MinDistances = Double.MAX_VALUE;
				String rightname = "";
				for (Entry<String, List<double[]>> tmp : corpus.entrySet()) {
					double distances = 0;
					for (double[] d : tmp.getValue()) {
						distances = distances + cos(fv1.getVector(), d, fv1.getVector().length);
					}
					// 求均值，注意原先是求累加和，这里求cos相似度均值的意义为：
					// 1、兼容原先的识别算法
					// 2、当样本库不均匀时，可以保证平均的cos相似度
					distances = distances / (tmp.getValue().size());
					if (distances < MinDistances) {
						MinDistances = distances;
						rightname = tmp.getKey();
					}
				}
				System.out.println("the " + fileName + "*******->identity is " + rightname + "*");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 余弦负值，使用余弦定理求两个向量的相似度，值越大相似度越大，
	 * 为了兼容距离相似度，所以取负值，值越小相似度越大。
	 */
	public static double cos(double[] v1, double[] v2, int dim) {
		return (-1.0) * d(v1, v2, dim) / (q(v1) * q(v2));
	}

	/**
	 * 求两个向量的乘积
	 */
	private static double d(double[] v1, double[] v2, int dim) {
		double result = 0.0;
		for (int i = 0; i < dim; i++) {
			result += v1[i] * v2[i];
		}
		return result;
	}

	/**
	 * 求一组向量的平方根
	 */
	private static double q(double[] v) {
		double result = 0.0;
		for (double t : v) {
			result += t * t;
		}
		return Math.sqrt(result);
	}

}
