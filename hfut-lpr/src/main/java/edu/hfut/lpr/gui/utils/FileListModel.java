package edu.hfut.lpr.gui.utils;

import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * 文件列表模型
 *
 * @author wanggang
 *
 */
public class FileListModel implements ListModel<Object> {

	/**
	 * 实体类
	 */
	public class FileListModelEntry {

		public String fileName;
		public String fullPath;
		public String recognizedPlate;

		public FileListModelEntry(String fileName, String fullPath) {
			this.fileName = fileName;
			this.fullPath = fullPath;
			this.recognizedPlate = "?";
		}

		@Override
		public String toString() {
			return this.fileName;
		}

	}

	public Vector<FileListModelEntry> fileList;

	/**
	 * 创建文件列表模型实例
	 */
	public FileListModel() {
		this.fileList = new Vector<FileListModelEntry>();
	}

	/**
	 * 添加文件列表实体
	 * @param fileName 文件名
	 * @param fullPath 完整路径
	 */
	public void addFileListModelEntry(String fileName, String fullPath) {
		this.fileList.add(new FileListModelEntry(fileName, fullPath));
	}

	/**
	 * 文件列表大小
	 */
	@Override
	public int getSize() {
		return this.fileList.size();
	}

	/**
	 * 获取某个文件对象
	 */
	@Override
	public Object getElementAt(int index) {
		return this.fileList.elementAt(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		//
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		//
	}

}