package edu.hfut.lpr.driver;

import org.apache.hadoop.util.ProgramDriver;

import edu.hfut.lpr.run.SimpleLPR;

/**
 * 车牌识别Hadoop驱动器
 *
 * @author wanggang
 *
 */
public class LPRDriver {

	public static void main(String argv[]) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("simpleLPR", SimpleLPR.class, "简单车牌识别算法");
			pgd.driver(argv);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

}
