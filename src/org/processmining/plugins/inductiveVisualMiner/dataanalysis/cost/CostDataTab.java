package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;

public class CostDataTab<C, P> implements DataTab<Object, C, P> {

	public static final String name = "Cost model";
	public static final String explanation = "The parameters of the discovered cost model.\nIf traces are highlighted, two cost models will be computed: for highlighted and not-highlighted traces.\nPress ctrl+T to change the cost model (e.g. to include time).";

	@Override
	public DataTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataTable<Object, C, P> table = new DataTableCost<>(name, dataAnalysesView);
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "cost" },
				{ "", "", "cost highlighted traces", "cost not-highlighted traces" } });

		return table;
	}

	@Override
	public List<DataRowBlock<Object, C, P>> createRowBlocks(DataTable<Object, C, P> table) {
		List<DataRowBlock<Object, C, P>> result = new ArrayList<>();
		result.add(new CostRowBlock<C, P>(table));
		return result;
	}

	@Override
	public List<DataRowBlockComputer<Object, C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<Object, C, P>> result = new ArrayList<>();
		return result;
	}

	@Override
	public String getAnalysisName() {
		return name;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}

	@Override
	public boolean isSwitchable() {
		return false;
	}

	public static class DataTableCost<C, P> extends DataTable<Object, C, P> {

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
