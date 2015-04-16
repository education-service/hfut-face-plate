package edu.hfut.api.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.api.server.ApiServer;

/**
 * 驱动类
 *
 * @author wanggang
 *
 */
public class ApiDriver {

	private static Logger logger = LoggerFactory.getLogger(ApiDriver.class);

	/**
	 * 主函数，需要修改，启动接口后，又关闭了
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.err.println("Usage: Input <class-name>, eg: \n" + //
					"`apiServer` 远程Http接口服务");
			System.exit(-1);
		}
		String[] leftArgs = new String[args.length - 1];
		System.arraycopy(args, 1, leftArgs, 0, leftArgs.length);

		switch (args[0]) {
		case "apiServer":
			logger.info("远程Http接口服务");
			ApiServer.main(leftArgs);
			break;
		default:
			return;
		}

	}

}
