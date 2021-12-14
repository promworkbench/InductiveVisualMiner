package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTabAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableModel;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class DataAnalysisTabCausal<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Causal";
	public static final String explanation = "Study influence of decisions in the model on one another.";

	public DataAnalysisTabCausal(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView, IvMDecoratorI decorator) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<Object, C, P>(name, dataAnalysesView, decorator) {
			private static final long serialVersionUID = -8536485501677939027L;

			@Override
			protected void setDefaultSorting(TableRowSorter<DataAnalysisTableModel<Object, C, P>> sorter,
					List<SortKey> sortKeys) {
				if (getColumnCount() >= 3) {
					sortKeys.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
				}
			}

			@Override
			public IvMObject<Boolean> isSwitchable() {
				return IvMObject.selected_causal_enabled;
			}
		};
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" },
				{ "From choice", "to choice", "causal dependency" }, { "", "", "", "" } });
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

}
