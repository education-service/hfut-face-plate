package edu.hfut.rpc.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.api.utils.ConfigUtil;
import edu.hfut.rpc.core.Registry;

public class RpcServer {

	private static Logger logger = LoggerFactory.getLogger(RpcServer.class);

	private final int PORT;

	public RpcServer() {
		Properties props = ConfigUtil.getProps("api.properties");
		PORT = Integer.parseInt(props.getProperty("api.port"));
	}

	public static void main(String[] args) {
		RpcServer rpcServer = new RpcServer();
		rpcServer.run();
	}

	public void run() {
		Registry registry = new Registry();
		registry.register(new RecognizeService(), PORT);
		logger.info("Server start at PORT:{}", PORT);
	}

}
