package edu.hfut.fr.driver.run;

/**
 *  不同方法的调用比较
 *
 * @author wanghao
 */
public class DifferentDetectors {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		HaarCascadeFaceDetector haarCascadeFaceDetector = new HaarCascadeFaceDetector();
		FKEFacePointDetector fkeFacePointDetector = new FKEFacePointDetector();

		/*
		 * 多线程测试
		 */
		/*
		Thread t1 = new Thread(haarCascadeFaceDetector);
		Thread t2 = new Thread(fkeFacePointDetector);

		t1.start();
		t2.start();
		*/
	}

}
