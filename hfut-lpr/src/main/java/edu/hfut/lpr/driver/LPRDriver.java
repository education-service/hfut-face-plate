package edu.hfut.lpr.driver;

import org.apache.hadoop.util.ProgramDriver;

import edu.hfut.lpr.run.PlateRecognizer;
import edu.hfut.lpr.run.SimpleLPR;

/**
 * 车牌识别Hadoop驱动器
 *
 * 测试命令：
 * target/hfut-lpr/bin/ctl.sh start plateRecognizer src/test/resources/en-snapshots/test_001.jpg
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
			pgd.addClass("plateRecognizer", PlateRecognizer.class, "分布式车牌识别算法");
			pgd.driver(argv);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

}
