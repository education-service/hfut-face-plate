package edu.hfut.fr.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import com.jsaragih.CLM;
import com.jsaragih.FDet;
import com.jsaragih.IO;
import com.jsaragih.MFCheck;

import edu.hfut.fr.image.analysis.algorithm.FourierTemplateMatcher;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.resize.ResizeProcessor;

/**
 * 基于CLM的跟踪器，用于处理一段视频中可能出现多个跟踪目标的情况
 *
 * @author jimbo
 */
public class MultiTracker {

	public static class TrackedFace extends DetectedFace {
		public CLM clm;

		public Matrix shape;

		public Matrix referenceShape;

		public FImage templateImage;

		public Rectangle lastMatchBounds;

		public Rectangle redetectedBounds;

		protected boolean gen = true;

		public TrackedFace(final Rectangle r, final TrackerVars tv) {
			this.redetectedBounds = r;
			this.clm = tv.clm.copy();
			this.shape = tv.shape.copy();
			this.referenceShape = tv.referenceShape.copy();
		}

		@Override
		public Rectangle getBounds() {
			return this.lastMatchBounds;
		}

		@Override
		public String toString() {
			return "Face[" + (this.redetectedBounds == null ? "null" : this.redetectedBounds.toString()) + "]";
		}
	}

	public static class TrackerVars {
		public CLM clm;

		public Matrix shape;

		public Matrix referenceShape;

		public FDet faceDetector;

		public MFCheck failureCheck;

		double[] similarity;
	}

	private static final double TSCALE = 0.3;

	public List<TrackedFace> trackedFaces = new ArrayList<TrackedFace>();

	private TrackerVars initialTracker = null;

	private long framesSinceLastDetection;

	private FImage currentFrame;

	private FImage small_;

	public MultiTracker(final CLM clm, final FDet fdet, final MFCheck fcheck, final Matrix rshape, final double[] simil) {
		this.initialTracker = new TrackerVars();
		this.initialTracker.clm = clm;
		this.initialTracker.clm._pdm.identity(clm._plocal, clm._pglobl);
		this.initialTracker.faceDetector = fdet;
		this.initialTracker.failureCheck = fcheck;
		this.initialTracker.referenceShape = rshape.copy();
		this.initialTracker.similarity = simil;
		this.initialTracker.shape = new Matrix(2 * clm._pdm.nPoints(), 1);
		this.framesSinceLastDetection = -1;
	}

	public MultiTracker(final TrackerVars tv) {
		this.initialTracker = tv;
		this.framesSinceLastDetection = -1;
	}

	protected MultiTracker() {
	}

	public void frameReset() {
		this.framesSinceLastDetection = -1;
		this.trackedFaces.clear();
	}

	public int track(final FImage im, final int[] wSize, final int fpd, final int nIter, final double clamp,
			final double fTol, final boolean fcheck, final float searchAreaSize) {
		this.currentFrame = im;

		if ((this.framesSinceLastDetection < 0) || (fpd >= 0 && fpd < this.framesSinceLastDetection)) {
			this.framesSinceLastDetection = 0;
			final List<Rectangle> RL = this.initialTracker.faceDetector.detect(this.currentFrame);

			if (this.trackedFaces.size() == 0) {
				for (final Rectangle r : RL)
					this.trackedFaces.add(new TrackedFace(r, this.initialTracker));
			} else {
				this.trackRedetect(this.currentFrame, searchAreaSize);

				final int sz = this.trackedFaces.size();
				for (final Rectangle r : RL) {
					boolean found = false;
					for (int i = 0; i < sz; i++) {
						if (r.percentageOverlap(this.trackedFaces.get(i).redetectedBounds) > 0.5) {
							found = true;
							break;
						}
					}

					if (!found)
						this.trackedFaces.add(new TrackedFace(r, this.initialTracker));
				}
			}
		} else {
			this.trackRedetect(this.currentFrame, searchAreaSize);
		}

		if (this.trackedFaces.size() == 0)
			return -1;

		boolean resize = true;

		for (final Iterator<TrackedFace> iterator = this.trackedFaces.iterator(); iterator.hasNext();) {
			final TrackedFace f = iterator.next();

			if ((f.redetectedBounds.width == 0) || (f.redetectedBounds.height == 0)) {
				iterator.remove();
				this.framesSinceLastDetection = -1;
				continue;
			}

			if (f.gen) {
				this.initShape(f.redetectedBounds, f.shape, f.referenceShape);
				f.clm._pdm.calcParams(f.shape, f.clm._plocal, f.clm._pglobl);
			} else {
				final double tx = f.redetectedBounds.x - f.lastMatchBounds.x;
				final double ty = f.redetectedBounds.y - f.lastMatchBounds.y;

				f.clm._pglobl.getArray()[4][0] += tx;
				f.clm._pglobl.getArray()[5][0] += ty;

				resize = false;
			}

			f.clm.fit(this.currentFrame, wSize, nIter, clamp, fTol);
			f.clm._pdm.calcShape2D(f.shape, f.clm._plocal, f.clm._pglobl);

			if (fcheck) {
				if (!this.initialTracker.failureCheck.check(f.clm.getViewIdx(), this.currentFrame, f.shape)) {
					iterator.remove();
					continue;
				}
			}

			f.lastMatchBounds = this.updateTemplate(f, this.currentFrame, f.shape, resize);

			if ((f.lastMatchBounds.width == 0) || (f.lastMatchBounds.height == 0)) {
				iterator.remove();
				this.framesSinceLastDetection = -1;
				continue;
			}
		}

		if (this.trackedFaces.size() == 0)
			return -1;

		this.framesSinceLastDetection++;

		return 0;
	}

