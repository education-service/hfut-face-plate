package edu.hfut.api.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.api.application.ApiApplication;
import edu.hfut.api.utils.URLCodecUtils;

/**
 * 接口服务资源类
 *
 * @author wanggang
 *
 */
public class ApiResource extends ServerResource {

	private static Logger logger = LoggerFactory.getLogger(ApiResource.class);

	private ApiApplication application;

	@Override
	public void doInit() {
		logger.info("Request Url: " + URLCodecUtils.decoder(getReference().toString(), "utf-8") + ".");
		application = (ApiApplication) getApplication();
	}

	@Post("json")
	public String recognize(PostData data) {
		InputStream ian = new ByteArrayInputStream(data.getData());
		BufferedImage image = null;
		try {
			image = ImageIO.read(ian);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return application.recognizeFace(data.getFileName(), image);
	}

}
