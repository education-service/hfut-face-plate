package edu.hfut.mapred.images.driver;

import org.apache.hadoop.util.ProgramDriver;

import edu.hfut.mapred.images.run.BufferedImage2Gray;
import edu.hfut.mapred.images.run.BufferedImageEdgeDetection;
import edu.hfut.mapred.images.run.BufferedImageFormatChange;
import edu.hfut.mapred.images.run.BufferedImageProcess;
import edu.hfut.mapred.images.run.BufferedImageSequenceInput;
import edu.hfut.mapred.images.run.BufferedImageSequenceOutput;
import edu.hfut.mapred.images.run.ColorImage2Gray;
import edu.hfut.mapred.images.run.CombineBufferedImageProcess;
import edu.hfut.mapred.images.run.fr.FaceCountGray;
import edu.hfut.mapred.images.run.fr.FaceCountGrayOther;
import edu.hfut.mapred.images.run.fr.FaceDetectionColor;
import edu.hfut.mapred.images.run.fr.FaceDetectionGray;
import edu.hfut.mapred.images.run.fr.FaceRecognitionDistribution;
import edu.hfut.mapred.images.run.lpr.PlateRecognitionDistribution;

/**
 * 驱动类
 *
 * @author wanggang
 *
 */
public class MapredImagesDriver {

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

		int exitCode = -1;
		org.apache.hadoop.util.ProgramDriver pgd = new ProgramDriver();
		try {
			// 基本图像分布式处理
			pgd.addClass("bufferedImageProcess", BufferedImageProcess.class,
					"BufferedImage图像处理(小图像)，通过ImageProcessor实现类指定处理算法");
			pgd.addClass("combineBufferedImageProcess", CombineBufferedImageProcess.class,
					"BufferedImage图像处理(大图像)，通过ImageProcessor实现类指定处理算法");
			pgd.addClass("bufferedImageFormatChange", BufferedImageFormatChange.class, "图片格式转换");
			pgd.addClass("bufferedImageEdgeDetection", BufferedImageEdgeDetection.class, "缓冲图像边缘检测");
			pgd.addClass("bufferedImageSequenceInput", BufferedImageSequenceInput.class, "缓冲图像序列化输入");
			pgd.addClass("bufferedImageSequenceOutput", BufferedImageSequenceOutput.class, "缓冲图像序列化输出");
			pgd.addClass("bufferedImage2Gray", BufferedImage2Gray.class, "缓冲图像灰度化");
			pgd.addClass("colorImage2Gray", ColorImage2Gray.class, "彩色图像灰度化");
			// 人脸识别相关分布式处理
			pgd.addClass("faceCountGray", FaceCountGray.class, "人脸检测并计数，普通实现，没有默认输入输出格式");
			pgd.addClass("faceCountGrayOther", FaceCountGrayOther.class, "人脸检测并计数，另一种实现，有默认输入输出格式");
			pgd.addClass("faceDetectionGray", FaceDetectionGray.class, "灰度图人脸检");
			pgd.addClass("faceDetectionColor", FaceDetectionColor.class, "彩色图人脸检测");
			pgd.addClass("faceRecognitionDistribution", FaceRecognitionDistribution.class, "人脸识别分布式处理");
			// 车牌识别相关分布式处理
			pgd.addClass("plateRecognitionDistribution", PlateRecognitionDistribution.class, "车牌识别分布式处理");
			pgd.driver(args);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		System.exit(exitCode);

	}

}
