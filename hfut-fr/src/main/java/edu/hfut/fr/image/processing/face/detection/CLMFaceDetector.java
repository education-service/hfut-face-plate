package edu.hfut.fr.image.processing.face.detection;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import com.jsaragih.CLM;
import com.jsaragih.FDet;
import com.jsaragih.IO;
import com.jsaragih.MFCheck;
import com.jsaragih.Tracker;

/**
 * 在本地训练完成模型上脸部检测
 *
 *@author wanggang
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Jason M. Saragih", "Simon Lucey", "Jeffrey F. Cohn" }, title = "Face alignment through subspace constrained mean-shifts", year = "2009", booktitle = "IEEE 12th International Conference on Computer Vision, ICCV 2009, Kyoto, Japan, September 27 - October 4, 2009", pages = {
		"1034", "1041" }, publisher = "IEEE", customData = { "doi", "http://dx.doi.org/10.1109/ICCV.2009.5459377",
		"researchr", "http://researchr.org/publication/SaragihLC09", "cites", "0", "citedby", "0" })
public class CLMFaceDetector implements FaceDetector<CLMDetectedFace, FImage> {

	/**
	 * 面部检测器配置
	 *
	 */
	public static class Configuration {
		public CLM clm;

		public Matrix referenceShape;

		public Matrix shape;

		/** 面部检测器 */
		public FDet faceDetector;

		public MFCheck failureCheck;

		double[] similarity;

		public int[][] triangles = null;

		public int[][] connections = null;

		public boolean fcheck = false;

		public int[] windowSize = { 11, 9, 7 };

		public int nIter = 5;

		public double clamp = 3;

		public double fTol = 0.01;

		private void read(final InputStream in) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(in));
				final Scanner sc = new Scanner(br);
				read(sc, true);
			} finally {
				try {
					br.close();
				} catch (final IOException e) {
				}
			}
		}

		private void read(Scanner s, boolean readType) {
			if (readType) {
				final int type = s.nextInt();
				assert (type == IO.Types.TRACKER.ordinal());
			}

			clm = CLM.read(s, true);
			faceDetector = FDet.read(s, true);
			failureCheck = MFCheck.read(s, true);
			referenceShape = IO.readMat(s);
			similarity = new double[] { s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble() };
			shape = new Matrix(2 * clm._pdm.nPoints(), 1);
			clm._pdm.identity(clm._plocal, clm._pglobl);
		}

		/**
		 * 默认模型构造
		 */
		public Configuration() {
			read(Tracker.class.getResourceAsStream("face2.tracker"));
			triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
			connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
		}
	}

	private Configuration config;

	/**
	 * 默认构造
	 */
	public CLMFaceDetector() {
		config = new Configuration();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		config = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(config, out);
	}

	@Override
	public List<CLMDetectedFace> detectFaces(FImage image) {
		final List<Rectangle> detRects = config.faceDetector.detect(image);

		return detectFaces(image, detRects);
	}

	public List<CLMDetectedFace> detectFaces(FImage image, List<Rectangle> detRects) {
		final List<CLMDetectedFace> faces = new ArrayList<CLMDetectedFace>();

		for (final Rectangle f : detRects) {
			if ((f.width == 0) || (f.height == 0)) {
				continue;
			}

			initShape(f, config.shape, config.referenceShape);
			config.clm._pdm.calcParams(config.shape, config.clm._plocal, config.clm._pglobl);

			config.clm.fit(image, config.windowSize, config.nIter, config.clamp, config.fTol);
			config.clm._pdm.calcShape2D(config.shape, config.clm._plocal, config.clm._pglobl);

			if (config.fcheck) {
				if (!config.failureCheck.check(config.clm.getViewIdx(), image, config.shape)) {
					continue;
				}
			}

			faces.add(new CLMDetectedFace(f, config.shape.copy(), config.clm._pglobl.copy(), config.clm._plocal.copy(),
					config.clm._visi[config.clm.getViewIdx()].copy(), image));
		}

		return faces;
	}

	private void initShape(final Rectangle r, final Matrix shape, final Matrix _rshape) {
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape.getColumnDimension() == _rshape
				.getColumnDimension()));

		final int n = _rshape.getRowDimension() / 2;

		final double a = r.width * Math.cos(config.similarity[1]) * config.similarity[0] + 1;
		final double b = r.width * Math.sin(config.similarity[1]) * config.similarity[0];

		final double tx = r.x + (int) (r.width / 2) + r.width * config.similarity[2];
		final double ty = r.y + (int) (r.height / 2) + r.height * config.similarity[3];

		final double[][] s = _rshape.getArray();
		final double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}

	/**
	 * 获得检测器内部配置
	 */
	public Configuration getConfiguration() {
		return config;
	}

}
