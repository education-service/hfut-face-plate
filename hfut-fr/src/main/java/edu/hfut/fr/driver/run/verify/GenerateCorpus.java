package edu.hfut.fr.driver.run.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 生成训练数据
 * 将训练样本图片放入Face_Sample文件下,储存面部图像"
 *
 * @author wanggang
 *
 */
public class GenerateCorpus {

	public static void main(String[] args) throws IOException {

		File FaceInput = new File("Face_Sample/");

		// ************************图像矫正,并存储***********************************
		// FKEFaceDetector
		String face_name = "";
		String Face_DbUrl = "Face_DB/";
		MBFImage colorImage = null;
		File[] name_lists = FaceInput.listFiles();
		// 循环每个人脸库
		for (File f1 : name_lists) {
			face_name = f1.getName();
			File tmp = new File(Face_DbUrl + face_name);
			tmp.mkdir();
			// 获取每个人脸库中的图片
			File[] face_pictures = f1.listFiles();
			int i = 1;
			for (File f2 : face_pictures) {
				System.out.println(f1.getName() + "," + f2.getName());
				colorImage = ImageUtilities.readMBF(f2);
				AffineAligner faceAligner = new AffineAligner();
				int max = Integer.MIN_VALUE;
				FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
				List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
				// 找出检测中的最大图像块即为人脸
				KEDetectedFace maxFace = null;
				for (KEDetectedFace face : faces) {
					if (face.getFacePatch().getHeight() > max) {
						max = face.getFacePatch().getHeight();
						maxFace = face;
					}
				}
				// 如果检测到则录入样本库
				if (maxFace != null) {
					FImage faceFA = faceAligner.align(maxFace);
					ImageUtilities.write(faceFA, "png", new File(Face_DbUrl + tmp.getName() + "/" + i + ".png"));
				}
				i++;
			}

		}

	}

}
