package edu.hfut.fr.image.processing.convolution.filterbank;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;

import edu.hfut.fr.image.processing.convolution.SumBoxFilter;

/**
 * LawsTexture 继承类
 *
 *@author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Laws, K. I." }, title = "{Rapid Texture Identification}", year = "1980", booktitle = "Proc. SPIE Conf. Image Processing for Missile Guidance", pages = {
		"376", "", "380" }, customData = { "citeulike-article-id", "2335645", "keywords", "bibtex-import", "posted-at",
		"2008-02-05 15:32:50", "priority", "2" })
public class LawsTexture extends LawsTextureBase {

	private int macroWidth = 15;
	private int macroHeight = 15;

	public LawsTexture() {
	}

	public LawsTexture(int macro) {
		this.macroWidth = macro;
		this.macroHeight = macro;
	}

	public LawsTexture(int macroWidth, int macroHeight) {
		this.macroWidth = macroWidth;
		this.macroHeight = macroHeight;
	}

	@Override
	public void analyseImage(FImage in) {
		super.analyseImage(in);

		final FImage[] tmpResp = responses;
		responses = new FImage[9];

		responses[0] = absAverage(tmpResp[L5E5], tmpResp[E5L5]);
		responses[1] = absAverage(tmpResp[L5R5], tmpResp[R5L5]);
		responses[2] = absAverage(tmpResp[E5S5], tmpResp[S5E5]);
		responses[3] = tmpResp[S5S5].abs();
		responses[4] = tmpResp[R5R5].abs();
		responses[5] = absAverage(tmpResp[L5S5], tmpResp[S5L5]);
		responses[6] = tmpResp[E5E5].abs();
		responses[7] = absAverage(tmpResp[E5R5], tmpResp[R5E5]);
		responses[8] = absAverage(tmpResp[S5R5], tmpResp[R5S5]);

		for (int i = 0; i < 9; i++) {
			responses[i] = responses[i].processInplace(new SumBoxFilter(macroWidth, macroHeight));
		}
	}

	private FImage absAverage(FImage i1, FImage i2) {
		final FImage img = new FImage(i1.width, i1.height);

		for (int y = 0; y < img.height; y++)
			for (int x = 0; x < img.width; x++)
				img.pixels[y][x] = Math.abs(i1.pixels[y][x] + i2.pixels[y][x]) / 2;

		return img;
	}

}
