package edu.hfut.fr.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

import edu.hfut.fr.image.processing.face.feature.ltp.LtpDtFeature;

/**
 * 基于欧几里得距离转换方法的比较器
 *
 *@author jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Tan, Xiaoyang", "Triggs, Bill" }, title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions", year = "2010", journal = "Trans. Img. Proc.", pages = {
		"1635", "1650" }, url = "http://dx.doi.org/10.1109/TIP.2010.2042645", month = "June", number = "6", publisher = "IEEE Press", volume = "19")
public class LtpDtFeatureComparator implements FacialFeatureComparator<LtpDtFeature> {

	@Override
	public double compare(LtpDtFeature query, LtpDtFeature target) {
		List<List<Pixel>> slicePixels = query.ltpPixels;
		float distance = 0;

		FImage[] distanceMaps = target.getDistanceMaps();

		for (int i = 0; i < distanceMaps.length; i++) {
			List<Pixel> pixels = slicePixels.get(i);

			if (distanceMaps[i] == null || pixels == null)
				continue;

			for (Pixel p : pixels) {
				distance += distanceMaps[i].pixels[p.y][p.x];
			}
		}

		return distance;
	}

	@Override
	public boolean isDistance() {
		return true;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
	}

	@Override
	public byte[] binaryHeader() {
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
	}

	@Override
	public String toString() {
		return "LtpDtFeatureComparator";
	}

}
