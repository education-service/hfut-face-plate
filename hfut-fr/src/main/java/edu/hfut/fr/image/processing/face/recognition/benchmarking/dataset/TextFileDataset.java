package edu.hfut.fr.image.processing.face.recognition.benchmarking.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * 目标人物与他们对应的图片的数据集
 *
 *@author jimbo
 */
public class TextFileDataset extends MapBackedDataset<String, ListDataset<FImage>, FImage> {

	private class LazyImageList extends AbstractList<FImage> {

		List<File> files = new ArrayList<File>();

		@Override
		public FImage get(int index) {
			File f = files.get(index);

			if (f.isAbsolute()) {
				try {
					return ImageUtilities.readF(f);
				} catch (IOException e) {
					logger.warn(e);
					return null;
				}
			} else {
				try {
					return ImageUtilities.readF(new File(file.getParentFile(), f.toString()));
				} catch (IOException e) {
					logger.warn(e);
					return null;
				}
			}
		}

		@Override
		public int size() {
			return files.size();
		}

		@Override
		public String toString() {
			return files.toString();
		}
	}

	private static final Logger logger = Logger.getLogger(TextFileDataset.class);
	private String separator = ",";
	File file;
	BufferedWriter writer;

	/**
	 * 使用指定文件进行初始化
	 */
	public TextFileDataset(File file) throws IOException {
		this(file, ",");
	}

	/**
	 * 构造函数
	 */
	public TextFileDataset(File file, String separator) throws IOException {
		this.file = file;
		this.separator = separator;

		if (file.exists())
			read();
		else
			openWriter();
	}

	@Override
	protected void finalize() throws Throwable {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}

		super.finalize();
	}

	private void read() throws IOException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));

			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(separator);

				addInternal(parts[0].trim(), new File(parts[1].trim()));
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
	}

	private void addInternal(String person, File file) {
		ListBackedDataset<FImage> list = (ListBackedDataset<FImage>) map.get(person);

		if (list == null)
			map.put(person, list = new ListBackedDataset<FImage>(new LazyImageList()));
		((LazyImageList) list.getList()).files.add(file);
	}

	/**
	 * 向数据集中添加一个实例
		*/
	public void add(String person, File file) throws IOException {
		if (writer == null)
			openWriter();

		writer.write(person + separator + file.getAbsolutePath() + "\n");
		writer.flush();

		addInternal(person, file);
	}

	private void openWriter() throws IOException {
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			writer = null;
			throw e;
		}
	}

	@Override
	public String toString() {
		return "Text File Dataset (" + file + ")";
	}

}
