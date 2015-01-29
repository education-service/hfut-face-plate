package edu.hfut.fr.image.processing.convolution.filterbank;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

import edu.hfut.fr.image.processing.convolution.FConvolution;

/**
 * LawsTexture实现类
 *
 *@author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Laws, K. I." }, title = "{Rapid Texture Identification}", year = "1980", booktitle = "Proc. SPIE Conf. Image Processing for Missile Guidance", pages = {
		"376", "", "380" }, customData = { "citeulike-article-id", "2335645", "keywords", "bibtex-import", "posted-at",
		"2008-02-05 15:32:50", "priority", "2" })
public class LawsTextureBase extends FilterBank {

	private static final float[] L5 = { 1, 4, 6, 4, 1 };
	private static final float[] E5 = { -1, -2, 0, 2, 1 };
	private static final float[] S5 = { -1, 0, 2, 0, -1 };
	private static final float[] R5 = { 1, -4, 6, -4, 1 };

	protected final static int L5E5 = 0;
	protected final static int E5L5 = 1;
	protected final static int L5R5 = 2;
	protected final static int R5L5 = 3;
	protected final static int E5S5 = 4;
	protected final static int S5E5 = 5;
	protected final static int S5S5 = 6;
	protected final static int R5R5 = 7;
	protected final static int L5S5 = 8;
	protected final static int S5L5 = 9;
	protected final static int E5E5 = 10;
	protected final static int E5R5 = 11;
	protected final static int R5E5 = 12;
	protected final static int S5R5 = 13;
	protected final static int R5S5 = 14;

	/**
	 * Default constructor
	 */
	public LawsTextureBase() {
		super(makeFilters());
	}

	private static FConvolution[] makeFilters() {
		final FConvolution[] filters = new FConvolution[15];

		filters[L5E5] = makeFilter(L5, E5);
		filters[E5L5] = makeFilter(E5, L5);
		filters[L5R5] = makeFilter(L5, R5);
		filters[R5L5] = makeFilter(R5, L5);
		filters[E5S5] = makeFilter(E5, S5);
		filters[S5E5] = makeFilter(S5, E5);
		filters[S5S5] = makeFilter(S5, S5);
		filters[R5R5] = makeFilter(R5, R5);
		filters[L5S5] = makeFilter(L5, S5);
		filters[S5L5] = makeFilter(S5, L5);
		filters[E5E5] = makeFilter(E5, E5);
		filters[E5R5] = makeFilter(E5, R5);
		filters[R5E5] = makeFilter(R5, E5);
		filters[S5R5] = makeFilter(S5, R5);
		filters[R5S5] = makeFilter(R5, S5);

		return filters;
	}

	private static FConvolution makeFilter(float[] l, float[] r) {
		final float[][] f = new float[l.length][r.length];

		for (int i = 0; i < l.length; i++)
			for (int j = 0; j < r.length; j++)
				f[i][j] = l[i] * r[j];

		return new FConvolution(f);
	}

}
