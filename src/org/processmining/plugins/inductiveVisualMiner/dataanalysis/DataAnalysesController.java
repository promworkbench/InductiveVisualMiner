package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer.DataRowBlockComputerRowBlock;

public class DataAnalysesController {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <C, P> void init(final DataChain<C> chain, final DataAnalysesView<C, P> view) {

		//register the tables
		for (DataAnalysisTable<?, C, P> dataTable : view.getAnalyses()) {

			final String tabName = dataTable.getModel().getTabName();
			final DataAnalysisTable<?, C, P> dataTable2 = dataTable;

			//register the blocks on the chain such that they will be triggered
			for (DataRowBlock<?, C, P> block : dataTable.getModel().getBlocks()) {
				chain.register(block);

				//for blocks that have a computer, also register the computer
				if (block instanceof DataRowBlockComputerRowBlock) {
					chain.register(((DataRowBlockComputerRowBlock) block).getComputer());
				}
			}

			//register handlers for switchers
			if (dataTable.isSwitchable() != null) {
				chain.setObject(dataTable.isSwitchable(), false);
				view.addSwitcherListener(tabName, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
						if (selected) {
							//start the computation
							chain.setObject(dataTable2.isSwitchable(), true);
							view.setSwitcherMessage(tabName, "Compute " + tabName + " [computing..]");
						} else {
							//stop the computation
							/*
							 * It seems counter-intuitive, but we already have
							 * means in place to stop running computations. That
							 * is, if we start a new one [which will not compute
							 * anything due the flag set], the old one will be
							 * stopped automatically.
							 */
							chain.setObject(dataTable2.isSwitchable(), false);
							view.setSwitcherMessage(tabName, "Compute " + tabName);
						}
					}
				});
			}
		}
	}
}
