package edu.hfut.fr.image.processing.face.detection;

import java.util.List;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.image.Image;

/**
 * 面部检测数据集
 *
 *@author wanggang
 */
public class DatasetFaceDetector {

	private DatasetFaceDetector() {

	}

	public static <PERSON, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace> GroupedDataset<PERSON, ListDataset<FACE>, FACE> process(
			GroupedDataset<PERSON, ? extends ListDataset<IMAGE>, IMAGE> input, FaceDetector<FACE, IMAGE> detector) {
		final MapBackedDataset<PERSON, ListDataset<FACE>, FACE> output = new MapBackedDataset<PERSON, ListDataset<FACE>, FACE>();

		for (final PERSON group : input.getGroups()) {
			final ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();
			final ListDataset<IMAGE> instances = input.getInstances(group);

			for (int i = 0; i < instances.size(); i++) {
				final IMAGE img = instances.getInstance(i);
				final List<FACE> faces = detector.detectFaces(img);

				if (faces == null || faces.size() == 0) {
					System.err.println("There was no face detected in " + group + " instance " + i);
					// detected.add(null);
					continue;
				}

				if (faces.size() == 1) {
					detected.add(faces.get(0));
					continue;
				}

				detected.add(getBiggest(faces));
			}

			output.getMap().put(group, detected);
		}

		return output;
	}

	public static <IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace> ListDataset<FACE> process(
			List<IMAGE> instances, FaceDetector<FACE, IMAGE> detector) {
		final ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();

		for (int i = 0; i < instances.size(); i++) {
			final IMAGE img = instances.get(i);
			final List<FACE> faces = detector.detectFaces(img);

			if (faces == null || faces.size() == 0) {
				System.err.println("There was no face detected in instance " + i);
				continue;
			}

			if (faces.size() == 1) {
				detected.add(faces.get(0));
				continue;
			}

			detected.add(getBiggest(faces));
		}

		return detected;
	}

	/**
	 * 得到列表中最大的面部
	 */
	public static <FACE extends DetectedFace> FACE getBiggest(List<FACE> faces) {
		if (faces == null || faces.size() == 0)
			return null;

		int biggestIndex = 0;
		double biggestSize = faces.get(0).bounds.calculateArea();

		for (int i = 1; i < faces.size(); i++) {
			final double sz = faces.get(i).bounds.calculateArea();
			if (sz > biggestSize) {
				biggestSize = sz;
				biggestIndex = i;
			}
		}

		return faces.get(biggestIndex);
	}

}
