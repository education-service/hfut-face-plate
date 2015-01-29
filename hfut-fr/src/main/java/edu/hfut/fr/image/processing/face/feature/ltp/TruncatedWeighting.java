package edu.hfut.fr.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * 削减过的距离超过阈值的截断权重方案。
 *
 *@author jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Tan, Xiaoyang", "Triggs, Bill" }, title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions", year = "2010", journal = "Trans. Img. Proc.", pages = {
		"1635", "1650" }, url = "http://dx.doi.org/10.1109/TIP.2010.2042645", month = "June", number = "6", publisher = "IEEE Press", volume = "19")
public class TruncatedWeighting implements LTPWeighting {

	float threshold = 6;

	/**
	 * 默认距离构造
	 */
	public TruncatedWeighting() {
	}

	public TruncatedWeighting(float threshold) {
		this.threshold = threshold;
	}

	@Override
	public float weightDistance(float distance) {
		return Math.min(distance, threshold);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		threshold = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(threshold);
	}

	@Override
	public String toString() {
		return "TruncatedWeighting[threshold=" + threshold + "]";
	}

}
