package edu.hfut.api.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.api.resource.ApiResource;
import edu.hfut.fr.driver.run.verify.VerifyRecognition;

/**
 * 接口服务应用类
 *
 * @author wanggang
 *
 */
public class ApiApplication extends Application {

	private static Logger logger = LoggerFactory.getLogger(ApiApplication.class);

	HashMap<String, List<double[]>> corpus;

	public ApiApplication() {
		logger.info("加载训练集开始...");
		long t1 = System.currentTimeMillis();
		corpus = new HashMap<>();
		File[] DbFiles = new File("Face_DB").listFiles();
		File[] dbfiles = null;
		for (File fdb1 : DbFiles) {
			List<double[]> list = new ArrayList<>();
			dbfiles = fdb1.listFiles();
			for (File fdb2 : dbfiles) {
				try {
					list.add(FImage2DoubleFV.INSTANCE.extractFeature(ImageUtilities.readF(fdb2)).getVector());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			corpus.put(fdb1.getName(), list);
		}
		long t2 = System.currentTimeMillis();
		logger.info("加载训练集完成...");
		logger.info("训练集加载时间为：{}", (t2 - t1) + "ms");
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("", ApiResource.class);
		return router;
	}

	/**
	 * 插入站点组合数据
	 */
	public String recognizeFace(String fileName, BufferedImage image) {
		logger.info("Face:{} has high:{},width:{}.", fileName, image.getHeight(), image.getWidth());
		return VerifyRecognition.recognizeFaceName(corpus, image);
	}

}
