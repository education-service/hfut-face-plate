package edu.hfut.fr.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * CCD 检测脸部类
 *
 * @author wanghgang
 */
public class CCDetectedFace extends DetectedFace {

	ConnectedComponent connectedComponent;

	/**
	 * 默认构造函数
	 */
	public CCDetectedFace() {
		super();
	}

	public CCDetectedFace(Rectangle bounds, FImage patch, ConnectedComponent cc, float confidence) {
		super(bounds, patch, confidence);
		this.connectedComponent = cc;
	}

	/**
	 * 返回脸部相连部分.
	 */
	public PixelSet getConnectedComponent() {
		return connectedComponent;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		connectedComponent.writeBinary(out);
	}

	@Override
	public byte[] binaryHeader() {
		return "CCDF".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		connectedComponent.readBinary(in);
	}

	@Override
	public Shape getShape() {
		return connectedComponent.toPolygon();
	}

}
