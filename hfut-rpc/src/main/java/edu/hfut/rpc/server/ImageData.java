package edu.hfut.rpc.server;

import java.io.Serializable;

public class ImageData implements Serializable {

	private static final long serialVersionUID = -669013137825430844L;

	private String fileName;
	private byte[] data;

	public ImageData() {
		super();
	}

	public ImageData(String fileName, byte[] data) {
		super();
		this.fileName = fileName;
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
