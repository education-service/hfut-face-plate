package edu.hfut.fr.image.processing.morphology;

/**
 * 固化
 *
 * @author Jimbo
 */
public class Skeleton extends SequentialThin {

	protected Skeleton(StructuringElement... se) {
		this.hitAndMiss = new HitAndMiss(se);
	}

	/**
	 * 固化的构造函数
	 */
	public Skeleton() {
		this.hitAndMiss = new HitAndMiss(GolayAlphabet.L);
	}

}
