package edu.hfut.lpr.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import javax.imageio.ImageIO;

import edu.hfut.lpr.utils.Configurator;

/**
 * 照片对象
 *
 * 实现AutoCloseable, Cloneable接口
 *
 * @author wanggang
 *
 */
public class Photo implements AutoCloseable, Cloneable {

	// Java原生的图像对象表达，缓冲图像类
	public BufferedImage image;

	public Photo(BufferedImage bi) {
		this.image = bi;
	}

	public Photo(InputStream is) throws IOException {
		this.loadImage(is);
	}

	/**
	 * 克隆对象
	 */
	@Override
	public Photo clone() throws CloneNotSupportedException {
		super.clone();
		return new Photo(duplicateBufferedImage(this.image));
	}

	/**
	 * 图像宽度
	 */
	public int getWidth() {
		return this.image.getWidth();
	}

	/**
	 * 图像长度
	 */
	public int getHeight() {
		return this.image.getHeight();
	}

	/**
	 * 返回默认的RGB彩色模型和彩色空间的像素值
	 */
	public int getRGB(int x, int y) {
		return this.image.getRGB(x, y);
	}

	/**
	 * 返回自身
	 */
	public BufferedImage getBi() {
		return this.image;
	}

	/**
	 * 返回自身，带有数轴
	 */
	public BufferedImage getBiWithAxes() {

		BufferedImage axis = new BufferedImage(this.image.getWidth() + 40, this.image.getHeight() + 40,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphicAxis = axis.createGraphics();

		graphicAxis.setColor(Color.LIGHT_GRAY);
		Rectangle backRect = new Rectangle(0, 0, this.image.getWidth() + 40, this.image.getHeight() + 40);
		graphicAxis.fill(backRect);
		graphicAxis.draw(backRect);

		graphicAxis.drawImage(this.image, 35, 5, null);

		graphicAxis.setColor(Color.BLACK);
		graphicAxis.drawRect(35, 5, this.image.getWidth(), this.image.getHeight());

		for (int ax = 0; ax < this.image.getWidth(); ax += 50) {
			graphicAxis.drawString(Integer.toString(ax), ax + 35, axis.getHeight() - 10);
			graphicAxis.drawLine(ax + 35, this.image.getHeight() + 5, ax + 35, this.image.getHeight() + 15);
		}
		for (int ay = 0; ay < this.image.getHeight(); ay += 50) {
			graphicAxis.drawString(Integer.toString(ay), 3, ay + 15);
			graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
		}
		graphicAxis.dispose();

		return axis;
	}

	/**
	 * 设置自身亮度
	 */
	public void setBrightness(int x, int y, float value) {
		this.image.setRGB(x, y, new Color(value, value, value).getRGB());
	}

	/**
	 * 设置某个图像亮度
	 */
	static public void setBrightness(BufferedImage image, int x, int y, float value) {
		image.setRGB(x, y, new Color(value, value, value).getRGB());
	}

	/**
	 * 获取某个图像的亮度，0～1.0
	 */
	static public float getBrightness(BufferedImage image, int x, int y) {
		// 对RGB栅格或光栅进行采样
		int r = image.getRaster().getSample(x, y, 0);
		int g = image.getRaster().getSample(x, y, 1);
		int b = image.getRaster().getSample(x, y, 2);
		// 返回值分别为：色度、饱和度、亮度
		float[] hsb = Color.RGBtoHSB(r, g, b, null);
		return hsb[2];
	}

	/**
	 * 获取某个图像的饱和度
	 */
	static public float getSaturation(BufferedImage image, int x, int y) {
		int r = image.getRaster().getSample(x, y, 0);
		int g = image.getRaster().getSample(x, y, 1);
		int b = image.getRaster().getSample(x, y, 2);
		float[] hsb = Color.RGBtoHSB(r, g, b, null);
		return hsb[1];
	}

	/**
	 * 获取某个图像的色度
	 */
	static public float getHue(BufferedImage image, int x, int y) {
		int r = image.getRaster().getSample(x, y, 0);
		int g = image.getRaster().getSample(x, y, 1);
		int b = image.getRaster().getSample(x, y, 2);
		float[] hsb = Color.RGBtoHSB(r, g, b, null);
		return hsb[0];
	}

	/**
	 * 获取自身的亮度
	 */
	public float getBrightness(int x, int y) {
		return Photo.getBrightness(this.image, x, y);
	}

	/**
	 * 获取自身的饱和度
	 */
	public float getSaturation(int x, int y) {
		return Photo.getSaturation(this.image, x, y);
	}

	/**
	 * 获取自身的色度
	 */
	public float getHue(int x, int y) {
		return Photo.getHue(this.image, x, y);
	}

	/**
	 * 将给定的图像转换成缓冲图
	 */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// 创建透明缓冲图对象
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// 将待转换图像绘制在缓冲图上
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		return bimage;
	}

