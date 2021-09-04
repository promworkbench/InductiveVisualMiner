package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class DataAnalysisTabAbstract<O, C, P> implements DataAnalysisTab<O, C, P> {

	private Callable<List<DataRowBlock<O, C, P>>> rowBlocksCreator;
	private Callable<List<DataRowBlockComputer<O, C, P>>> rowBlockComputersCreator;

	public DataAnalysisTabAbstract(Callable<List<DataRowBlock<O, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<O, C, P>>> rowBlockComputersCreator) {
		this.rowBlocksCreator = rowBlocksCreator;
		this.rowBlockComputersCreator = rowBlockComputersCreator;
	}

	@Override
	public List<DataRowBlock<O, C, P>> createRowBlocks(DataAnalysisTable<O, C, P> table) {
		try {
			List<DataRowBlock<O, C, P>> result = rowBlocksCreator.call();
			for (DataRowBlock<O, C, P> rowBlock : result) {
				rowBlock.setTable(table);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<DataRowBlockComputer<O, C, P>> createRowBlockComputers() {
		try {
			return rowBlockComputersCreator.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
