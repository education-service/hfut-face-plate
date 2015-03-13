package edu.hfut.fr.driver.run.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.FImage2DoubleFV;

import edu.hfut.fr.image.processing.face.alignment.AffineAligner;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.FKEFaceDetector;
import edu.hfut.fr.image.processing.face.detection.keypoints.KEDetectedFace;

/**
 * 面部识别
 *
 * @author wanghao
 */
public class FaceRecognitionVerify {

	public static void main(String[] args) throws IOException {

		//************************训练样本图片的程序输入***********************************
		System.out.println("将训练样本图片放入Face_Sample文件下,储存面部图像");

		File FaceInput = new File("Face_Sample/");

		//************************图像矫正,并存储***********************************
		//FKEFaceDetector

		String face_name = "";
		String Face_DbUrl = "Face_DB/";

		MBFImage colorImage = null;
		File[] name_lists = FaceInput.listFiles();

		for (File f1 : name_lists) {
			//为每个用户创建数据库
			face_name = f1.getName();
			File tmp = new File(Face_DbUrl + face_name);
			tmp.mkdir();

			File[] face_pictures = f1.listFiles();
			int i = 1;
			for (File f2 : face_pictures) {
				colorImage = ImageUtilities.readMBF(f2);
				FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
				List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(colorImage));
				AffineAligner faceAligner = new AffineAligner();
				for (KEDetectedFace face : faces) {
					FImage faceFA = faceAligner.align(face);
					//					String s = Face_DbUrl + tmp.getName() + "/" + i + ".pgm";
					//					System.out.println(s);
					ImageUtilities.write(faceFA, "png", new File(Face_DbUrl + tmp.getName() + "/" + i + ".png"));
				}
				i++;
			}

		}

		//*************************对比数据库,进行人物分类******************************
		String Face_TestUrl = "Face_Test/";

		File[] TestFiles = new File(Face_TestUrl).listFiles();
		File[] DbFiles = new File("Face_DB").listFiles();
		File[] dbfiles = null;
		//		int PeopleInDb = new File(Face_DbUrl).listFiles().length;

		FImage fimage1 = null, fimage2;
		MBFImage mbimage;
		DoubleFV fv1, fv2;
		for (File f : TestFiles) {
			//			File f = new File("Face_Test/8.png");
			//识别每个用户
			//			System.out.println("This is " + f.getName());

			String rightname = "";
			mbimage = ImageUtilities.readMBF(new File(Face_TestUrl + f.getName()));
			DisplayUtilities.display(mbimage);
			FaceDetector<KEDetectedFace, FImage> faceDetector = new FKEFaceDetector(20);
			List<KEDetectedFace> faces = faceDetector.detectFaces(Transforms.calculateIntensity(mbimage));
			AffineAligner faceAligner = new AffineAligner();
			for (KEDetectedFace face : faces) {
				fimage1 = faceAligner.align(face);
			}
			fv1 = FImage2DoubleFV.INSTANCE.extractFeature(fimage1);
			double MinDistances = Double.MAX_VALUE;
			for (File fdb1 : DbFiles) {
				dbfiles = fdb1.listFiles();
				double distances = 0;

				for (File fdb2 : dbfiles) {
					fimage2 = ImageUtilities.readF(fdb2);
					fv2 = FImage2DoubleFV.INSTANCE.extractFeature(fimage2);
					distances = distances + distance(fv1.getVector(), fv2.getVector(), fv1.getVector().length);

				}
				if (distances < MinDistances) {
					MinDistances = distances;
					rightname = fdb1.getName();
				}
			}
			System.out.println("the " + f.getName() + "*******->identity is " + rightname + "*");
		}
		System.out.println("Finsh!");

	}

	/**
	 * 使用平方差和衡量距离
	 */
	public static double distance(double[] v1, double[] v2, int dim) {
		float dis = 0.0f;
		for (int i = 0; i < dim; i++) {
			dis += Math.pow(Math.abs(v1[i] - v2[i]), 2);
		}
		return Math.sqrt(dis);
	}

}
