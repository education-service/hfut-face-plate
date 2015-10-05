package edu.hfut.fr.driver.run.impl;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FileManagerHD {

	public static void main(String[] args) throws IOException {
		Matrix matrix = FileManagerHD.convertPNGtoMatrix("Face_DB/s1/1.png");
		System.err.println(matrix.get(10, 21));
	}

	public static Matrix convertPNGtoMatrix(String address) throws IOException {
		//		FImage image = ImageUtilities.readF(new File(address));
		BufferedImage image = ImageIO.read(new File(address));
		int picWidth = image.getWidth();
		int picHeight = image.getHeight();

		//		double[] feature = FImage2DoubleFV.INSTANCE.extractFeature(image).getVector();

		double[][] data2D = new double[picHeight][picWidth];
		for (int row = 0; row < picHeight; row++) {
			for (int col = 0; col < picWidth; col++) {
				//								data2D[row][col] = feature[row * 80 + col];
				//				data2D[row][col] = image.getPixel(row, col);
				data2D[row][col] = image.getRaster().getSample(row, col, 0);
			}
		}

		return new Matrix(data2D);
	}

	public static Matrix normalize(Matrix input) {
		int row = input.getRowDimension();

		for (int i = 0; i < row; i++) {
			input.set(i, 0, 0 - input.get(i, 0));

		}

		double max = input.get(0, 0);
		double min = input.get(0, 0);

		for (int i = 1; i < row; i++) {
			if (max < input.get(i, 0))
				max = input.get(i, 0);

			if (min > input.get(i, 0))
				min = input.get(i, 0);

		}

		Matrix result = new Matrix(80, 80);
		for (int p = 0; p < 80; p++) {
			for (int q = 0; q < 80; q++) {
				double value = input.get(p * 80 + q, 0);
				value = (value - min) * 255 / (max - min);
				result.set(q, p, value);
			}
		}

		return result;
	}

	public static void convertMatricetoImage(Matrix x, int featureMode) throws IOException {
		int row = x.getRowDimension();
		int column = x.getColumnDimension();

		for (int i = 0; i < column; i++) {
			Matrix eigen = normalize(x.getMatrix(0, row - 1, i, i));

			BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster raster = img.getRaster();

			for (int m = 0; m < 80; m++) {
				for (int n = 0; n < 80; n++) {
					int value = (int) eigen.get(m, n);
					raster.setSample(n, m, 0, value);
				}
			}

			File file = null;
			if (featureMode == 0)
				file = new File("Eigenface" + i + ".bmp");
			else if (featureMode == 1)
				file = new File("Fisherface" + i + ".bmp");
			else if (featureMode == 2)
				file = new File("Laplacianface" + i + ".bmp");

			if (!file.exists())
				file.createNewFile();

			//			ImageIO.write(img, "bmp", file);
		}

	}

	public static void convertToImage(Matrix input, int name) throws IOException {
		File file = new File(name + " dimensions.bmp");
		if (!file.exists())
			file.createNewFile();

		BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = img.getRaster();

		for (int m = 0; m < 80; m++) {
			for (int n = 0; n < 80; n++) {
				int value = (int) input.get(n * 80 + m, 0);
				raster.setSample(n, m, 0, value);
			}
		}

		//		ImageIO.write(img, "bmp", file);
	}

}
