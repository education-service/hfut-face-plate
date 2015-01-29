package edu.hfut.fr.image.processing.face.detection.keypoints;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

public class FacialKeypointTest {

	static float FLOAT_EPS = 0.01f;

	FKEFaceDetector engine;
	FImage noface;
	FImage face;
	List<KEDetectedFace> k1;
	List<KEDetectedFace> k2;

	@Before
	public void setup() throws Exception {
		engine = new FKEFaceDetector();
		noface = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		face = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/face/ss.jpg"));

		k1 = engine.detectFaces(noface);
		k2 = engine.detectFaces(face);
	}

	@Test
	public void testNoFaces() {
		assertTrue(k1.size() == 0);
	}

	//	@Test public void testIO() throws IOException {
	//		File ascii = null;
	//		File binary = null;
	//
	//		try {
	//			ascii = File.createTempFile("facetest", "ascii");
	//			binary = File.createTempFile("facetest", "bin");
	//
	//			IOUtils.writeASCII(ascii, k2);
	//			IOUtils.writeBinary(binary, k2);
	//
	//			//test ascii read
	//			List<FacialDescriptor> asciiKeys = MemoryLocalFeatureList.read(ascii, FacialDescriptor.class);
	//			List<FacialDescriptor> asciiKeys2 = k2;
	//
	//			assertTrue(asciiKeys.size() == 1);
	//			assertTrue(asciiKeys.size() == asciiKeys2.size());
	//
	//			FacialDescriptor fpk1 = asciiKeys.get(0);
	//			FacialDescriptor fpk2 = asciiKeys2.get(0);
	//
	//			assertEquals(fpk1.featureLength,fpk2.featureLength);
	//			assertEquals(fpk1.featureRadius,fpk2.featureRadius);
	//			assertEquals(fpk1.featureVector.length,fpk2.featureVector.length);
	//			assertEquals(fpk1.nFeatures,fpk2.nFeatures);
	//			for(int i = 0; i < fpk1.featureVector[i]; i++) assertTrue(fpk1.featureVector[i] == fpk2.featureVector[i]);
	//
	//			//test binary read
	//			List<FacialDescriptor> binKeys = MemoryLocalFeatureList.read(binary, FacialDescriptor.class);
	//			assertTrue(asciiKeys.size() == binKeys.size());
	//			fpk2 = binKeys.get(0);
	//			assertEquals(fpk1.featureLength,fpk2.featureLength);
	//			assertEquals(fpk1.featureRadius,fpk2.featureRadius);
	//			assertEquals(fpk1.featureVector.length,fpk2.featureVector.length);
	//			assertEquals(fpk1.nFeatures,fpk2.nFeatures);
	//			for(int i = 0; i < fpk1.featureVector[i]; i++) assertTrue(fpk1.featureVector[i] == fpk2.featureVector[i]);
	//		} finally {
	//			if (ascii != null) ascii.delete();
	//			if (binary != null) binary.delete();
	//		}
	//	}

}
