package edu.hfut.lpr.images;


/**
 * 完整车牌图像中车牌区域的位置类
 *
 * @author wanggang
 *
 */
public class PositionInPlate {

	// 最左边的x位置
	public int x1;
	// 最右边的x位置
	public int x2;

	PositionInPlate(int x1, int x2) {
		this.x1 = x1;
		this.x2 = x2;
	}

}