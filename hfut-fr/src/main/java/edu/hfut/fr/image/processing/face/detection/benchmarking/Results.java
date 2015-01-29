package edu.hfut.fr.image.processing.face.detection.benchmarking;

import java.util.ArrayList;
import java.util.List;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.benchmarking.Matcher.Match;

/**
 * 结果表示类
 *
 * @author wanghao
 */
public class Results {
	int N;

	double scoreThreshold;

	double TPCont;

	double TPDisc;

	double FP;

	String imName;

	Results() {
	}

	Results(Results r) {
		if (r != null) {
			N = r.N;
			TPCont = r.TPCont;
			TPDisc = r.TPDisc;
			FP = r.FP;
			scoreThreshold = r.scoreThreshold;
		}
	}

	Results(Results r, int N2) {
		if (r != null) {
			imName = r.imName;
			N = r.N + N2;
			TPCont = r.TPCont;
			TPDisc = r.TPDisc;
			FP = r.FP;
			scoreThreshold = r.scoreThreshold;
		}
	}

	Results(Results r1, Results r2) {
		N = 0;
		TPDisc = 0;
		TPCont = 0;
		FP = 0;
		scoreThreshold = Double.MAX_VALUE;

		if (r1 != null) {
			imName = r1.imName;
			N += r1.N;
			TPCont += r1.TPCont;
			TPDisc += r1.TPDisc;
			FP += r1.FP;
			if (r1.scoreThreshold < scoreThreshold)
				scoreThreshold = r1.scoreThreshold;
		}

		if (r2 != null) {
			imName = r2.imName;
			N += r2.N;
			TPCont += r2.TPCont;
			TPDisc += r2.TPDisc;
			FP += r2.FP;
			if (r2.scoreThreshold < scoreThreshold)
				scoreThreshold = r2.scoreThreshold;
		}
	}

	Results(String s, double scoreThresh, List<Match> mp, List<? extends DetectedFace> annot,
			List<? extends DetectedFace> det) {
		imName = s;
		scoreThreshold = scoreThresh;

		N = annot.size();

		FP = det.size();

		TPCont = 0;
		TPDisc = 0;
		if (mp != null)
			for (int i = 0; i < mp.size(); i++) {
				final double score = mp.get(i).score;
				TPCont += score;
				if (score > 0.5) {
					TPDisc++;
					FP--;
				}
			}

	}

	static List<Results> merge(List<Results> rv1, List<Results> rv2) {

		final List<Results> mergeV = new ArrayList<Results>();
		int n1 = 0;
		if (rv1 != null)
			n1 = rv1.size();
		int n2 = 0;
		if (rv2 != null)
			n2 = rv2.size();

		int nAnnot1 = 0, nAnnot2 = 0;

		if (n1 > 0) {
			nAnnot1 = rv1.get(0).getN();
			if (n2 > 0)
				nAnnot2 = rv2.get(0).getN();

			int i1 = 0, i2 = 0;
			double score1, score2;

			Results r1 = null;
			Results r2 = null;

			while (i1 < n1) {
				r1 = rv1.get(i1);
				score1 = rv1.get(i1).scoreThreshold;
				if (i2 < n2) {
					r2 = rv2.get(i2);
					score2 = rv2.get(i2).scoreThreshold;
					final Results newR = new Results(r1, r2);
					mergeV.add(newR);
					if (score1 < score2) {
						i1++;
					} else if (score1 == score2) {
						i1++;
						i2++;
					} else {
						i2++;
					}
				} else {
					while (i1 < n1) {
						// add from rv1
						r1 = rv1.get(i1);

						final Results newR = new Results(r1, nAnnot2);
						mergeV.add(newR);
						i1++;
					}
				}
			}

			while (i2 < n2) {
				r2 = rv2.get(i2);
				final Results newR = new Results(r2, nAnnot1);
				mergeV.add(newR);
				i2++;
			}
		} else {
			if (n2 > 0) {
				for (int i = 0; i < n2; i++)
					mergeV.add(new Results(rv2.get(i)));
			}
		}
		return mergeV;
	}

	@Override
	public String toString() {
		return imName + " Threshold = " + scoreThreshold + " N = " + N + " TP cont = " + TPCont + " TP disc = "
				+ TPDisc + " FP = " + FP;
	}

	public static String getROCData(List<Results> rv) {
		String osc = "";
		String osd = "";

		for (int i = 0; i < rv.size(); i++) {
			final Results r = rv.get(i);
			if (r.N > 0) {
				osc += (r.TPCont / r.N) + " " + r.FP + "\n";
				osd += (r.TPDisc / r.N) + " " + r.FP + " " + r.scoreThreshold + "\n";
			} else {
				osc += "0 0" + "\n";
				osd += "0 0 " + r.scoreThreshold + "\n";
			}
		}
		return "DISCRETE:\n" + osd + "\nCONTINOUS:\n" + osc;
	}

	public int getN() {
		return N;
	}

}
