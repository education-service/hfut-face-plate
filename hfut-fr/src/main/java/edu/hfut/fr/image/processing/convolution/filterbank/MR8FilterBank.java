package edu.hfut.fr.image.processing.convolution.filterbank;

import org.openimaj.image.FImage;

/**
 * MBR8 过滤器组实现类
 *
 * @author wanghao
 */
public class MR8FilterBank extends RootFilterSetFilterBank {

	@Override
	public void analyseImage(FImage image) {
		super.analyseImage(image);

		final FImage[] allresponses = responses;
		responses = new FImage[8];

		int allIndex = 0;
		int idx = 0;
		for (int type = 0; type < 2; type++) {
			for (int scale = 0; scale < SCALES.length; scale++) {
				responses[idx] = allresponses[allIndex];
				allIndex++;

				for (int orient = 1; orient < NUM_ORIENTATIONS; orient++) {
					for (int y = 0; y < image.height; y++) {
						for (int x = 0; x < image.width; x++) {
							responses[idx].pixels[y][x] = Math.max(responses[idx].pixels[y][x],
									allresponses[allIndex].pixels[y][x]);
						}
					}

					allIndex++;
				}

				idx++;
			}
		}

		responses[idx++] = allresponses[allIndex++];
		responses[idx++] = allresponses[allIndex++];
	}

}
