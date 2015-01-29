package edu.hfut.fr.image.processing.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.io.ReadWriteable;

/**
 * affine仿真参数定义
 *
 * @author Jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Morel, Jean-Michel", "Yu, Guoshen" }, title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}", year = "2009", journal = "SIAM J. Img. Sci.", publisher = "Society for Industrial and Applied Mathematics")
public class AffineParams implements ReadWriteable {

	/**
	 * 旋转角度
	 */
	public float theta;

	/**
	 * tilt的数量
	 */
	public float tilt;

	/**
	 *
	 * 构造函数
	 * @param theta
	 * 		旋转角度
	 * @param tilt
	 * 		tilt
	 */
	public AffineParams(float theta, float tilt) {
		this.theta = theta;
		this.tilt = tilt;
	}

	/**
	 * 无参构造函数
	 */
	public AffineParams() {
	}

	@Override
	public boolean equals(Object po) {
		if (po instanceof AffineParams) {
			final AffineParams p = (AffineParams) po;
			return (Math.abs(theta - p.theta) < 0.00001 && Math.abs(tilt - p.tilt) < 0.00001);
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int hash = new Float(theta).hashCode() ^ new Float(tilt).hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return String.format("theta:%f tilt:%f", theta, tilt);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.theta = in.readFloat();
		this.tilt = in.readFloat();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.theta = in.nextFloat();
		this.tilt = in.nextFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(this.theta);
		out.writeFloat(this.tilt);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.theta);
		out.println(this.tilt);
	}

}
