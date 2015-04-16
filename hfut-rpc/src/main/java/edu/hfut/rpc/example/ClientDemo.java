package edu.hfut.rpc.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.hfut.rpc.core.Registry;
import edu.hfut.rpc.server.ImageData;
import edu.hfut.rpc.server.Recognize;

public class ClientDemo {

	public static void main(String[] args) throws IOException {

		File file = new File("Face_Test/cl2.bmp");
		BufferedImage img = ImageIO.read(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		baos.flush();
		byte[] data = baos.toByteArray();
		ImageData imageData = new ImageData("cl2", data);
		Registry registry = new Registry();
		Recognize stub = registry.lookup(Recognize.class, "localhost", 8888);
		System.out.println(stub.runRecognize(imageData));

	}

}
