package edu.hfut.lpr.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import edu.hfut.lpr.analysis.CarSnapshot;
import edu.hfut.lpr.gui.utils.ReportGenerator;
import edu.hfut.lpr.utils.TestUtility;

/**
 * ReportGenerator测试类 {@link ReportGenerator}
 *
 * @author wanggang
 *
 */
public class ReportGeneratorTest {

	TestUtility testUtility = new TestUtility();

	/**
	 * 使用无效的输入数据测试 {@link ReportGenerator#insertImage(BufferedImage, String, int, int)}
	 */
	@Test
	public void testInsertImage_Valid() throws IllegalArgumentException, IOException {
		final int w = 1;
		try (final CarSnapshot carSnapshot = new CarSnapshot("en-snapshots/test_002.jpg");) {
			final BufferedImage image = carSnapshot.renderGraph();
			final String cls = "test";
			final int h = 1;
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			reportGenerator.insertImage(image, cls, w, h);
		} catch (final Exception e) {
			fail();
		}
	}

	/**
	 * 使用无效的输入数据测试抛出异常 {@link ReportGenerator#insertImage(BufferedImage, String, int, int)}
	 */
	@Test
	public void testInsertImage_BadInput() throws IllegalArgumentException, IOException {
		try (final CarSnapshot carSnapshot = new CarSnapshot("en-snapshots/test_00.jpg");) {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			final int w = 1;
			final BufferedImage image = carSnapshot.renderGraph();
			final String cls = "test";
			final int h = 1;
			reportGenerator.insertImage(image, cls, w, h);
		} catch (final Exception e) {
			assertEquals("input == null!", e.getMessage());
		}
	}

	/**
	 * 测试null输入 {@link ReportGenerator#insertText(String)}
	 */
	@Test
	public void testInsertText_NullInput() throws IllegalArgumentException, IOException {
		try {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			reportGenerator.insertText(null);
		} catch (final Exception e) {
			fail();
		}
	}

	/**
	 * 测试空字符串输入 {@link ReportGenerator#insertText(String)}
	 */
	@Test
	public void testInsertText_EmptyInput() throws IllegalArgumentException, IOException {
		try {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			reportGenerator.insertText("");
		} catch (final Exception e) {
			fail();
		}
	}

	/**
	 * 测试null输入流 {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)}
	 */
	@Test
	public void testSaveStreamToFile_InvalidInput() throws IllegalArgumentException, IOException {
		try {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			final File io = new File("target/test-classes/out.txt");
			reportGenerator.saveStreamToFile(null, io);
		} catch (final Exception e) {
			assertEquals(null, e.getMessage());
		}
	}

	/**
	 * 测试null输出流 {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)}
	 */
	@Test
	public void testSaveStreamToFile_InvalidOutput() throws IllegalArgumentException, IOException {
		try {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			final InputStream inStream = new FileInputStream("src/test/resources/en-snapshots/test_002.jpg");
			reportGenerator.saveStreamToFile(inStream, null);
		} catch (final Exception e) {
			assertEquals(null, e.getMessage());
		}
	}

	/**
	 * 测试无效的输入输出流 {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)}
	 */
	@Test
	public void testSaveStreamToFile_Valid() throws IllegalArgumentException, IOException {
		final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
		final InputStream inStream = new FileInputStream("src/test/resources/en-snapshots/test_002.jpg");
		final File io = new File("target/test-classes/out.txt");
		reportGenerator.saveStreamToFile(inStream, io);
		StringBuilder sb = new StringBuilder();
		sb = testUtility.readFile("target/test-classes/out.txt");
		assertEquals(true, sb.toString().contains("Hewlett-Packard"));
	}

	/**
	 * 测试无效的输入 {@link ReportGenerator#saveImage(BufferedImage, String)}
	 */
	@Test
	public void testSaveImage_Valid() throws IllegalArgumentException, IOException {
		try (final CarSnapshot carSnapshot = new CarSnapshot("en-snapshots/test_002.jpg");) {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			final BufferedImage image = carSnapshot.renderGraph();
			reportGenerator.saveImage(image, "png");
		} catch (final Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * 测试无效的字符串输入和无效的图像输入 {@link ReportGenerator#saveImage(BufferedImage, String)}
	 */
	@Test
	public void testSaveImage_InvalidInput() throws IllegalArgumentException, IOException {
		try (final CarSnapshot carSnapshot = new CarSnapshot("en-snapshots/test_002.jpg");) {
			final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
			final BufferedImage image = carSnapshot.renderGraph();
			reportGenerator.saveImage(image, "target/test-classes/txt");
		} catch (final Exception e) {
			assertEquals("Unsupported file format", e.getMessage());
		}
	}

}