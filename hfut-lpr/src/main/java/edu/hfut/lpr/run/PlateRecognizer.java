package edu.hfut.lpr.run;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.hfut.lpr.images.CarSnapshot;
import edu.hfut.lpr.tackle.TackleCore;

/**
 * 车牌识别类
 *
 * @author wanggang
 *
 */
public class PlateRecognizer {

	private static Logger logger = LoggerFactory.getLogger(PlateRecognizer.class);

	public static void main(String[] args) {
		String str = PlateRecognizer.recognizeResult(args[0]);
		//		String str = PlateRecognizer.recognizeResult("src/test/resources/zh-snapshots/test_006.jpg");
		System.out.println(str);
	}

	public static String recognizeResult(String imageFile) {
		logger.info("Recognizing ImageFile is {}", imageFile);
		try {
			CarSnapshot carSnap = new CarSnapshot(imageFile);
			TackleCore intel = new TackleCore();
			return intel.recognize(carSnap);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	public static String recognizeResult(BufferedImage imageFile) {
		logger.info("Recognizing ImageFile's width={}, hight={}", imageFile.getWidth(), imageFile.getHeight());
		try {
			CarSnapshot carSnap = new CarSnapshot(imageFile);
			TackleCore intel = new TackleCore();
			return intel.recognize(carSnap);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	public static String recognizeResult(InputStream imageFile) {
		try {
			CarSnapshot carSnap = new CarSnapshot(imageFile);
			TackleCore intel = new TackleCore();
			return intel.recognize(carSnap);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

}
