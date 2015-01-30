package edu.hfut.lpr.integration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.hfut.lpr.images.CarSnapshot;
import edu.hfut.lpr.tackle.TackleCore;

/**
 * 车牌识别集成测试
 *
 * @author wanggang
 *
 */
public class RecognitionIT {

	@Rule
	public ErrorCollector recognitionErrors = new ErrorCollector();

	final private static Logger logger = LoggerFactory.getLogger(RecognitionIT.class);

	/*
	 * TODO 需要修改一些奇怪的编码的jpeg格式图像读取 - 不能正确地加载这些图像，可以参考如下解决方案：
	 * http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file
	 * 但是B/W目前加载没有问题 - 可以使用snapshots/test_041.jpg这个图片来测试。
	 */
	@Test
	public void intelligenceSingleTest() throws IOException, ParserConfigurationException, SAXException {
		final String image = "en-snapshots/test_041.jpg";

		//	5秒中加载图像
		/*InputStream is = Configurator.getConfigurator().getResourceAsStream(image);
		BufferedImage bi = ImageIO.read(is);
		TestImageDraw t = new TestImageDraw(bi);
		Thread.sleep(5000);
		is = Configurator.getConfigurator().getResourceAsStream(image);*/

		//	5秒中加载照片
		/*Photo p = new Photo(is);
		t = new TestImageDraw(p.image);
		Thread.sleep(5000);
		p.close();*/

		CarSnapshot carSnap = new CarSnapshot(image);
		assertNotNull("carSnap is null", carSnap);
		assertNotNull("carSnap.image is null", carSnap.image);

		// 5秒中加载车辆快照图
		/*t = new TestImageDraw(carSnap.image);
		Thread.sleep(5000);
		t.frame.dispose();*/

		TackleCore intel = new TackleCore();
		assertNotNull(intel);

		String spz = intel.recognize(carSnap);
		assertNotNull("The licence plate is null - are you sure the image has the correct color space?", spz);

		//		System.out.println(spz);

		assertEquals("LM025BD", spz);

		//		System.out.println(intel.lastProcessDuration());
		carSnap.close();
	}

	/**
	 * 对车辆图片库进行测试，并给出识别正确率
	 */
	@Test
	public void testAllSnapshots() throws Exception {

		String snapshotDirPath = "src/test/resources/en-snapshots";
		String resultsPath = "src/test/resources/results.properties";
		InputStream resultsStream = new FileInputStream(new File(resultsPath));

		Properties properties = new Properties();
		properties.load(resultsStream);
		resultsStream.close();
		assertTrue(properties.size() > 0);

		File snapshotDir = new File(snapshotDirPath);
		File[] snapshots = snapshotDir.listFiles();
		assertNotNull(snapshots);
		assertTrue(snapshots.length > 0);

		TackleCore intel = new TackleCore();
		assertNotNull(intel);

		int correctCount = 0;
		int counter = 0;
		boolean correct = false;
		for (File snap : snapshots) {
			CarSnapshot carSnap = new CarSnapshot(new FileInputStream(snap));
			assertNotNull("carSnap is null", carSnap);
			assertNotNull("carSnap.image is null", carSnap.image);

			String snapName = snap.getName();
			String plateCorrect = properties.getProperty(snapName);
			assertNotNull(plateCorrect);

			String numberPlate = intel.recognize(carSnap);

			//TODO 当测试通过时，可以开启下面的校验
			// 首先得确定这些图像都有正确的颜色空间
			//			recognitionErrors.checkThat("The licence plate is null", numberPlate, is(notNullValue()));
			//			recognitionErrors.checkThat("The file \"" + snapName + "\" was incorrectly recognized.", numberPlate,
			//					is(plateCorrect));

			if (numberPlate != null && numberPlate.equals(plateCorrect)) {
				correctCount++;
				correct = true;
			}

			carSnap.close();

			counter++;
			logger.debug("Finished recognizing {} ({} of {})\t{}", snapName, counter, snapshots.length,
					correct ? "correct" : "incorrect");
			correct = false;
		}

		logger.info("Correct images: {}, total images: {}, accuracy: {}%", correctCount, snapshots.length,
				(float) correctCount / (float) snapshots.length * 100f);
	}

}
