package edu.hfut.fr.driver;

import org.apache.hadoop.util.ProgramDriver;

import edu.hfut.fr.driver.run.FaceRecognition;

/**
 * 人脸识别Hadoop驱动器
 *
 * @author wanghao
 *
 */

public class FRDriver {

	public static void main(String argv[]) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		//  添加运行类
		try {
			pgd.addClass("faceRecognition", FaceRecognition.class, "人脸识别主类");
			pgd.driver(argv);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

}
