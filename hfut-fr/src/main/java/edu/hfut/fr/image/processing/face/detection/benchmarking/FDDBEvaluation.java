package edu.hfut.fr.image.processing.face.detection.benchmarking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.data.dataset.ListDataset;

import edu.hfut.fr.image.objectdetection.filtering.OpenCVGrouping;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.HaarCascadeDetector;
import edu.hfut.fr.image.processing.face.detection.benchmarking.Matcher.Match;
import gnu.trove.set.hash.TDoubleHashSet;

/**
 * FDDB实现类
 *
 * @author wanghao
 */
public class FDDBEvaluation {

	public interface EvaluationDetector {
		List<? extends DetectedFace> getDetections(FDDBRecord record);
	}

	public List<Results> performEvaluation(ListDataset<FDDBRecord> dataset, EvaluationDetector detector) {
		List<Results> cumRes = new ArrayList<Results>();
		final Matcher matcher = new Matcher();

		final int numImages = dataset.size();
		for (int i = 0; i < numImages; i++) {
			final FDDBRecord data = dataset.getInstance(i);
			final String imName = data.getImageName();

			final List<? extends DetectedFace> annot = data.getGroundTruth();
			final List<? extends DetectedFace> det = detector.getDetections(data);

			final List<Results> imageResults = new ArrayList<Results>();

			if (det.size() == 0) {
				final Results r = new Results(imName, Double.MAX_VALUE, null, annot, det);
				imageResults.add(r);
			} else {
				final double[] uniqueScores = getUniqueConfidences(det);

				for (final double scoreThreshold : uniqueScores) {
					final ArrayList<DetectedFace> filteredDet = new ArrayList<DetectedFace>();
					for (int di = 0; di < det.size(); di++) {
						final DetectedFace rd = det.get(di);
						if (rd.getConfidence() >= scoreThreshold)
							filteredDet.add(rd);
					}

					final List<Match> mps = matcher.match(annot, filteredDet);

					final Results r = new Results(imName, scoreThreshold, mps, annot, filteredDet);
					imageResults.add(r);
				}
			}

			cumRes = Results.merge(cumRes, imageResults);
		}

		return cumRes;
	}

	private double[] getUniqueConfidences(List<? extends DetectedFace> faces) {
		final TDoubleHashSet set = new TDoubleHashSet();

		for (final DetectedFace f : faces) {
			set.add(f.getConfidence());
		}

		final double[] ret = set.toArray();

		Arrays.sort(ret);

		return ret;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		final File fddbGroundTruth = new File("/Users/jsh2/Downloads/FDDB-folds/FDDB-fold-01-ellipseList.txt");
		final File imageBase = new File("/Users/jsh2/Downloads/originalPics/");
		final FDDBDataset dataset = new FDDBDataset(fddbGroundTruth, imageBase, true);

		final HaarCascadeDetector det = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		det.setGroupingFilter(new OpenCVGrouping(0));
		det.setMinSize(80);
		final EvaluationDetector evDet = new EvaluationDetector() {

			@Override
			public synchronized List<? extends DetectedFace> getDetections(FDDBRecord record) {
				final List<DetectedFace> faces = det.detectFaces(record.getFImage());

				return faces;
			}
		};

		final FDDBEvaluation eval = new FDDBEvaluation();
		final List<Results> result = eval.performEvaluation(dataset, evDet);

		System.out.println(Results.getROCData(result));
	}

}
