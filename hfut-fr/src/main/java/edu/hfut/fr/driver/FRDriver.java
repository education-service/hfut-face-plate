package edu.hfut.fr.driver;

import org.apache.hadoop.util.ProgramDriver;

import edu.hfut.fr.run.FKEFaceDetectorDemo;
import edu.hfut.fr.run.FaceRecognition;

/**
 * 车牌识别Hadoop驱动器
 */
public class FRDriver {

	public static void main(String argv[]) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("faceRecognition", FaceRecognition.class, "人脸识别主类");
			pgd.addClass("fKEFaceDetectorDemo", FKEFaceDetectorDemo.class, "FKE人脸检测");
			pgd.driver(argv);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

}
