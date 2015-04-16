package zx.soft.rpc.example;

import zx.soft.rpc.core.Registry;

public class Client {

	public static void main(String[] args) {
		Registry registry = new Registry();
		Hello stub = registry.lookup(Hello.class, "127.0.0.1", 1313);
		System.out.println(stub.sayHello());
	}

}