	/**
	 * 加载图像
	 * @param is 输入流
	 * @throws IOException
	 */
	public void loadImage(InputStream is) throws IOException {
		BufferedImage image = ImageIO.read(is);

		BufferedImage outimage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g = outimage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		this.image = outimage;
	}

	/**
	 * 保存图像
	 * @param filepath 保存路径
	 * @throws IOException
	 */
	public void saveImage(String filepath) throws IOException {
		String type = filepath.substring(filepath.lastIndexOf('.') + 1, filepath.length()).toUpperCase();
		// 这里仅支持BMP、JPG、JPEG、PNG四种格式
		if (!type.equals("BMP") && !type.equals("JPG") && !type.equals("JPEG") && !type.equals("PNG")) {
			throw new IOException("Unsupported file format");
		}
		File destination = new File(filepath);
		ImageIO.write(this.image, type, destination);
	}

	/**
	 * 标准化亮度值
	 */
	public void normalizeBrightness(float coef) {
		Statistics stats = new Statistics(this);
		for (int x = 0; x < this.getWidth(); x++) {
			for (int y = 0; y < this.getHeight(); y++) {
				Photo.setBrightness(this.image, x, y,
						stats.thresholdBrightness(Photo.getBrightness(this.image, x, y), coef));
			}
		}
	}

	/**
	 * 自身大小线性缩放
	 */
	public void linearResize(int width, int height) {
		this.image = Photo.linearResizeBi(this.image, width, height);
	}

	/**
	 * 线性调整图像大小
	 * @param origin 待调整图像
	 * @param width 调整后宽度
	 * @param height 调整后长度
	 */
	static public BufferedImage linearResizeBi(BufferedImage origin, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		float xScale = (float) width / origin.getWidth();
		float yScale = (float) height / origin.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g.drawRenderedImage(origin, at);
		g.dispose();
		return resizedImage;
	}

	/**
	 * 自身大小平均缩放
	 */
	public void averageResize(int width, int height) {
		this.image = this.averageResizeBi(this.image, width, height);
	}

	/**
	 * 平均调整图像大小
	 * @param origin 待调整图像
	 * @param width 调整后宽度
	 * @param height 调整后长度
	 */
	public BufferedImage averageResizeBi(BufferedImage origin, int width, int height) {

		// 如果是图像增长或增宽，采用线性调整
		if ((origin.getWidth() < width) || (origin.getHeight() < height)) {
			return Photo.linearResizeBi(origin, width, height);
		}

		// 否则就是对图像的长和宽同时缩短
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// xScale > 1 AND yScale > 1
		float xScale = (float) origin.getWidth() / width;
		float yScale = (float) origin.getHeight() / height;

		// 选取点(x,y)对应原图中右上矩阵所有像素点的均值作为该点像素值
		for (int x = 0; x < width; x++) {
			int x0min = Math.round(x * xScale);
			int x0max = Math.round((x + 1) * xScale);

			for (int y = 0; y < height; y++) {
				int y0min = Math.round(y * yScale);
				int y0max = Math.round((y + 1) * yScale);

				float sum = 0;
				int sumCount = 0;

				for (int x0 = x0min; x0 < x0max; x0++) {
					for (int y0 = y0min; y0 < y0max; y0++) {
						sum += Photo.getBrightness(origin, x0, y0);
						sumCount++;
					}
				}
				sum /= sumCount;
				Photo.setBrightness(resized, x, y, sum);
			}
		}

		return resized;
	}

	/**
	 * 复制自身
	 */
	public Photo duplicate() {
		return new Photo(Photo.duplicateBufferedImage(this.image));
	}

	/**
	 * 复制某个图像
	 */
	static public BufferedImage duplicateBufferedImage(BufferedImage image) {
		BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageCopy.setData(image.getData());
		return imageCopy;
	}

	static public BufferedImage duplicateBufferedImage(BufferedImage image, String china) {
		BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageCopy.setData(image.getData());
		//		BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		//		Graphics2D g = imageCopy.createGraphics();
		//		g.drawImage(image, 0, 0, null);
		//		g.dispose();
		return imageCopy;
	}

	/**
	 * 图像二值化
	 */
	static void thresholding(BufferedImage bi) {
		short[] threshold = new short[256];
		for (short i = 0; i < 36; i++) {
			threshold[i] = 0;
		}
		for (short i = 36; i < 256; i++) {
			threshold[i] = i;
		}
		BufferedImageOp thresholdOp = new LookupOp(new ShortLookupTable(0, threshold), null);
		thresholdOp.filter(bi, bi);
	}

