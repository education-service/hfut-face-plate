package edu.hfut.fr.image.feature.dense.gradient.binning;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.util.array.ArrayUtils;

import edu.hfut.fr.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import edu.hfut.fr.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;

/**
 * FixedHOGStrategy实现方法
 *
 * @author wanghao
 */
@Reference(type = ReferenceType.Inproceedings, author = { "Dalal, Navneet", "Triggs, Bill" }, title = "Histograms of Oriented Gradients for Human Detection", year = "2005", booktitle = "Proceedings of the 2005 IEEE Computer Society Conference on Computer Vision and Pattern Recognition (CVPR'05) - Volume 1 - Volume 01", pages = {
		"886", "", "893" }, url = "http://dx.doi.org/10.1109/CVPR.2005.177", publisher = "IEEE Computer Society", series = "CVPR '05", customData = {
		"isbn", "0-7695-2372-2", "numpages", "8", "doi", "10.1109/CVPR.2005.177", "acmid", "1069007", "address",
		"Washington, DC, USA" })
public class FixedHOGStrategy implements SpatialBinningStrategy {

	public enum BlockNormalisation {
		L1 {
			@Override
			final void normalise(Histogram h, int blockArea) {
				h.normaliseL1();
			}
		},
		L2 {
			@Override
			final void normalise(Histogram h, int blockArea) {
				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				ArrayUtils.divide(h.values, blockArea);
			}
		},
		L1sqrt {
			@Override
			final void normalise(Histogram h, int blockArea) {
				h.normaliseL1();

				for (int x = 0; x < h.values.length; x++)
					h.values[x] = Math.sqrt(h.values[x]);
			}
		},
		L2clip {
			@Override
			final void normalise(Histogram h, int blockArea) {
				// each cell is l2 normed, so it follows that the l2 norm of the
				// block is simply the values divided by the area
				double sumsq = 0;
				for (int x = 0; x < h.values.length; x++) {
					h.values[x] = h.values[x] / blockArea;
					if (h.values[x] > 0.2)
						h.values[x] = 0.2;
					sumsq += h.values[x] * h.values[x];
				}

				final double invNorm = 1.0 / Math.sqrt(sumsq);
				for (int x = 0; x < h.values.length; x++) {
					h.values[x] *= invNorm;
				}
			}
		};

		abstract void normalise(Histogram h, int blockArea);
	}

	int cellWidth = 6;
	int cellHeight = 6;
	int cellsPerBlockX = 3;
	int cellsPerBlockY = 3;
	int blockStepX = 1;
	int blockStepY = 1;
	BlockNormalisation norm = BlockNormalisation.L2;

	/**
	 * 构造函数
	 */
	public FixedHOGStrategy(int cellSize, int cellsPerBlock, BlockNormalisation norm) {
		this(cellSize, cellsPerBlock, 1, norm);
	}

	public FixedHOGStrategy(int cellSize, int cellsPerBlock, int blockStep, BlockNormalisation norm) {
		this(cellSize, cellSize, cellsPerBlock, cellsPerBlock, blockStep, blockStep, norm);
	}

	public FixedHOGStrategy(int cellWidth, int cellHeight, int cellsPerBlockX, int cellsPerBlockY, int blockStepX,
			int blockStepY, BlockNormalisation norm) {
		super();
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.cellsPerBlockX = cellsPerBlockX;
		this.cellsPerBlockY = cellsPerBlockY;
		this.norm = norm;
		this.blockStepX = blockStepX;
		this.blockStepY = blockStepY;
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		final Histogram[][] cells = computeCells(binnedData, region);
		final Histogram[][] blocks = computeBlocks(cells);

		final int blockSize = blocks[0][0].values.length;
		final int blockArea = cellsPerBlockX * cellsPerBlockY;

		if (output == null || output.values.length != blocks[0].length * blocks.length * blockSize)
			output = new Histogram(blocks[0].length * blocks.length * blockSize);

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++, k++) {
				norm.normalise(blocks[j][i], blockArea);

				System.arraycopy(blocks[j][i].values, 0, output.values, k * blockSize, blockSize);
			}
		}

		return output;
	}

	private Histogram[][] computeBlocks(Histogram[][] cells) {
		final int numBlocksX = 1 + (cells[0].length - cellsPerBlockX) / this.blockStepX;
		final int numBlocksY = 1 + (cells.length - cellsPerBlockY) / this.blockStepY;
		final Histogram[][] blocks = new Histogram[numBlocksY][numBlocksX];

		for (int y = 0; y < numBlocksY; y++) {
			for (int x = 0; x < numBlocksX; x++) {
				final Histogram[] blockData = new Histogram[cellsPerBlockX * cellsPerBlockY];

				for (int j = 0, k = 0; j < cellsPerBlockY; j++) {
					for (int i = 0; i < cellsPerBlockX; i++) {
						blockData[k++] = cells[y * blockStepY + j][x * blockStepX + i];
					}
				}

				blocks[y][x] = new Histogram(blockData);
			}
		}
		return blocks;
	}

	private Histogram[][] computeCells(WindowedHistogramExtractor binnedData, Rectangle region) {
		final int numCellsX = (int) ((region.width + cellWidth / 2) / cellWidth);
		final int numCellsY = (int) ((region.height + cellHeight / 2) / cellHeight);

		final Histogram[][] cells = new Histogram[numCellsY][numCellsX];
		for (int j = 0, y = (int) region.y; j < numCellsY; j++, y += cellHeight) {
			for (int i = 0, x = (int) region.x; i < numCellsX; i++, x += cellWidth) {
				cells[j][i] = binnedData.computeHistogram(x, y, cellWidth, cellHeight);
				cells[j][i].normaliseL2();
			}
		}

		return cells;
	}

}
