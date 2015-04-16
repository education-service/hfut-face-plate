package edu.hfut.rpc.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.hfut.fr.driver.run.verify.VerifyRecognition;

public class RecognizeService implements Recognize {

	private static Logger logger = LoggerFactory.getLogger(RecognizeService.class);

	private static HashMap<String, List<double[]>> corpus;

	static {
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
	public String runRecognize(ImageData data) {
		InputStream ian = new ByteArrayInputStream(data.getData());
		BufferedImage image = null;
		try {
			image = ImageIO.read(ian);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("Face:{} has high:{},width:{}.", data.getFileName(), image.getHeight(), image.getWidth());
		return VerifyRecognition.recognizeFaceName(corpus, image);
	}

}
