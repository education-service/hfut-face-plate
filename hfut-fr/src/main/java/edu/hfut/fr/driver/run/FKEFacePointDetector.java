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
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * FKE人脸点检测器
 *
 * @author wanggang
 *
 */
public class FKEFacePointDetector implements Display, Runnable {

	/**
	 *  构造函数
	 */
	public FKEFacePointDetector() {
		try {
			this.colorImage = ImageUtilities.readMBF(new File("faces_test/multifaces.jpg"));
			displayMBF(this.colorImage);
			this.faceDetector = new FKEFaceDetector(20);
			this.faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.colorImage));
			for (DetectedFace face : this.faces) {
				this.colorImage.drawShape(face.getBounds(), RGBColour.RED);
			}
			displayMBF(this.colorImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FKEFacePointDetector(String file) {
		try {
			this.colorImage = ImageUtilities.readMBF(new File(file));
			displayMBF(this.colorImage);
			this.faceDetector = new FKEFaceDetector(20);
			this.faces = faceDetector.detectFaces(Transforms.calculateIntensity(this.colorImage));
			for (DetectedFace face : this.faces) {
				this.colorImage.drawShape(face.getBounds(), RGBColour.RED);
			}
			displayMBF(this.colorImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FKEFacePointDetector(String file, int num) {
		this.num = num;
		try {
			this.colorImage = ImageUtilities.readMBF(new File(file));
			displayMBF(this.colorImage);
			this.faceDetector = new FKEFaceDetector(this.num);
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
		new FKEFacePointDetector();
	}

	/**
	 * Get/Set 方法
	 * @return
	 */
	public MBFImage getColorImage() {
		return colorImage;
	}

	public void setColorImage(MBFImage colorImage) {
		this.colorImage = colorImage;
	}

	public FaceDetector<KEDetectedFace, FImage> getFaceDetector() {
		return faceDetector;
	}

	public void setFaceDetector(FaceDetector<KEDetectedFace, FImage> faceDetector) {
		this.faceDetector = faceDetector;
	}

	public List<KEDetectedFace> getFaces() {
		return faces;
	}

	public void setFaces(List<KEDetectedFace> faces) {
		this.faces = faces;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	private MBFImage colorImage = null;
	private FaceDetector<KEDetectedFace, FImage> faceDetector = null;
	private List<KEDetectedFace> faces = null;
	private int num = 0;

}
