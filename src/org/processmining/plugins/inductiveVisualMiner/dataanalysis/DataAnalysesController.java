package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer.DataRowBlockComputerRowBlock;

public class DataAnalysesController {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <C, P> void init(DataChain<C> chain, DataAnalysesView<C, P> view) {
		for (DataAnalysisTable<?, C, P> dataTable : view.getAnalyses()) {

			//register the blocks on the chain such that they will be triggered
			for (DataRowBlock<?, C, P> block : dataTable.getModel().getBlocks()) {
				chain.register(block);

				//for blocks that have a computer, also register the computer
				if (block instanceof DataRowBlockComputerRowBlock) {
					chain.register(((DataRowBlockComputerRowBlock) block).getComputer());
				}
			}
		}
	}
}
