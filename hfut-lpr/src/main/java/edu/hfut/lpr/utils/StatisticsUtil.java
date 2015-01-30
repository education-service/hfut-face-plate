package edu.hfut.lpr.utils;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileReader;
//import java.io.IOException;
import java.util.Vector;

/**
 * 统计分析测试类
 *
 * @author wanggang
 *
 */
public class StatisticsUtil {

	public static String helpText = "" + "-----------------------------------------------------------\n"
			+ "LPR 统计分析生成器\n\n命令行参数s\n\n    -help         帮助信息\n    -i <file>     对测试文件创建统计信息\n\n"
			+ "测试文件必须有一个CSV格式文件\n文件中每一行必须包含车辆图片的名字,\n实际的车辆图片名和识别后的车牌号示例 : \n001.jpg, 1B01234, 1B012??";

	public StatisticsUtil() {
		//
	}

	public static void main(String[] args) {
		if ((args.length == 2) && args[0].equals("-i")) {
			// 分析处理
			try {
				File f = new File(args[1]);
				BufferedReader input = new BufferedReader(new FileReader(f));
				String line;
				int lineCount = 0;
				String[] split;
				TestReport testReport = new TestReport();
				while ((line = input.readLine()) != null) {
					lineCount++;
					split = line.split(",", 4);
					if (split.length != 3) {
						System.out.println("Warning: line " + lineCount + " contains invalid CSV data (skipping)");
						continue;
					}
					testReport.addRecord(testReport.new TestRecord(split[0], split[1], split[2]));
				}
				input.close();
				testReport.printStatistics();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		} else {
			System.out.println(StatisticsUtil.helpText);
		}

	}

}

/**
 * 测试报告
 */
class TestReport {

	class TestRecord {

		String name, plate, recognizedPlate;
		int good;
		int length;

		TestRecord(String name, String plate, String recognizedPlate) {
			this.name = name.trim();
			this.plate = plate.trim();
			this.recognizedPlate = recognizedPlate.trim();
			this.compute();
		}

		private void compute() {
			this.length = Math.max(this.plate.length(), this.recognizedPlate.length());
			int g1 = 0;
			int g2 = 0;
			for (int i = 0; i < this.length; i++) {
				// BA123AB vs BA123ABX
				if (this.getChar(this.plate, i) == this.getChar(this.recognizedPlate, i)) {
					g1++;
				}
			}
			for (int i = 0; i < this.length; i++) {
				// BA123AB vs XBA123AB
				if (this.getChar(this.plate, this.length - i - 1) == this.getChar(this.recognizedPlate, this.length - i
						- 1)) {
					g2++;
				}
			}
			this.good = Math.max(g1, g2);
		}

		private char getChar(String string, int position) {
			if (position >= string.length()) {
				return ' ';
			}
			if (position < 0) {
				return ' ';
			}
			return string.charAt(position);
		}

		public int getGoodCount() {
			return this.good;
		}

		public int getLength() {
			return this.length;
		}

		public boolean isOk() {
			return this.length == this.good;
		}

	}

	Vector<TestRecord> records;

	TestReport() {
		this.records = new Vector<>();
	}

	void addRecord(TestRecord testRecord) {
		this.records.add(testRecord);
	}

	void printStatistics() {
		int weightedScoreCount = 0;
		int binaryScoreCount = 0;
		int characterCount = 0;
		System.out.println("----------------------------------------------");
		System.out.println("Defective plates\n");

		for (TestRecord record : this.records) {
			characterCount += record.getLength();
			weightedScoreCount += record.getGoodCount();
			binaryScoreCount += (record.isOk() ? 1 : 0);
			if (!record.isOk()) {
				System.out.println(record.plate + " ~ " + record.recognizedPlate + " ("
						+ (((float) record.getGoodCount() / record.getLength()) * 100) + "% ok)");
			}
		}
		System.out.println("\n----------------------------------------------");
		System.out.println("Test report statistics\n");
		System.out.println("Total number of plates     : " + this.records.size());
		System.out.println("Total number of characters : " + characterCount);
		System.out.println("Binary score               : " + (((float) binaryScoreCount / this.records.size()) * 100));
		System.out.println("Weighted score             : " + (((float) weightedScoreCount / characterCount) * 100));
	}

}