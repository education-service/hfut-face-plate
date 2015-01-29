package edu.hfut.fr.image.objectdetection.haar;

/**
 * 关联权重矩形
 *
 * @author wanghao
 */
public class WeightedRectangle {

	/**
	 * 权重
	 */
	public float weight;

	/**
	* 高度
	 */
	public int height;

	/**
	 * 宽度
	 */
	public int width;

	public int y;

	public int x;

	/**
	 * 构造函数
	 *
	 */
	public WeightedRectangle(int x, int y, int width, int height, float weight) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.weight = weight;
	}

	/**
	 *解析字符串函数
	 */
	public static WeightedRectangle parse(String str) {
		final String[] parts = str.trim().split(" ");

		final int x = Integer.parseInt(parts[0]);
		final int y = Integer.parseInt(parts[1]);
		final int width = Integer.parseInt(parts[2]);
		final int height = Integer.parseInt(parts[3]);
		final float weight = Float.parseFloat(parts[4]);

		return new WeightedRectangle(x, y, width, height, weight);
	}

}
