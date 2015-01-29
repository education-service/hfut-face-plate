package edu.hfut.fr.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * 面部检测模板类
 *
 *@author wanggang
 */
public class DetectedFace implements ReadWriteableBinary {

	protected Rectangle bounds;

	/**
	 * 得到图像中人脸的子图像
	 */
	protected FImage facePatch;

	protected float confidence = 1;

	/**
	 * 默认构造函数
	 */
	public DetectedFace() {
		this.bounds = new Rectangle();
	}

	public DetectedFace(final Rectangle bounds, final FImage patch, final float confidence) {
		this.bounds = bounds;
		this.facePatch = patch;
		this.confidence = confidence;
	}

	/**
	 * 返回子图片
	 */
	public FImage getFacePatch() {
		return this.facePatch;
	}

	/**
	 * 重置图像照片
	 */
	public void setFacePatch(final FImage img) {
		this.facePatch = img;
	}

	/**
	 * 得到图像识别边界
	 */
	public Rectangle getBounds() {
		return this.bounds;
	}

	/**
	 * 设置图像边界
	 */
	public void setBounds(final Rectangle rect) {
		this.bounds = rect;
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		this.bounds.writeBinary(out);
		ImageUtilities.write(this.facePatch, "png", out);
	}

	@Override
	public byte[] binaryHeader() {
		return "DF".getBytes();
	}

	@Override
	public void readBinary(final DataInput in) throws IOException {
		this.bounds.readBinary(in);
		this.facePatch = ImageUtilities.readF(in);
	}

	public float getConfidence() {
		return this.confidence;
	}

	/**
	 * 获得检测的形状
	 */
	public Shape getShape() {
		return this.bounds;
	}

	public void setConfidence(final int confidence) {
		this.confidence = confidence;
	}

}
