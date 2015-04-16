package zx.soft.rpc.example;

import zx.soft.rpc.core.Registry;

public class Server implements Hello {

	public static void main(String[] args) {
		Registry registry = new Registry();
		registry.register(new HelloService(), 1313);
		System.err.println("Server ready");
	}

	@Override
	public String sayHello() {
		return "Hello, world!";
	}

}
