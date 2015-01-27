package edu.hfut.fr.image.analysis.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.MultidimensionalDoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.processing.edges.CannyEdgeDetector2;

/**
 * 边缘协同向量
 *
 * @author wanggang
 *
 */
@SuppressWarnings("deprecation")
public class EdgeDirectionCoherenceVector implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {

	/**
	 * 边缘协同向量直方图
	 */
	public class EdgeDirectionCoherenceHistogram {

		public Histogram coherentHistogram = null;

		public Histogram incoherentHistogram = null;

		public DoubleFV asDoubleFV() {
			final double[] d = new double[coherentHistogram.values.length + incoherentHistogram.values.length];
			int i = 0;
			for (final double dd : coherentHistogram.asDoubleVector())
				d[i++] = dd;
			for (final double dd : incoherentHistogram.asDoubleVector())
				d[i++] = dd;
			return new DoubleFV(d);
		}

		public MultidimensionalDoubleFV asMultidimensionalDoubleFV() {
			final double[][] d = new double[2][coherentHistogram.values.length];
			int i = 0;
			for (final double dd : coherentHistogram.asDoubleVector())
				d[0][i++] = dd;
			i = 0;
			for (final double dd : incoherentHistogram.asDoubleVector())
				d[1][i++] = dd;
			return new MultidimensionalDoubleFV(d);
		}

	}

	private EdgeDirectionCoherenceHistogram coDirHist = null;

	private int numberOfDirBins = 72;

	private float directionThreshold = 360 / numberOfDirBins;

	private ConnectedComponent.ConnectMode mode = ConnectedComponent.ConnectMode.CONNECT_8;

	private double coherenceFactor = 0.00002;

	private CannyEdgeDetector2 cannyEdgeDetector = null;

	public EdgeDirectionCoherenceVector() {
		cannyEdgeDetector = new CannyEdgeDetector2();
	}

	public int getNumberOfDirBins() {
		return numberOfDirBins;
	}

	public void setNumberOfBins(int nb) {
		this.numberOfDirBins = nb;
		this.directionThreshold = 360 / numberOfDirBins;
	}

	public EdgeDirectionCoherenceHistogram getLastHistogram() {
		return coDirHist;
	}

	@Override
	public void analyseImage(FImage image) {

		final int w = image.getWidth();
		final int h = image.getHeight();

		// 计算边缘图像
		final FImage edgeImage = image.clone();
		cannyEdgeDetector.processImage(edgeImage);

		final float[] mags = cannyEdgeDetector.getMagnitude();
		final float[] dirs = cannyEdgeDetector.getOrientation();

		if (mags == null || dirs == null)
			System.out.println("Canny Edge Detector did not " + "return magnitude or direction.");

		final int numberOfBins = numberOfDirBins + 1;

		final double[] dirHist = new double[numberOfBins];

		int nonEdgeCount = 0;
		for (int y = 0; y < edgeImage.getHeight(); y++)
			for (int x = 0; x < edgeImage.getWidth(); x++)
				if (edgeImage.getPixel(x, y) == 0)
					nonEdgeCount++;
		dirHist[0] = nonEdgeCount;

		final FImage directionImage = new FImage(w, h);
		for (int j = 0; j < w * h; j++) {
			final int x = j % w;
			final int y = j / w;

			if (edgeImage.getPixel(x, y) > 0) {
				final int dirBin = (int) ((dirs[j] + 180) * numberOfDirBins / 360f) % numberOfDirBins;
				dirHist[dirBin + 1]++;

				final float v = (dirs[j] + 180);
				directionImage.setPixel(x, y, v);
			} else
				directionImage.setPixel(x, y, -1f);
		}

		final int numberOfEdgePix = w * h - nonEdgeCount;

		for (int j = 0; j < numberOfDirBins; j++)
			dirHist[j + 1] /= numberOfEdgePix;
		dirHist[0] /= w * h;

		coDirHist = new EdgeDirectionCoherenceHistogram();
		coDirHist.coherentHistogram = new Histogram(numberOfDirBins);
		coDirHist.incoherentHistogram = new Histogram(numberOfDirBins);

		final FImage outputImage = new FImage(w, h);

		for (int j = 0; j < w * h; j++) {
			final int x = j % w;
			final int y = j / w;

			final float p = directionImage.getPixel(x, y);

			if (p != -1) {
				final List<Point2d> v = getConnectedEdges(x, y, w, h, p, numberOfBins, directionImage, dirs, mode);

				final int dirBin = (int) ((dirs[j] + 180) * numberOfDirBins / 360f) % numberOfDirBins;

				boolean isCoherent = false;
				if (v.size() > (w * h * coherenceFactor)) {
					for (int k = 0; k < v.size(); k++) {
						final Point2d pp = v.get(k);
						outputImage.setPixel(Math.round(pp.getX()), Math.round(pp.getY()), 1f);
					}

					isCoherent = true;
				}

				if (isCoherent)
					coDirHist.coherentHistogram.values[dirBin] += v.size();
				else
					coDirHist.incoherentHistogram.values[dirBin] += v.size();
			}
		}

		image.internalAssign(outputImage);
	}

	private List<Point2d> getConnectedEdges(int xx, int yy, int w, int h, float p, int numberOfBins, FImage edgeImage,
			float[] dirs, ConnectedComponent.ConnectMode connectedness) {

		final List<Point2d> v = new ArrayList<Point2d>();

		v.add(new Point2dImpl(xx, yy));

		edgeImage.setPixel(xx, yy, -1f);

		final float dir = dirs[yy * w + xx];
		boolean connected = true;
		int x = xx, y = yy;
		while (connected) {
			int nx = x, ny = y;

			switch (connectedness) {
			case CONNECT_4:
				nx = x + 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y;
				break;

			case CONNECT_8:
				nx = x + 1;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x + 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x + 1;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y + 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x - 1;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y - 1;
				if (nx >= 0 && ny >= 0 && nx < w && ny < h && dirs[ny * w + nx] < dir + directionThreshold
						&& dirs[ny * w + nx] > dir - directionThreshold && edgeImage.getPixel(nx, ny) != -1)
					break;
				nx = x;
				ny = y;
				break;
			}

			if ((nx >= 0 && nx != x) || (ny >= 0 && ny != y)) {
				v.add(new Point2dImpl(nx, ny));
				edgeImage.setPixel(nx, ny, -1f);
				x = nx;
				y = ny;
			} else
				connected = false;
		}
		return v;
	}

	public EdgeDirectionCoherenceHistogram getHistogram() {
		return coDirHist;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return coDirHist.asMultidimensionalDoubleFV();
	}

	public CannyEdgeDetector2 getCannyEdgeDetector() {
		return cannyEdgeDetector;
	}

	/**
	 * 获取协同因子
	 */
	public double getCoherenceFactor() {
		return coherenceFactor;
	}

	/**
	 * 设置协同因子
	 */
	public void setCoherenceFactor(double coherenceFactor) {
		this.coherenceFactor = coherenceFactor;
	}

}
