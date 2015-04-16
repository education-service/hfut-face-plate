package edu.hfut.api.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.hfut.api.resource.PostData;
import edu.hfut.api.utils.JsonUtils;
import edu.hfut.utils.http.RestletClientDaoImpl;

public class ClientDemo {

	public static void main(String[] args) throws IOException {

		String url = "http://localhost:8888/recognize";
		File file = new File("Face_Test/cl2.bmp");
		// RestletClientDaoImpl
		BufferedImage img = ImageIO.read(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		baos.flush();
		byte[] data = baos.toByteArray();
		PostData postData = new PostData("cl2", data);
		RestletClientDaoImpl rcdi = new RestletClientDaoImpl();
		System.out.println(rcdi.doPost(url, JsonUtils.toJsonWithoutPretty(postData)));

	}

}
