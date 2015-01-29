package edu.hfut.fr.image.processing.face.recognition.benchmarking.dataset;

import java.io.File;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * 人脸数据集
 *
 * @author jimbo
 */
@DatasetDescription(name = "Our Database of Faces/The ORL Face Database/The AT&T Face database", description = "Our Database of Faces, (formerly 'The ORL Database of Faces'), "
		+ "contains a set of face images taken between April 1992 and April 1994 "
		+ "at the lab. The database was used in the context of a face recognition "
		+ "project carried out in collaboration with the Speech, Vision and "
		+ "Robotics Group of the Cambridge University Engineering Department. "
		+ "There are ten different images of each of 40 distinct subjects. "
		+ "For some subjects, the images were taken at different times, varying "
		+ "the lighting, facial expressions (open / closed eyes, smiling / not smiling) "
		+ "and facial details (glasses / no glasses). "
		+ "All the images were taken against a dark homogeneous background with the "
		+ "subjects in an upright, frontal position (with tolerance for some side "
		+ "movement). A preview image of the Database of Faces is available.", url = "http://www.cl.cam.ac.uk/research/dtg/attarchive/facedatabase.html")
@Reference(type = ReferenceType.Inproceedings, author = { "Samaria, F.S.", "Harter, A.C." }, title = "Parameterisation of a stochastic model for human face identification", year = "1994", booktitle = "Applications of Computer Vision, 1994., Proceedings of the Second IEEE Workshop on", pages = {
		"138 ", "142" }, month = "dec")
public class ATandTDataset extends MapBackedDataset<Integer, ListDataset<FImage>, FImage> {

	/**
	 * 数据集构造函数
	 */
	public ATandTDataset() throws IOException {
		this(new File(System.getProperty("user.home"), "Data/att_faces"));
	}

	/**
	 *	构造函数
	 */
	public ATandTDataset(File baseDir) throws IOException {
		super();

		for (int s = 1; s <= 40; s++) {
			final ListBackedDataset<FImage> list = new ListBackedDataset<FImage>();
			map.put(s, list);

			for (int i = 1; i <= 10; i++) {
				final File file = new File(baseDir, "s" + s + "/" + i + ".pgm");

				final FImage image = ImageUtilities.readF(file);

				list.add(image);
			}
		}
	}

}
