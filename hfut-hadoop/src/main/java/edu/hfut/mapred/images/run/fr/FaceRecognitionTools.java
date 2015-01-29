package edu.hfut.mapred.images.run.fr;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.openimaj.image.FImage;

/**
 * 人脸识别工具类
 *
 * 命令行：
 * java -jar hfut-hadoop-jar-with-dependencies.jar faceRecognitionTools hdfs://localhost:9000/user/hadoop/faces_db
 * bin/hadoop -jar hfut-hadoop-jar-with-dependencies.jar faceRecognitionTools hdfs://localhost:9000/user/hadoop/faces_db
 *
 * @author wanggang
 *
 */
public class FaceRecognitionTools {

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: SequenceFileReadDemo <input path>");
			System.exit(-1);
		}
		String uri = args[0];

		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		Path path = new Path(uri);

		FileStatus[] status = fs.listStatus(path);
		Path[] listedPaths = FileUtil.stat2Paths(status);
		for (Path p : listedPaths) {
			System.out.println(p.getName());
		}

		//		SequenceFile.Reader reader = null;
		//		try {
		//			reader = new SequenceFile.Reader(fs, path, conf);
		//			Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
		//			Writable value = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);
		//			long position = reader.getPosition();
		//			while (reader.next(key, value)) {
		//				String syncSeen = reader.syncSeen() ? "*" : "";
		//				System.out.printf("[%s%s]\t%s\t%s\n", position, syncSeen, key, value);
		//				position = reader.getPosition();
		//			}
		//		} finally {
		//			IOUtils.closeStream(reader);
		//		}

	}

	/**
	 * 需要根据实际需求实现
	 * @param faceDbPath
	 * @param grayImage
	 * @return
	 */
	public static String recognizeDataset(String faceDbPath, FImage grayImage) {
		return "";
	}

}
