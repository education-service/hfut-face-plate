package edu.hfut.rpc.core;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.rpc.serializer.JavaSerializer;
import edu.hfut.rpc.serializer.KryoSerializer;
import edu.hfut.rpc.serializer.Serializer;

public class Registry {

	private static Logger logger = LoggerFactory.getLogger(Registry.class);

	private final Serializer serializer;

	public Registry() {
		Properties properties = new Properties();
		File confFile = new File("./simpleRpc.properties");
		try {
			if (confFile.exists()) {
				properties.load(new FileReader(confFile));
				logger.info("Load config:{}", confFile.getAbsolutePath());
			} else {
				properties.load(this.getClass().getClassLoader().getResourceAsStream("simpleRpc.properties"));
				logger.info("Load config from CLASSPATH.");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String serializerProp = properties.getProperty("serializer");
		if (serializerProp.equals("java")) {
			serializer = new JavaSerializer();
		} else if (serializerProp.equals("kryo")) {
			serializer = new KryoSerializer();
		} else {
			throw new RuntimeException("Unsupported serializer: " + serializerProp);
		}
		logger.info("serializer:{}", serializerProp);

	}

	public Registry(Serializer serializer) {
		this.serializer = serializer;
	}

	@SuppressWarnings("unchecked")
	public <T> T lookup(Class<T> serviceType, String host, int port) {
		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(host, port, serviceType, serializer);
		return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[] { serviceType }, handler);
	}

	public <T> void register(T serviceInstance, int port) {
		try {
			new Thread(new AcceptLoop(serviceInstance, serializer, port)).start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
