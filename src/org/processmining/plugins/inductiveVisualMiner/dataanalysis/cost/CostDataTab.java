package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;

public class CostDataTab<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Cost model";
	public static final String explanation = "The parameters of the discovered cost model.\nIf traces are highlighted, two cost models will be computed: for highlighted and not-highlighted traces.\nPress ctrl+T to change the cost model (e.g. to include time).";

	public CostDataTab(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataTableCost<>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "cost" },
				{ "", "", "cost highlighted traces", "cost not-highlighted traces" } });

		return table;
	}

	@Override
	public String getAnalysisName() {
		return name;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}

	public static class DataTableCost<C, P> extends DataAnalysisTable<Object, C, P> {

		private static final long serialVersionUID = 8503524403860837088L;

		private Runnable onChangeCostModel;

		public DataTableCost(String tabName, DataAnalysesView<C, P> dataAnalysesView) {
			super(tabName, dataAnalysesView);

			getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl T"),
					"toggleCostModel");
			getActionMap().put("toggleCostModel", new AbstractAction() {
				private static final long serialVersionUID = -8983001960412000793L;

				public void actionPerformed(ActionEvent e) {
					if (onChangeCostModel != null) {
						onChangeCostModel.run();
					}
				}
			});
		}

		public Runnable getOnChangeCostModel() {
			return onChangeCostModel;
		}

		public void setOnChangeCostModel(Runnable onChangeCostModel) {
			this.onChangeCostModel = onChangeCostModel;
		}

	}
}
