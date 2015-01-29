package edu.hfut.fr.image.feature.dense.gradient.binning;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

import edu.hfut.fr.image.analysis.algorithm.histogram.WindowedHistogramExtractor;
import edu.hfut.fr.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import edu.hfut.fr.image.feature.dense.gradient.binning.FixedHOGStrategy.BlockNormalisation;

/**
 * FlexibleHOGStrategy实现类
 *
 * @author wanghao
 */
public class FlexibleHOGStrategy implements SpatialBinningStrategy {

	int numCellsX = 8;
	int numCellsY = 16;
	int cellsPerBlockX = 2;
	int cellsPerBlockY = 2;
	BlockNormalisation norm = BlockNormalisation.L2;

	private int numBlocksX;
	private int numBlocksY;
	private int blockLength;
	private int blockArea;
	private int blockStepX;
	private int blockStepY;

	private transient Histogram[][] blocks;
	private transient Histogram[][] cells;

	/**
	 * 构造函数
	 */
	public FlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlock) {
		this(numCellsX, numCellsY, cellsPerBlock, 1, BlockNormalisation.L2);
	}

	public FlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlock, BlockNormalisation norm) {
		this(numCellsX, numCellsY, cellsPerBlock, 1, norm);
	}

	public FlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlock, int blockStep, BlockNormalisation norm) {
		this(numCellsX, numCellsY, cellsPerBlock, cellsPerBlock, blockStep, blockStep, norm);
	}

	public FlexibleHOGStrategy(int numCellsX, int numCellsY, int cellsPerBlockX, int cellsPerBlockY, int blockStepX,
			int blockStepY, BlockNormalisation norm) {
		super();
		this.numCellsX = numCellsX;
		this.numCellsY = numCellsY;
		this.cellsPerBlockX = cellsPerBlockX;
		this.cellsPerBlockY = cellsPerBlockY;
		this.norm = norm;
		this.blockStepX = blockStepX;
		this.blockStepY = blockStepY;

		numBlocksX = 1 + (numCellsX - cellsPerBlockX) / blockStepX;
		numBlocksY = 1 + (numCellsY - cellsPerBlockY) / blockStepY;
	}

	@Override
	public Histogram extract(WindowedHistogramExtractor binnedData, Rectangle region, Histogram output) {
		if (cells == null || cells[0][0].values.length != binnedData.getNumBins()) {
			cells = new Histogram[numCellsY][numCellsX];
			blocks = new Histogram[numBlocksY][numBlocksX];

			for (int j = 0; j < numCellsY; j++)
				for (int i = 0; i < numCellsX; i++)
					cells[j][i] = new Histogram(binnedData.getNumBins());

			for (int j = 0; j < numBlocksY; j++)
				for (int i = 0; i < numBlocksX; i++)
					blocks[j][i] = new Histogram(binnedData.getNumBins() * cellsPerBlockX * cellsPerBlockY);

			blockLength = blocks[0][0].values.length;
			blockArea = cellsPerBlockX * cellsPerBlockY;
		}

		computeCells(binnedData, region);
		computeBlocks(cells);

		if (output == null || output.values.length != blocks[0].length * blocks.length * blockLength)
			output = new Histogram(blocks[0].length * blocks.length * blockLength);

		for (int j = 0, k = 0; j < blocks.length; j++) {
			for (int i = 0; i < blocks[0].length; i++, k++) {
				norm.normalise(blocks[j][i], blockArea);

				System.arraycopy(blocks[j][i].values, 0, output.values, k * blockLength, blockLength);
			}
		}

		return output;
	}

	private void computeBlocks(Histogram[][] cells) {
		for (int y = 0; y < numBlocksY; y++) {
			for (int x = 0; x < numBlocksX; x++) {
				final double[] blockData = blocks[y][x].values;

				for (int j = 0, k = 0; j < cellsPerBlockY; j++) {
					for (int i = 0; i < cellsPerBlockX; i++) {
						final double[] cellData = cells[y * blockStepY + j][x * blockStepX + i].values;

						System.arraycopy(cellData, 0, blockData, k, cellData.length);

						k += cellData.length;
					}
				}
			}
		}
	}

	private void computeCells(WindowedHistogramExtractor binnedData, Rectangle region) {
		final int cellWidth = (int) (region.width / numCellsX);
		final int cellHeight = (int) (region.height / numCellsY);

		for (int j = 0, y = (int) region.y; j < numCellsY; j++, y += cellHeight) {
			for (int i = 0, x = (int) region.x; i < numCellsX; i++, x += cellWidth) {
				binnedData.computeHistogram(x, y, cellWidth, cellHeight, cells[j][i]);
				cells[j][i].normaliseL2();
			}
		}
	}

}
