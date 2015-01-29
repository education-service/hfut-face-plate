package edu.hfut.fr.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * 高斯方法
 *
 * @author jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Tan, Xiaoyang", "Triggs, Bill" }, title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions", year = "2010", journal = "Trans. Img. Proc.", pages = {
		"1635", "1650" }, url = "http://dx.doi.org/10.1109/TIP.2010.2042645", month = "June", number = "6", publisher = "IEEE Press", volume = "19")
public class GaussianWeighting implements LTPWeighting {

	private float sigma = 3;

	public GaussianWeighting() {
	}

	public GaussianWeighting(float sigma) {
		this.sigma = sigma;
	}

	@Override
	public float weightDistance(float distance) {
		return (float) Math.exp(-(distance * distance) / (sigma * sigma * 2));
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		sigma = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(sigma);
	}

	@Override
	public String toString() {
		return "GaussianWeighting[sigma=" + sigma + "]";
	}

}
