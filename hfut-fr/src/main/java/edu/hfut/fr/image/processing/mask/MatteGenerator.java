package edu.hfut.fr.image.processing.mask;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * 灰度matte 产生器
 *
 *@author jimbo
 */
public class MatteGenerator {

	/**
	 * matte算法的枚举值
	 */
	public enum MatteType {

		LINEAR_VERTICAL_GRADIENT {
			@Override
			public void generateMatte(final FImage img, final Object... args) {
				boolean whiteAtTop = false;
				if (args.length == 0 || ((args[0] instanceof Boolean) && !((Boolean) args[0]).booleanValue()))
					whiteAtTop = true;

				final double g = (whiteAtTop ? 1 : 0);
				final double scalar = (whiteAtTop ? -1d / img.getHeight() : 1d / img.getHeight());

				for (int y = 0; y < img.getHeight(); y++)
					for (int x = 0; x < img.getWidth(); x++)
						img.pixels[y][x] = (float) (g + y * scalar);
			}
		},

		LINEAR_HORIZONTAL_GRADIENT {
			@Override
			public void generateMatte(final FImage img, final Object... args) {
				boolean whiteAtLeft = false;
				if (args.length == 0 || ((args[0] instanceof Boolean) && !((Boolean) args[0]).booleanValue()))
					whiteAtLeft = true;

				final double g = (whiteAtLeft ? 1 : 0);
				final double scalar = (whiteAtLeft ? -1d / img.getWidth() : 1d / img.getWidth());

				for (int y = 0; y < img.getHeight(); y++)
					for (int x = 0; x < img.getWidth(); x++)
						img.pixels[y][x] = (float) (g + x * scalar);
			}
		},

		RADIAL_GRADIENT {
			@Override
			public void generateMatte(final FImage img, final Object... args) {
				boolean whiteInCentre = false;
				if (args.length > 0 && args[0] instanceof Boolean && ((Boolean) args[0]).booleanValue())
					whiteInCentre = true;

				final int cx = img.getWidth() / 2;
				final int cy = img.getHeight() / 2;

				final int maxDist = Math.max(Math.max(img.getWidth() - cx, cx), Math.max(img.getHeight() - cy, cy));
				final double scale = maxDist;

				for (int y = 0; y < img.getHeight(); y++)
					for (int x = 0; x < img.getWidth(); x++)
						img.pixels[y][x] = whiteInCentre ? 1f - (float) this.distanceFromCentre(cx, cy, x, y, scale)
								: (float) this.distanceFromCentre(cx, cy, x, y, scale);
			}

			/**
			 * 计算距离中心位置的距离
			 *
			 */
			private double distanceFromCentre(final int cx, final int cy, final int x, final int y, final double scale) {
				final double b = cx - x;
				final double c = cy - y;
				double v = Math.abs(Math.sqrt(b * b + c * c)) / scale;
				if (v > 1)
					v = 1;
				if (v < 0)
					v = 0;
				return v;
			}
		},

		ANGLED_LINEAR_GRADIENT {

			@Override
			public void generateMatte(final FImage img, final Object... args) {
				double angle = 0;
				double lx = 0;
				double ly = 0;

				if (args.length > 0 && args[0] instanceof Double)
					angle = ((Double) args[0]).doubleValue();
				if (args.length > 1 && args[1] instanceof Double)
					lx = ((Double) args[1]).doubleValue();
				if (args.length > 2 && args[2] instanceof Double)
					ly = ((Double) args[2]).doubleValue();

				final double scalar = Math.max(Math.max(img.getWidth() - lx, lx), Math.max(img.getHeight() - ly, ly));

				for (int y = 0; y < img.getHeight(); y++)
					for (int x = 0; x < img.getWidth(); x++)
						img.pixels[y][x] = (float) this.distanceFromAxis(lx, ly, angle, x, y, scalar);
			}

			/**
			 * 计算距离轴的距离
			 *
			 */
			private double distanceFromAxis(final double lx, final double ly, final double angle, final double x,
					final double y, final double scalar) {
				final Line2d line = Line2d.lineFromRotation((int) lx, (int) ly, angle, 1);
				final Point2d A = line.begin;
				final Point2d B = line.end;
				final Point2dImpl P = new Point2dImpl((float) x, (float) y);
				final double normalLength = Math.hypot(B.getX() - A.getX(), B.getY() - A.getY());
				double grad = Math.abs((P.x - A.getX()) * (B.getY() - A.getY()) - (P.y - A.getY())
						* (B.getX() - A.getX()))
						/ normalLength / scalar;
				if (grad < 0)
					grad = 0;
				if (grad > 1)
					grad = 1;
				return grad;
			}
		};

		public abstract void generateMatte(FImage img, Object... args);
	}

	/**
	 * 在指定的图像中产生一个matte
	 */
	public static FImage generateMatte(final MBFImage image, final int band, final MatteType type, final Object... args) {
		return MatteGenerator.generateMatte(image.getBand(band), type, args);
	}

	/**
	 * 在指定的图像中产生一个matte
	 */
	public static FImage generateMatte(final FImage image, final MatteType type, final Object... args) {
		type.generateMatte(image, args);
		return image;
	}

}
