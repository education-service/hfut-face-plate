package edu.hfut.fr.image.processing.algorithm;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.mask.AbstractMaskedObject;
import org.openimaj.image.processor.ImageProcessor;

/**
 * 鲁棒的对比度均衡化
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Article, author = { "Tan, Xiaoyang", "Triggs, Bill" }, title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions", year = "2010", journal = "Trans. Img. Proc.", pages = {
		"1635", "", "1650" }, url = "http://dx.doi.org/10.1109/TIP.2010.2042645", month = "June", number = "6", publisher = "IEEE Press", volume = "19")
public class MaskedRobustContrastEqualisation extends AbstractMaskedObject<FImage> implements ImageProcessor<FImage> {

	double alpha = 0.1;
	double tau = 10;

	public MaskedRobustContrastEqualisation() {
		super();
	}

	/**
	 * 构造函数
	 */
	public MaskedRobustContrastEqualisation(FImage mask) {
		super(mask);
	}

	@Override
	public void processImage(FImage image) {
		image.divideInplace(firstPassDivisor(image, mask));

		image.divideInplace(secondPassDivisor(image, mask));

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					image.pixels[y][x] = (float) (tau * Math.tanh(image.pixels[y][x] / tau));
				} else {
					image.pixels[y][x] = 0;
				}
			}
		}
	}

	float firstPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];

					accum += Math.pow(Math.abs(ixy), alpha);
					count++;
				}
			}
		}

		return (float) Math.pow(accum / count, 1.0 / alpha);
	}

	float secondPassDivisor(FImage image, FImage mask) {
		double accum = 0;
		int count = 0;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (mask.pixels[y][x] == 1) {
					double ixy = image.pixels[y][x];

					accum += Math.pow(Math.min(tau, Math.abs(ixy)), alpha);
					count++;
				}
			}
		}

		return (float) Math.pow(accum / count, 1.0 / alpha);
	}

}
