package edu.hfut.lpr.analysis;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Stack;
import java.util.Vector;

/**
 * 像素图类
 *
 * @author wanggang
 *
 */
public class PixelMap {

	/**
	 * 像素点位置类
	 */
	private class Point {

		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
			// 布尔型，判断是否被删除
			// this.deleted = false;
		}

		boolean equals(Point p2) {
			return (p2.x == this.x) && (p2.y == this.y);
		}

		/*boolean equals(int x, int y) {
			if ((x == this.x) && (y == this.y)) {
				return true;
			}
			return false;
		}*/

		/*public boolean value() {
			return matrix[x][y];
		}*/
	}

	/**
	 * 像素点集合类
	 */
	private class PointSet extends Stack<Point> {

		private static final long serialVersionUID = 4997339122817752875L;

		/**
		 * 移除像素点
		 */
		public void removePoint(Point p) {
			Point toRemove = null;
			for (Point px : this) {
				if (px.equals(p)) {
					toRemove = px;
				}
			}
			this.remove(toRemove);
		}

	}

	/**
	 * 像素块集合类
	 */
	public class PieceSet extends Vector<Piece> {

		private static final long serialVersionUID = 5234661385050278357L;

	}

	// 最好的像素块
	private Piece bestPiece = null;

	/**
	 * 像素块类
	 */
	public class Piece extends PointSet {

		private static final long serialVersionUID = -3271018169079927081L;

		// 像素块最左边点x位置
		public int mostLeftPoint;
		// 像素块最右边点x位置
		public int mostRightPoint;
		// 像素块最上边点y位置
		public int mostTopPoint;
		// 像素块最下边点y位置
		public int mostBottomPoint;
		// 像素块的宽度
		public int width;
		// 像素块的长度
		public int height;
		// 像素块中心的x坐标值
		public int centerX;
		// 像素块中心的y坐标值
		public int centerY;
		// 像素块的幅度值，即黑色点的比例值
		public float magnitude;
		// 像素块中黑色点的数量
		public int numberOfBlackPoints;
		// 像素块中所有点的数量
		public int numberOfAllPoints;

		/**
		 * 渲染该像素块，返回二值化图像
		 */
		public BufferedImage render() {
			if (this.numberOfAllPoints == 0) {
				return null;
			}
			BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
			for (int x = this.mostLeftPoint; x <= this.mostRightPoint; x++) {
				for (int y = this.mostTopPoint; y <= this.mostBottomPoint; y++) {
					if (PixelMap.this.matrix[x][y]) {
						// 值为true,为黑色
						image.setRGB(x - this.mostLeftPoint, y - this.mostTopPoint, Color.BLACK.getRGB());
					} else {
						// 值为flase,为白色
						image.setRGB(x - this.mostLeftPoint, y - this.mostTopPoint, Color.WHITE.getRGB());
					}
				}
			}
			return image;
		}

		/**
		 * 创建统计数据
		 */
		public void createStatistics() {
			this.mostLeftPoint = this.mostLeftPoint();
			this.mostRightPoint = this.mostRightPoint();
			this.mostTopPoint = this.mostTopPoint();
			this.mostBottomPoint = this.mostBottomPoint();
			this.width = (this.mostRightPoint - this.mostLeftPoint) + 1;
			this.height = (this.mostBottomPoint - this.mostTopPoint) + 1;
			this.centerX = (this.mostLeftPoint + this.mostRightPoint) / 2;
			this.centerY = (this.mostTopPoint + this.mostBottomPoint) / 2;
			this.numberOfBlackPoints = this.numberOfBlackPoints();
			this.numberOfAllPoints = this.numberOfAllPoints();
			this.magnitude = this.magnitude();
		}

		/**
		 * 计算白色点的数量
		 */
		public int cost() {
			return this.numberOfAllPoints - this.numberOfBlackPoints();
		}

		/**
		 * 使像素块变白
		 */
		public void bleachPiece() {
			for (Point p : this) {
				PixelMap.this.matrix[p.x][p.y] = false;
			}
		}

		/**
		 * 返回黑色点的比例值
		 */
		private float magnitude() {
			return ((float) this.numberOfBlackPoints / this.numberOfAllPoints);
		}

		/**
		 * 黑色点的数量
		 */
		private int numberOfBlackPoints() {
			return this.size();
		}

		/**
		 * 所有点总数量
		 */
		private int numberOfAllPoints() {
			return this.width * this.height;
		}

		/**
		 * 最左边点x位置
		 */
		private int mostLeftPoint() {
			int position = Integer.MAX_VALUE;
			for (Point p : this) {
				position = Math.min(position, p.x);
			}
			return position;
		}

		/**
		 * 最右边点x位置
		 */
		private int mostRightPoint() {
			int position = 0;
			for (Point p : this) {
				position = Math.max(position, p.x);
			}
			return position;
		}

		/**
		 * 最上边点y位置，注意：图像以左上角为坐标原点
		 */
		private int mostTopPoint() {
			int position = Integer.MAX_VALUE;
			for (Point p : this) {
				position = Math.min(position, p.y);
			}
			return position;
		}

		/**
		 * 最下边点y位置
		 */
		private int mostBottomPoint() {
			int position = 0;
			for (Point p : this) {
				position = Math.max(position, p.y);
			}
			return position;
		}
	}

	// 像素图行列矩阵
	boolean[][] matrix;
	// 像素图宽度
	private int width;
	// 像素图长度
	private int height;

	public PixelMap(Photo bi) {
		this.matrixInit(bi);
	}

	/**
	 * 像素图矩阵初始化
	 */
	void matrixInit(Photo bi) {
		this.width = bi.getWidth();
		this.height = bi.getHeight();

		this.matrix = new boolean[this.width][this.height];

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				// 布尔型
				this.matrix[x][y] = bi.getBrightness(x, y) < 0.5;
			}
		}
	}

	/**
	 * 二值化渲染
	 */
	public BufferedImage render() {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				if (this.matrix[x][y]) {
					image.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					image.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return image;
	}

	/**
	 * 返回最好的像素块
	 */
	public Piece getBestPiece() {
		this.reduceOtherPieces();
		if (this.bestPiece == null) {
			return new Piece();
		}
		return this.bestPiece;
	}

	/**
	 * 获取像素点的值
	 */
	private boolean getPointValue(int x, int y) {
		if ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height)) {
			return false;
		}
		return this.matrix[x][y];
	}

	/**
	 * 判断某个点是否为边界点
	 */
	private boolean isBoundaryPoint(int x, int y) {
		// 白色点，不是边界点
		if (!this.getPointValue(x, y)) {
			return false;
		}

		// 当黑色点周围的8个点都是白色时，为边界点
		return !this.getPointValue(x - 1, y - 1) || !this.getPointValue(x - 1, y + 1)
				|| !this.getPointValue(x + 1, y - 1) || !this.getPointValue(x + 1, y + 1)
				|| !this.getPointValue(x, y + 1) || !this.getPointValue(x, y - 1) || !this.getPointValue(x + 1, y)
				|| !this.getPointValue(x - 1, y);
	}

	/**
	 * 计算某个点周围8个点中黑色点数量
	 */
	private int n(int x, int y) {
		int n = 0;
		if (this.getPointValue(x - 1, y - 1)) {
			n++;
		}
		if (this.getPointValue(x - 1, y + 1)) {
			n++;
		}
		if (this.getPointValue(x + 1, y - 1)) {
			n++;
		}
		if (this.getPointValue(x + 1, y + 1)) {
			n++;
		}
		if (this.getPointValue(x, y + 1)) {
			n++;
		}
		if (this.getPointValue(x, y - 1)) {
			n++;
		}
		if (this.getPointValue(x + 1, y)) {
			n++;
		}
		if (this.getPointValue(x - 1, y)) {
			n++;
		}
		return n;
	}

	/**
	 * 计算某个点附近p2,p3,p4,...,p9,p2顺序点，两两白黑相间的数量
	 */
	private int t(int x, int y) {
		int n = 0;
		// 计算白黑相间的数量，即：2-3, 3-4, 4-5, 5-6, 6-7, 7-8, 8-9, 9-2组中白黑相间的数量
		for (int i = 2; i <= 8; i++) {
			if (!this.p(i, x, y) && this.p(i + 1, x, y)) {
				n++;
			}
		}
		if (!this.p(9, x, y) && this.p(2, x, y)) {
			n++;
		}
		return n;
	}

	/**
	 * 指定第i个点周围8个点的位置
	 *
	 * 如果第i个点记作p1,那么p1~p9表示为：
	 *         | p9 | p2 | p3
	 *         | p8 | p1 | p4
	 *         | p7 | p6 | p5
	 */
	private boolean p(int i, int x, int y) {
		if (i == 1) {
			return this.getPointValue(x, y);
		}
		if (i == 2) {
			return this.getPointValue(x, y - 1);
		}
		if (i == 3) {
			return this.getPointValue(x + 1, y - 1);
		}
		if (i == 4) {
			return this.getPointValue(x + 1, y);
		}
		if (i == 5) {
			return this.getPointValue(x + 1, y + 1);
		}
		if (i == 6) {
			return this.getPointValue(x, y + 1);
		}
		if (i == 7) {
			return this.getPointValue(x - 1, y + 1);
		}
		if (i == 8) {
			return this.getPointValue(x - 1, y);
		}
		if (i == 9) {
			return this.getPointValue(x - 1, y - 1);
		}
		return false;
	}

	/**
	 * 查找边界：步骤1
	 */
	private boolean step1passed(int x, int y) {
		int n = this.n(x, y);
		return (((2 <= n) && (n <= 6)) && (this.t(x, y) == 1)
				&& (!this.p(2, x, y) || !this.p(4, x, y) || !this.p(6, x, y)) && (!this.p(4, x, y) || !this.p(6, x, y) || !this
				.p(8, x, y)));
	}

	/**
	 * 查找边界：步骤2
	 */
	private boolean step2passed(int x, int y) {
		int n = this.n(x, y);
		return (((2 <= n) && (n <= 6)) && (this.t(x, y) == 1)
				&& (!this.p(2, x, y) || !this.p(4, x, y) || !this.p(8, x, y)) && (!this.p(2, x, y) || !this.p(6, x, y) || !this
				.p(8, x, y)));
	}

	/**
	 * 找出所有边界点集合
	 */
	private void findBoundaryPoints(PointSet set) {
		if (!set.isEmpty()) {
			set.clear();
		}
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				if (this.isBoundaryPoint(x, y)) {
					set.add(new Point(x, y));
				}
			}
		}
	}

	/**
	 * 像素图骨骼化或者线条化
	 */
	public PixelMap skeletonize() {
		// 稀释或打薄处理
		// 需要删除的点集合
		PointSet flaggedPoints = new PointSet();
		// 边界点集合
		PointSet boundaryPoints = new PointSet();
		boolean cont;

		do {
			cont = false;
			this.findBoundaryPoints(boundaryPoints);
			// 使用步骤1标志出需要删除的点
			for (Point p : boundaryPoints) {
				if (this.step1passed(p.x, p.y)) {
					flaggedPoints.add(p);
				}
			}
			// 删除已经被标志的点集合
			if (!flaggedPoints.isEmpty()) {
				cont = true;
			}
			for (Point p : flaggedPoints) {
				this.matrix[p.x][p.y] = false;
				boundaryPoints.remove(p);
			}
			flaggedPoints.clear();
			// 使用步骤2标志需要保留的点，也就是边结点
			for (Point p : boundaryPoints) {
				if (this.step2passed(p.x, p.y)) {
					flaggedPoints.add(p);
				}
			}
			// 删除被标志的点集合
			if (!flaggedPoints.isEmpty()) {
				cont = true;
			}
			for (Point p : flaggedPoints) {
				this.matrix[p.x][p.y] = false;
			}
			boundaryPoints.clear();
			flaggedPoints.clear();
		} while (cont);

		return (this);
	}

	/**
	 * 去除噪声
	 */
	public PixelMap reduceNoise() {
		PointSet pointsToReduce = new PointSet();
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				// 记录周围黑色点数量小与4的点，记为噪声点
				if (this.n(x, y) < 4) {
					pointsToReduce.add(new Point(x, y));
				}
			}
		}
		// 将噪声点设置白色
		for (Point p : pointsToReduce) {
			this.matrix[p.x][p.y] = false;
		}
		return (this);
	}

	/****************** 去除其他的像素块 ******************/

	/*private boolean isInPieces(PieceSet pieces, int x, int y) {
		for (Piece piece : pieces) {
			for (Point point : piece) {
				if (point.equals(x, y)) {
					return true;
				}
			}
		}

		return false;
	}*/

	/**
	 * 判断某个点是否应该被添加到像素块中
	 */
	private boolean seedShouldBeAdded(Piece piece, Point p) {
		if ((p.x < 0) || (p.y < 0) || (p.x >= this.width) || (p.y >= this.height)) {
			return false;
		}
		// 白色点不被添加
		if (!this.matrix[p.x][p.y]) {
			return false;
		}
		// 如果p在像素块中，则不被添加
		for (Point piecePoint : piece) {
			if (piecePoint.equals(p)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 创建像素块
	 */
	private Piece createPiece(PointSet unsorted) {

		Piece piece = new Piece();

		PointSet stack = new PointSet();
		stack.push(unsorted.lastElement());

		while (!stack.isEmpty()) {
			Point p = stack.pop();
			if (this.seedShouldBeAdded(piece, p)) {
				piece.add(p);
				unsorted.removePoint(p);
				stack.push(new Point(p.x + 1, p.y));
				stack.push(new Point(p.x - 1, p.y));
				stack.push(new Point(p.x, p.y + 1));
				stack.push(new Point(p.x, p.y - 1));
			}
		}
		piece.createStatistics();

		return piece;
	}

	/**
	 * 找出所有像素块
	 */
	public PieceSet findPieces() {
		//		boolean continueFlag;
		PieceSet pieces = new PieceSet();

		PointSet unsorted = new PointSet();
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				if (this.matrix[x][y]) {
					unsorted.add(new Point(x, y));
				}
			}
		}

		while (!unsorted.isEmpty()) {
			pieces.add(this.createPiece(unsorted));
		}

		/*do {
			continueFlag = false;
			boolean loopBreak = false;
			for (int x = 0; x < this.width; x++) {
				for (int y = 0; y < this.height; y++) {
					if (this.matrix[x][y] && !isInPieces(pieces, x, y)) {
						continueFlag = true;
						pieces.add(createPiece(x, y));
					}
				}
			}
		} while (continueFlag);*/

		return pieces;
	}

	/**
	 * 去除其他的像素块
	 */
	public PixelMap reduceOtherPieces() {
		if (this.bestPiece != null) {
			return this;
		}

		PieceSet pieces = this.findPieces();
		int maxCost = 0;
		int maxIndex = 0;
		for (int i = 0; i < pieces.size(); i++) {
			if (pieces.elementAt(i).cost() > maxCost) {
				maxCost = pieces.elementAt(i).cost();
				maxIndex = i;
			}
		}

		for (int i = 0; i < pieces.size(); i++) {
			if (i != maxIndex) {
				pieces.elementAt(i).bleachPiece();
			}
		}
		if (pieces.size() != 0) {
			this.bestPiece = pieces.elementAt(maxIndex);
		}
		return this;
	}

}