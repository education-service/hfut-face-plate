package edu.hfut.lpr.utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

/**
 * 可视化图片的工具类 - 打开GUI窗口 (JFrame)
 *
 * @author wanggang
 *
 */
public class SimpleImageViewer {

	public JFrame frame;
	BufferedImage img;
	public int WIDTH = 800;
	public int HEIGHT = 600;

	public SimpleImageViewer(BufferedImage img) {
		this.img = img;
		this.frame = new JFrame("WINDOW");
		this.frame.setVisible(true);

		this.start();
		this.frame.add(new JLabel(new ImageIcon(this.getImage())));

		this.frame.pack();
		//		frame.setSize(WIDTH, HEIGHT);
		// DISPOSE 比 EXIT 好
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public Image getImage() {
		return this.img;
	}

	public void start() {
		while (true) {
			BufferStrategy bs = this.frame.getBufferStrategy();
			if (bs == null) {
				this.frame.createBufferStrategy(4);
				return;
			}

			Graphics g = bs.getDrawGraphics();
			g.drawImage(this.img, 0, 0, this.WIDTH, this.HEIGHT, null);
			g.dispose();
			bs.show();
		}
	}

}