	/**
	 * 图像垂直边缘检测
	 */
	public void verticalEdgeDetector(BufferedImage source) {
		BufferedImage destination = Photo.duplicateBufferedImage(source);
		float data1[] = { -1, 0, 1, -2, 0, 2, -1, 0, 1, };
		// float data2[] = { 1, 0, -1, 2, 0, -2, 1, 0, -1, };
		new ConvolveOp(new Kernel(3, 3, data1), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
	}

	/**
	 * 获取缓冲图的亮度数组
	 */
	public float[][] bufferedImageToArray(BufferedImage image, int w, int h) {
		float[][] array = new float[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				array[x][y] = Photo.getBrightness(image, x, y);
			}
		}
		return array;
	}

	/**
	 * 获取含有边界的缓冲图的亮度数组
	 */
	public float[][] bufferedImageToArrayWithBounds(BufferedImage image, int w, int h) {
		float[][] array = new float[w + 2][h + 2];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				array[x + 1][y + 1] = Photo.getBrightness(image, x, y);
			}
		}
		for (int x = 0; x < (w + 2); x++) {
			array[x][0] = 1;
			array[x][h + 1] = 1;
		}
		for (int y = 0; y < (h + 2); y++) {
			array[0][y] = 1;
			array[w + 1][y] = 1;
		}

		return array;
	}

	/**
	 * 将亮度数组转换成缓冲图
	 */
	static public BufferedImage arrayToBufferedImage(float[][] array, int w, int h) {
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				Photo.setBrightness(bi, x, y, array[x][y]);
			}
		}
		return bi;
	}

	/**
	 * 创建空的缓冲图，基于某个图像
	 */
	static public BufferedImage createBlankBi(BufferedImage image) {
		return new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * 创建空的缓冲图，基于指定长宽
	 */
	public BufferedImage createBlankBi(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * 两个缓冲图叠加
	 */
	public BufferedImage sumBi(BufferedImage bi1, BufferedImage bi2) {
		BufferedImage out = new BufferedImage(Math.min(bi1.getWidth(), bi2.getWidth()), Math.min(bi1.getHeight(),
				bi2.getHeight()), BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < out.getWidth(); x++) {
			for (int y = 0; y < out.getHeight(); y++) {
				Photo.setBrightness(out, x, y,
						(float) Math.min(1.0, Photo.getBrightness(bi1, x, y) + Photo.getBrightness(bi2, x, y)));
			}
		}
		return out;
	}

	/**
	 * 简单图像二值化，针对当前图像
	 */
	public void plainThresholding(Statistics stat) {
		int w = this.getWidth();
		int h = this.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				this.setBrightness(x, y, stat.thresholdBrightness(this.getBrightness(x, y), 1.0f));
			}
		}
	}

	/**
	 * 自适应邻域图像二值化，针对当前图像
	 */
	public void adaptiveThresholding() {

		Statistics stat = new Statistics(this);
		int radius = Configurator.getConfigurator().getIntProperty("photo_adaptivethresholdingradius");
		if (radius == 0) {
			this.plainThresholding(stat);
			return;
		}

		int w = this.getWidth();
		int h = this.getHeight();

		float[][] sourceArray = this.bufferedImageToArray(this.image, w, h);
		float[][] destinationArray = this.bufferedImageToArray(this.image, w, h);

		int count;
		float neighborhood;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				// 计算邻域亮度均值
				count = 0;
				neighborhood = 0;
				for (int ix = x - radius; ix <= (x + radius); ix++) {
					for (int iy = y - radius; iy <= (y + radius); iy++) {
						if ((ix >= 0) && (iy >= 0) && (ix < w) && (iy < h)) {
							neighborhood += sourceArray[ix][iy];
							count++;
						}
						// else {
						// neighborhood += stat.average;
						// count++;
						// }
					}
				}
				neighborhood /= count;
				if (destinationArray[x][y] < neighborhood) {
					destinationArray[x][y] = 0f;
				} else {
					destinationArray[x][y] = 1f;
				}
			}
		}
		this.image = Photo.arrayToBufferedImage(destinationArray, w, h);
	}

	/**
	 * 自身图像Hough变换
	 */
	public HoughTransformation getHoughTransformation() {
		HoughTransformation hough = new HoughTransformation(this.getWidth(), this.getHeight());
		for (int x = 0; x < this.getWidth(); x++) {
			for (int y = 0; y < this.getHeight(); y++) {
				hough.addLine(x, y, this.getBrightness(x, y));
			}
		}
		return hough;
	}

	/**
	 * 重写equals
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Photo photo = (Photo) o;
		if (this.getWidth() != photo.getWidth() || this.getHeight() != photo.getHeight()) {
			return false;
		}

		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (this.getRGB(i, j) != this.getRGB(i, j)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 重写Hash实现
	 */
	@Override
	public int hashCode() {
		BigInteger rgbSum = BigInteger.ZERO;
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				rgbSum = rgbSum.add(BigInteger.valueOf(getRGB(i, j)));
			}
		}
		return rgbSum.hashCode();
	}

	/**
	 * 关闭资源
	 */
	@Override
	public void close() {
		this.image.flush();
	}

}