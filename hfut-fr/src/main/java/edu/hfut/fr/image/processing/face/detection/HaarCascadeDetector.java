package edu.hfut.fr.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.hash.HashCodeUtil;
import org.openimaj.util.pair.ObjectIntPair;

import edu.hfut.fr.image.objectdetection.filtering.DetectionFilter;
import edu.hfut.fr.image.objectdetection.filtering.OpenCVGrouping;
import edu.hfut.fr.image.objectdetection.haar.Detector;
import edu.hfut.fr.image.objectdetection.haar.OCVHaarLoader;
import edu.hfut.fr.image.objectdetection.haar.StageTreeClassifier;
import edu.hfut.fr.image.processing.algorithm.EqualisationProcessor;

/**
 * HaarCascade检测器类
 *
 * @author wanggang
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Viola, P.", "Jones, M." }, title = "Rapid object detection using a boosted cascade of simple features", year = "2001", booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on", pages = {
		" I", "511 ", " I", "518 vol.1" }, number = "", volume = "1", customData = {
		"keywords",
		" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
		"doi", "10.1109/CVPR.2001.990517", "ISSN", "1063-6919 " })
public class HaarCascadeDetector implements FaceDetector<DetectedFace, FImage> {

	public enum BuiltInCascade {
		/**
		 * 眼睛检测
		 */
		eye("haarcascade_eye.xml"),
		/**
		 * 带有眼镜的眼镜检测
		 */
		eye_tree_eyeglasses("haarcascade_eye_tree_eyeglasses.xml"),
		/**
		 * 正面脸部检测
		 */
		frontalface_alt("haarcascade_frontalface_alt.xml"),
		/**
		 * 正面脸部检测
		 */
		frontalface_alt2("haarcascade_frontalface_alt2.xml"),
		/**
		 * 正面脸部检测
		 */
		frontalface_alt_tree("haarcascade_frontalface_alt_tree.xml"),

		frontalface_default("haarcascade_frontalface_default.xml"),
		/**
		 * 全身检测
		 */
		fullbody("haarcascade_fullbody.xml"),
		/**
		 * 左眼检测
		 */
		lefteye_2splits("haarcascade_lefteye_2splits.xml"),
		/**
		 */
		lowerbody("haarcascade_lowerbody.xml"),
		/**
		 */
		mcs_eyepair_big("haarcascade_mcs_eyepair_big.xml"),
		/**
		 */
		mcs_eyepair_small("haarcascade_mcs_eyepair_small.xml"),
		/**
		 * 左眼检测
		 */
		mcs_lefteye("haarcascade_mcs_lefteye.xml"),
		/**
		 * 嘴部检验
		 */
		mcs_mouth("haarcascade_mcs_mouth.xml"),
		/**
		 * 鼻子检验
		 */
		mcs_nose("haarcascade_mcs_nose.xml"),
		/**
		 * 右眼检验
		 */
		mcs_righteye("haarcascade_mcs_righteye.xml"),
		/**
		 * 上身检验
		 */
		mcs_upperbody("haarcascade_mcs_upperbody.xml"),
		/**
		 * 侧部面部检验
		 */
		profileface("haarcascade_profileface.xml"),
		/**
		 * 右眼检测
		 */
		righteye_2splits("haarcascade_righteye_2splits.xml"),
		/**
		 * 上身检测
		 */
		upperbody("haarcascade_upperbody.xml");

		private String classFile;

		private BuiltInCascade(String classFile) {
			this.classFile = classFile;
		}

		/**
		 * 返回分类文件
		 */
		public String classFile() {
			return classFile;
		}

		/**
		 * 创建新的检测器
		 */
		public HaarCascadeDetector load() {
			try {
				return new HaarCascadeDetector(classFile);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected Detector detector;
	protected DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> groupingFilter;
	protected boolean histogramEqualize = false;

	/**
	 * 构造函数
	 */
	public HaarCascadeDetector(String cas) {
		try {
			setCascade(cas);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		groupingFilter = new OpenCVGrouping();
	}

	public HaarCascadeDetector() {
		this(BuiltInCascade.frontalface_default.classFile());
	}

	public HaarCascadeDetector(int minSize) {
		this();
		this.detector.setMinimumDetectionSize(minSize);
	}

	public HaarCascadeDetector(String cas, int minSize) {
		this(cas);
		this.detector.setMinimumDetectionSize(minSize);
	}

	/**
	 * 返回最小检测大小
	 */
	public int getMinSize() {
		return this.detector.getMinimumDetectionSize();
	}

	/**
	 * 设置最小检测大小
	 *
	 */
	public void setMinSize(int size) {
		this.detector.setMinimumDetectionSize(size);
	}

	/**
	 * 返回最大检测大小
	 */
	public int getMaxSize() {
		return this.detector.getMaximumDetectionSize();
	}

	/**
	 * 设置最大检测大小
	 */
	public void setMaxSize(int size) {
		this.detector.setMaximumDetectionSize(size);
	}

	public DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> getGroupingFilter() {
		return groupingFilter;
	}

	public void setGroupingFilter(DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> grouping) {
		this.groupingFilter = grouping;
	}

	@Override
	public List<DetectedFace> detectFaces(FImage image) {
		if (histogramEqualize)
			image.processInplace(new EqualisationProcessor());

		final List<Rectangle> rects = detector.detect(image);
		final List<ObjectIntPair<Rectangle>> filteredRects = groupingFilter.apply(rects);

		final List<DetectedFace> results = new ArrayList<DetectedFace>();
		for (final ObjectIntPair<Rectangle> r : filteredRects) {
			results.add(new DetectedFace(r.first, image.extractROI(r.first), r.second));
		}

		return results;
	}

	/**
	 * 获得维度因子
	 */
	public double getScaleFactor() {
		return detector.getScaleFactor();
	}

	/**
	 * 设置级联分类器
	 */
	public void setCascade(String cascadeResource) throws Exception {
		InputStream in = null;
		try {
			in = OCVHaarLoader.class.getResourceAsStream(cascadeResource);

			if (in == null) {
				in = new FileInputStream(new File(cascadeResource));
			}
			final StageTreeClassifier cascade = OCVHaarLoader.read(in);

			if (this.detector == null)
				this.detector = new Detector(cascade);
			else
				this.detector = new Detector(cascade, this.detector.getScaleFactor());
		} catch (final Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	/**
	 * 设置维度因子
	 */
	public void setScale(float scaleFactor) {
		this.detector.setScaleFactor(scaleFactor);
	}

	public void save(OutputStream os) throws IOException {
		final ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
	}

	public static HaarCascadeDetector read(InputStream is) throws IOException, ClassNotFoundException {
		final ObjectInputStream ois = new ObjectInputStream(is);
		return (HaarCascadeDetector) ois.readObject();
	}

	@Override
	public int hashCode() {
		int hashCode = HashCodeUtil.SEED;

		hashCode = HashCodeUtil.hash(hashCode, this.detector.getMinimumDetectionSize());
		hashCode = HashCodeUtil.hash(hashCode, this.detector.getScaleFactor());
		hashCode = HashCodeUtil.hash(hashCode, this.detector.getClassifier().getName());
		hashCode = HashCodeUtil.hash(hashCode, this.groupingFilter);
		hashCode = HashCodeUtil.hash(hashCode, this.histogramEqualize);

		return hashCode;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.detector = IOUtils.read(in);
		this.groupingFilter = IOUtils.read(in);

		histogramEqualize = in.readBoolean();
	}

	@Override
	public byte[] binaryHeader() {
		return "HAAR".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(detector, out);
		IOUtils.write(groupingFilter, out);

		out.writeBoolean(histogramEqualize);
	}

	@Override
	public String toString() {
		return "HaarCascadeDetector[cascade=" + detector.getClassifier().getName() + "]";
	}

	public StageTreeClassifier getCascade() {
		return detector.getClassifier();
	}

	public Detector getDetector() {
		return detector;
	}

}
