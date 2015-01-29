package edu.hfut.fr.image.processing.face.similarity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.InvertData;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.feature.FacialFeature;
import edu.hfut.fr.image.processing.face.feature.FacialFeatureExtractor;
import edu.hfut.fr.image.processing.face.feature.comparison.FacialFeatureComparator;

/**
 * 计算图像相似度的工具
 *
 * @author jimbo
 */
public class FaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature, I extends Image<?, I>> {

	private FaceDetector<D, I> detector;
	private FacialFeatureExtractor<F, D> extractor;
	private FacialFeatureComparator<F> comparator;
	private Map<String, Rectangle> boundingBoxes;
	private Map<String, F> featureCache;
	private Map<String, List<D>> detectedFaceCache;
	private LinkedHashMap<String, Map<String, Double>> similarityMatrix;
	private List<D> queryfaces;
	private List<D> testfaces;
	private String queryId;
	private String testId;
	private boolean cache;

	/**
	 * 构造函数
	 */
	public FaceSimilarityEngine(FaceDetector<D, I> detector, FacialFeatureExtractor<F, D> extractor,
			FacialFeatureComparator<F> comparator) {
		this.detector = detector;
		this.extractor = extractor;
		this.comparator = comparator;
		this.similarityMatrix = new LinkedHashMap<String, Map<String, Double>>();
		this.boundingBoxes = new HashMap<String, Rectangle>();
		featureCache = new HashMap<String, F>();
		detectedFaceCache = new HashMap<String, List<D>>();
	}

	/**
	 * 检测器
	 */
	public FaceDetector<D, I> detector() {
		return detector;
	}

	/**
	 * 特征提取
	 */
	public FacialFeatureExtractor<F, D> extractor() {
		return extractor;
	}

	/**
	 * 比较器
	 */
	public FacialFeatureComparator<F> comparator() {
		return comparator;
	}

	/**
	 * 构建一个相似度比较器
	 */
	public static <D extends DetectedFace, F extends FacialFeature, I extends Image<?, I>> FaceSimilarityEngine<D, F, I> create(
			FaceDetector<D, I> detector, FacialFeatureExtractor<F, D> extractor, FacialFeatureComparator<F> comparator) {
		return new FaceSimilarityEngine<D, F, I>(detector, extractor, comparator);
	}

	public void setQuery(I queryImage, String queryId) {
		this.queryfaces = getDetectedFaces(queryId, queryImage);
		this.queryId = queryId;
		updateBoundingBox(this.queryfaces, queryId);
	}

	private List<D> getDetectedFaces(String faceId, I faceImage) {
		List<D> toRet = null;
		if (!this.cache) {
			toRet = this.detector.detectFaces(faceImage);
		} else {
			toRet = this.detectedFaceCache.get(faceId);
			if (toRet == null) {
				toRet = this.detector.detectFaces(faceImage);
				;
				this.detectedFaceCache.put(faceId, toRet);
			}
		}
		return toRet;
	}

	private void updateBoundingBox(List<D> faces, String imageId) {
		if (boundingBoxes != null)
			for (int ff = 0; ff < faces.size(); ff++)
				if (boundingBoxes.get(imageId + ":" + ff) == null)
					boundingBoxes.put(imageId + ":" + ff, faces.get(ff).getBounds());
	}

	public void setTest(I testImage, String testId) {
		this.testId = testId;
		this.testfaces = getDetectedFaces(testId, testImage);
		updateBoundingBox(this.testfaces, testId);
	}

	public void setQueryTest() {
		this.testfaces = this.queryfaces;
		this.testId = this.queryId;
	}

	/**
	 * 计算指定目标与样本的相似度
	 */
	public void performTest() {
		for (int ii = 0; ii < queryfaces.size(); ii++) {
			String face1id = queryId + ":" + ii;
			D f1f = queryfaces.get(ii);

			F f1fv = getFeature(face1id, f1f);

			for (int jj = 0; jj < testfaces.size(); jj++) {
				double d = 0;
				String face2id = null;

				if (queryfaces == testfaces && ii == jj) {
					d = 0;
					face2id = face1id;
				} else {
					D f2f = testfaces.get(jj);
					face2id = testId + ":" + jj;

					F f2fv = getFeature(face2id, f2f);

					d = comparator.compare(f1fv, f2fv);
				}

				Map<String, Double> mm = this.similarityMatrix.get(face1id);
				if (mm == null)
					this.similarityMatrix.put(face1id, mm = new HashMap<String, Double>());
				mm.put(face2id, d);
			}
		}
	}

	private F getFeature(String id, D face) {
		F toRet = null;

		if (!cache) {
			toRet = extractor.extractFeature(face);
		} else {
			String combinedID = String.format("%s:%b", id);
			toRet = this.featureCache.get(combinedID);

			if (toRet == null) {
				toRet = extractor.extractFeature(face);
				this.featureCache.put(combinedID, toRet);
			}
		}
		return toRet;
	}

	/**
	 * 返回相似度计算结果
	 */
	public Map<String, Map<String, Double>> getSimilarityDictionary() {
		return this.similarityMatrix;
	}

	/**
	 * 获取相似度矩阵
	 */
	public SimilarityMatrix getSimilarityMatrix(boolean invertIfRequired) {
		Set<String> keys = this.similarityMatrix.keySet();
		String[] indexArr = keys.toArray(new String[keys.size()]);
		SimilarityMatrix simMatrix = new SimilarityMatrix(indexArr);
		for (int i = 0; i < indexArr.length; i++) {
			String x = indexArr[i];
			for (int j = 0; j < indexArr.length; j++) {
				String y = indexArr[j];
				simMatrix.set(i, j, this.similarityMatrix.get(x).get(y));
			}
		}

		if (this.comparator.isDistance() && invertIfRequired) {
			simMatrix.processInplace(new InvertData());
		}
		return simMatrix;
	}

	public Map<String, Rectangle> getBoundingBoxes() {
		return this.boundingBoxes;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

}