	public void initShape(final Rectangle r, final Matrix shape, final Matrix _rshape) {
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape.getColumnDimension() == _rshape
				.getColumnDimension()));

		final int n = _rshape.getRowDimension() / 2;

		final double a = r.width * Math.cos(this.initialTracker.similarity[1]) * this.initialTracker.similarity[0] + 1;
		final double b = r.width * Math.sin(this.initialTracker.similarity[1]) * this.initialTracker.similarity[0];

		final double tx = r.x + (int) (r.width / 2) + r.width * this.initialTracker.similarity[2];
		final double ty = r.y + (int) (r.height / 2) + r.height * this.initialTracker.similarity[3];

		final double[][] s = _rshape.getArray();
		final double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}

	private void trackRedetect(final FImage im, final float searchAreaSize) {
		final int ww = im.width;
		final int hh = im.height;

		this.small_ = ResizeProcessor.resample(im, (int) (MultiTracker.TSCALE * ww), (int) (MultiTracker.TSCALE * hh));

		for (final TrackedFace f : this.trackedFaces) {
			f.gen = false;

			Rectangle searchAreaBounds = f.lastMatchBounds.clone();
			searchAreaBounds.scale((float) MultiTracker.TSCALE);
			searchAreaBounds.scaleCentroid(searchAreaSize);
			searchAreaBounds = searchAreaBounds.overlapping(this.small_.getBounds());

			final FImage searchArea = this.small_.extractROI(searchAreaBounds);

			final FourierTemplateMatcher matcher = new FourierTemplateMatcher(f.templateImage,
					FourierTemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
			matcher.analyseImage(searchArea);

			final float[][] ncc_ = matcher.getResponseMap().pixels;

			f.redetectedBounds = f.templateImage.getBounds();

			final int h = searchArea.height - f.templateImage.height + 1;
			final int w = searchArea.width - f.templateImage.width + 1;
			float vb = -2;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					final float v = ncc_[y][x];
					if (v > vb) {
						vb = v;
						f.redetectedBounds.x = x + searchAreaBounds.x;
						f.redetectedBounds.y = y + searchAreaBounds.y;
					}
				}
			}

			f.redetectedBounds.scale((float) (1d / MultiTracker.TSCALE));
		}
	}

	protected Rectangle updateTemplate(final TrackedFace f, final FImage im, final Matrix s, final boolean resize) {
		final int n = s.getRowDimension() / 2;

		final double[][] sv = s.getArray();
		double xmax = sv[0][0], ymax = sv[n][0], xmin = sv[0][0], ymin = sv[n][0];

		for (int i = 0; i < n; i++) {
			final double vx = sv[i][0];
			final double vy = sv[i + n][0];

			xmax = Math.max(xmax, vx);
			ymax = Math.max(ymax, vy);

			xmin = Math.min(xmin, vx);
			ymin = Math.min(ymin, vy);
		}

		if ((xmin < 0) || (ymin < 0) || (xmax >= im.width) || (ymax >= im.height) || Double.isNaN(xmin)
				|| Double.isInfinite(xmin) || Double.isNaN(xmax) || Double.isInfinite(xmax) || Double.isNaN(ymin)
				|| Double.isInfinite(ymin) || Double.isNaN(ymax) || Double.isInfinite(ymax)) {
			return new Rectangle(0, 0, 0, 0);
		} else {
			xmin *= MultiTracker.TSCALE;
			ymin *= MultiTracker.TSCALE;
			xmax *= MultiTracker.TSCALE;
			ymax *= MultiTracker.TSCALE;

			final Rectangle R = new Rectangle((float) Math.floor(xmin), (float) Math.floor(ymin),
					(float) Math.ceil(xmax - xmin), (float) Math.ceil(ymax - ymin));

			final int ww = im.width;
			final int hh = im.height;

			if (resize)
				this.small_ = ResizeProcessor.resample(im, (int) (MultiTracker.TSCALE * ww),
						(int) (MultiTracker.TSCALE * hh));

			f.templateImage = this.small_.extractROI(R);

			R.x *= 1.0 / MultiTracker.TSCALE;
			R.y *= 1.0 / MultiTracker.TSCALE;
			R.width *= 1.0 / MultiTracker.TSCALE;
			R.height *= 1.0 / MultiTracker.TSCALE;

			return R;
		}
	}

	/**
	 * 加载跟踪器
	 *
	 */
	public static TrackerVars load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			final Scanner sc = new Scanner(br);
			return MultiTracker.read(sc, true);
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * 加载跟踪器
	 *
	 */
	public static TrackerVars load(final InputStream in) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			final Scanner sc = new Scanner(br);
			return MultiTracker.read(sc, true);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * 加载跟踪器
	 *
	 */
	private static TrackerVars read(final Scanner s, final boolean readType) {
		if (readType) {
			final int type = s.nextInt();
			assert (type == IO.Types.TRACKER.ordinal());
		}
		final TrackerVars trackerVars = new TrackerVars();
		trackerVars.clm = CLM.read(s, true);
		trackerVars.faceDetector = FDet.read(s, true);
		trackerVars.failureCheck = MFCheck.read(s, true);
		trackerVars.referenceShape = IO.readMat(s);
		trackerVars.similarity = new double[] { s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble() };
		trackerVars.shape = new Matrix(2 * trackerVars.clm._pdm.nPoints(), 1);
		trackerVars.clm._pdm.identity(trackerVars.clm._plocal, trackerVars.clm._pglobl);

		return trackerVars;
	}

	/**
	 * 返回初始化以后的人脸跟踪器
	 */
	public TrackerVars getInitialVars() {
		return this.initialTracker;
	}

}
