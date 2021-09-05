package org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations;

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

public class DataAnalysisTabAssociations<C, P> extends DataAnalysisTabAbstract<Object, C, P> {

	public static final String name = "Associations";
	public static final String explanation = "Study influence of trace attributes on process behaviour.";

	public DataAnalysisTabAssociations(Callable<List<DataRowBlock<Object, C, P>>> rowBlocksCreator,
			Callable<List<DataRowBlockComputer<Object, C, P>>> rowBlockComputersCreator) {
		super(rowBlocksCreator, rowBlockComputersCreator);
	}

	@Override
	public DataAnalysisTable<Object, C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataAnalysisTable<Object, C, P> table = new DataAnalysisTable<Object, C, P>(name, dataAnalysesView) {

			private static final long serialVersionUID = 7015864214763696785L;

			@Override
			protected void setDefaultSorting(TableRowSorter<DataAnalysisTableModel<Object, C, P>> sorter,
					List<SortKey> sortKeys) {
				if (getColumnCount() >= 4) {
					sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
				}
			}

			@Override
			public IvMObject<Boolean> isSwitchable() {
				return IvMObject.selected_associations_enabled;
			}
		};
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "" },
				{ "Trace attribute", "attribute type", "association with process", "distance plot" } });
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