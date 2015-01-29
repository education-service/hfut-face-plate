package edu.hfut.fr.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.dense.binarypattern.LocalTernaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.io.wrappers.WriteableArrayBinary;
import org.openimaj.io.wrappers.WriteableListBinary;

import edu.hfut.fr.image.analysis.algorithm.EuclideanDistanceTransform;
import edu.hfut.fr.image.processing.algorithm.DifferenceOfGaussian;
import edu.hfut.fr.image.processing.algorithm.GammaCorrection;
import edu.hfut.fr.image.processing.algorithm.MaskedRobustContrastEqualisation;
import edu.hfut.fr.image.processing.face.feature.FacialFeature;

/**
 * 基于LTP的特征提取中计算到指定点的距离
 *
 *@author jimbo
 */
@Reference(type = ReferenceType.Article, author = { "Tan, Xiaoyang", "Triggs, Bill" }, title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions", year = "2010", journal = "Trans. Img. Proc.", pages = {
		"1635", "1650" }, url = "http://dx.doi.org/10.1109/TIP.2010.2042645", month = "June", number = "6", publisher = "IEEE Press", volume = "19")
public class AbstractLtpDtFeature implements FacialFeature {

	public List<List<Pixel>> ltpPixels;

	private int width;
	private int height;
	private LTPWeighting weighting;

	private FImage[] cachedDistanceMaps;

	public AbstractLtpDtFeature(int width, int height, LTPWeighting weighting, List<List<Pixel>> ltpPixels) {
		this.width = width;
		this.height = height;
		this.weighting = weighting;
		this.ltpPixels = ltpPixels;
	}

	public FImage[] getDistanceMaps() {
		if (cachedDistanceMaps == null)
			cachedDistanceMaps = extractDistanceTransforms(constructSlices(ltpPixels, width, height), weighting);

		return cachedDistanceMaps;
	}

	protected static FImage normaliseImage(FImage image, FImage mask) {
		if (mask == null) {
			return image.process(new GammaCorrection()).processInplace(new DifferenceOfGaussian())
					.processInplace(new MaskedRobustContrastEqualisation());
		}

		return image.process(new GammaCorrection()).processInplace(new DifferenceOfGaussian())
				.processInplace(new MaskedRobustContrastEqualisation(mask)).multiply(mask);
	}

	protected static List<List<Pixel>> extractLTPSlicePixels(FImage image) {
		LocalTernaryPattern ltp = new LocalTernaryPattern(2, 8, 0.1f);
		image.analyseWith(ltp);

		List<List<Pixel>> positiveSlices = UniformBinaryPattern.extractPatternPixels(ltp.getPositivePattern(), 8);
		List<List<Pixel>> negativeSlices = UniformBinaryPattern.extractPatternPixels(ltp.getNegativePattern(), 8);

		positiveSlices.addAll(negativeSlices);

		return positiveSlices;
	}

	protected FImage[] extractDistanceTransforms(FImage[] slices, LTPWeighting weighting) {
		FImage[] dist = new FImage[slices.length];
		int width = slices[0].width;
		int height = slices[0].height;
		int[][] indices = new int[height][width];

		for (int i = 0; i < slices.length; i++) {
			if (slices[i] == null)
				continue;

			dist[i] = new FImage(width, height);

			EuclideanDistanceTransform.squaredEuclideanDistanceBinary(slices[i], dist[i], indices);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					dist[i].pixels[y][x] = weighting.weightDistance((float) Math.sqrt(dist[i].pixels[y][x]));
				}
			}
		}

		return dist;
	}

	protected FImage[] constructSlices(List<List<Pixel>> ltpPixels, int width, int height) {
		FImage[] slices = new FImage[ltpPixels.size()];

		for (int i = 0; i < slices.length; i++) {
			List<Pixel> pixels = ltpPixels.get(i);

			if (pixels == null)
				continue;

			slices[i] = new FImage(width, height);
			for (Pixel p : pixels) {
				slices[i].pixels[p.y][p.x] = 1;
			}
		}

		return slices;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		new ReadableListBinary<List<Pixel>>(ltpPixels) {

			@Override
			protected List<Pixel> readValue(DataInput in) throws IOException {
				List<Pixel> pixels = new ArrayList<Pixel>();

				new ReadableListBinary<Pixel>(pixels) {
					@Override
					protected Pixel readValue(DataInput in) throws IOException {
						Pixel p = new Pixel();
						p.readBinary(in);
						return p;
					}
				}.readBinary(in);

				return pixels;
			}

		}.readBinary(in);

		List<FImage> images = new ArrayList<FImage>();
		new ReadableListBinary<FImage>(images) {
			@Override
			protected FImage readValue(DataInput in) throws IOException {
				return ImageUtilities.readF(in);
			}
		}.readBinary(in);
		cachedDistanceMaps = images.size() == 0 ? null : images.toArray(new FImage[images.size()]);
	}

	@Override
	public byte[] binaryHeader() {
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		new WriteableListBinary<List<Pixel>>(ltpPixels) {

			@Override
			protected void writeValue(List<Pixel> v, DataOutput out) throws IOException {
				new WriteableListBinary<Pixel>(v) {

					@Override
					protected void writeValue(Pixel v, DataOutput out) throws IOException {
						v.writeBinary(out);
					}
				}.writeBinary(out);
			}

		}.writeBinary(out);

		new WriteableArrayBinary<FImage>(cachedDistanceMaps) {
			@Override
			protected void writeValue(FImage v, DataOutput out) throws IOException {
				ImageUtilities.write(v, "png", out);
			}
		}.writeBinary(out);
	}

}
