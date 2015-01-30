package edu.hfut.lpr.frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import edu.hfut.lpr.utils.Configurator;

/**
 * 生成报告
 *
 * @author wanggang
 *
 */
public class ReportGenerator {

	private String directory;
	private String output;
	// private BufferedWriter out;
	private boolean enabled;

	public ReportGenerator(String directory) throws IOException {
		this.directory = directory;
		this.enabled = true;

		File f = new File(directory);
		if (!f.exists() || !f.isDirectory()) {
			throw new IOException("Report directory '" + directory + "' doesn't exist or isn't a directory");
		}

		this.output = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" + "<html>"
				+ "<head><title>车牌识别报告</title>" + "</head>" + "<style type=\"text/css\">" + "@import \"style.css\";"
				+ "</style>";
	}

	public ReportGenerator() {
		this.enabled = false;
	}

	/**
	 * 插入文本
	 */
	public void insertText(String text) {
		if (!this.enabled) {
			return;
		}
		this.output += text;
		this.output += "\n";
	}

	/**
	 * 插入图像
	 * @param image 图像
	 * @param cls 类名
	 * @param w 宽度
	 * @param h 长度
	 */
	public void insertImage(BufferedImage image, String cls, int w, int h) throws IllegalArgumentException, IOException {
		if (!this.enabled) {
			return;
		}

		String imageName = String.valueOf(image.hashCode()) + ".jpg";
		this.saveImage(image, imageName);

		if ((w != 0) && (h != 0)) {
			this.output += "<img src='" + imageName + "' alt='' width='" + w + "' height='" + h + "' class='" + cls
					+ "'>\n";
		} else {
			this.output += "<img src='" + imageName + "' alt='' class='" + cls + "'>\n";
		}
	}

	/**
	 * 完成
	 */
	public void finish() throws IOException {
		if (!this.enabled) {
			return;
		}
		this.output += "</html>";
		FileOutputStream os = new FileOutputStream(this.directory + File.separator + "index.html");
		Writer writer = new OutputStreamWriter(os);
		writer.write(this.output);
		writer.flush();
		writer.close();

		String cssPath = Configurator.getConfigurator().getPathProperty("reportgeneratorcss");
		InputStream inStream = Configurator.getConfigurator().getResourceAsStream(cssPath);

		this.saveStreamToFile(inStream, new File(this.directory + File.separator + "style.css"));
	}

	/**
	 * 保存到文件中
	 */
	public void saveStreamToFile(InputStream inStream, File out) throws IOException {
		FileOutputStream outStream = new FileOutputStream(out);

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = inStream.read(bytes)) != -1) {
			outStream.write(bytes, 0, read);
		}

		outStream.close();
		inStream.close();
	}

	/**
	 * 保存图像
	 */
	public void saveImage(BufferedImage bi, String filename) throws IOException, IllegalArgumentException {
		if (!this.enabled) {
			return;
		}

		String type = new String(filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase());

		if (!type.equals("bmp") && !type.equals("jpg") && !type.equals("jpeg") && !type.equals("png")) {
			throw new IllegalArgumentException("Unsupported file format");
		}

		File destination = new File(this.directory + File.separator + filename);
		try {
			ImageIO.write(bi, type, destination);
		} catch (IOException e) {
			throw new IOException("Can't open destination report directory", e);
		}
	}

}