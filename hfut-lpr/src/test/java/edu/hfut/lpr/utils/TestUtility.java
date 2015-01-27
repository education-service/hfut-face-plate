package edu.hfut.lpr.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 测试工具类
 *
 * @author wanggang
 *
 */
public class TestUtility {

	public StringBuilder readFile(final String filename) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(filename));) {
			StringBuilder sb = new StringBuilder();
			String currentLine = null;
			while ((currentLine = br.readLine()) != null) {
				sb.append(currentLine);
			}
			return sb;
		}
	}

}
