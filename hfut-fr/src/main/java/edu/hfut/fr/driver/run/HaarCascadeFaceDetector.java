package edu.hfut.fr.driver.run;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;

/**
 * Haar检测器实现类
 *
 * @author wanghao
 */
public class HaarCascadeFaceDetector implements Display, Runnable {

	/**
	 *  构造函数
	 */
	public HaarCascadeFaceDetector() {
		try {
			this.colorImage = ImageUtilities.readMBF(new File("faces_test/multifaces.jpg"));
			//			displayMBF(this.colorImage);
			this.faceDetector = new HaarCascadeDetector(20);
			this.faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.colorImage));
			for (DetectedFace face : this.faces) {
				this.colorImage.drawShape(face.getBounds(), RGBColour.RED);
			}
			displayMBF(this.colorImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HaarCascadeFaceDetector(String file) {
		try {
			this.colorImage = ImageUtilities.readMBF(new File(file));
			displayMBF(this.colorImage);
			this.faceDetector = new HaarCascadeDetector(20);
			this.faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.colorImage));
			for (DetectedFace face : this.faces) {
				this.colorImage.drawShape(face.getBounds(), RGBColour.RED);
			}
			displayMBF(this.colorImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HaarCascadeFaceDetector(String file, int num) {
		this.num = num;
		try {
			this.colorImage = ImageUtilities.readMBF(new File(file));
			displayMBF(this.colorImage);
			this.faceDetector = new HaarCascadeDetector(this.num);
			this.faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.colorImage));
			for (DetectedFace face : this.faces) {
				this.colorImage.drawShape(face.getBounds(), RGBColour.RED);
			}
			displayMBF(this.colorImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void displayMBF(MBFImage image) {
		DisplayUtilities.display(image);
	}

	@Override
	public void displayF(FImage image) {
		DisplayUtilities.display(image);
	}

	/**
	 * 多线程处理识别人脸
	 */
	@Override
	public void run() {
		// 可实现多个图片处理,实现自己的并行处理图片操作
		new HaarCascadeFaceDetector();
	}

	/**
	 * Get/Set 方法
	 */
	public MBFImage getColorImage() {
		return colorImage;
	}

	public void setColorImage(MBFImage colorImage) {
		this.colorImage = colorImage;
	}

	public FaceDetector<DetectedFace, FImage> getFaceDetector() {
		return faceDetector;
	}

	public void setFaceDetector(FaceDetector<DetectedFace, FImage> faceDetector) {
		this.faceDetector = faceDetector;
	}

	public List<DetectedFace> getFaces() {
		return faces;
	}

	public void setFaces(List<DetectedFace> faces) {
		this.faces = faces;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	private MBFImage colorImage = null;
	private FaceDetector<DetectedFace, FImage> faceDetector = null;
	private List<DetectedFace> faces = null;
	private int num = 0;

}
