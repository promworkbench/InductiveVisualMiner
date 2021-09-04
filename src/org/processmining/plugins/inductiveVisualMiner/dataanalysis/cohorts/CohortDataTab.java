package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableCellRenderer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTableModel;

public class CohortDataTab<C, P> implements DataTab<Cohort, C, P> {
	public static final String name = "Cohort analysis";
	public static final String explanation = "Study influence of trace attributes on process behaviour.\n"
			+ "Click: highlight cohort;\t" + "shift+click: highlight other traces;\t"
			+ "ctrl+click: disable cohort highlighting.";

	@Override
	public DataTableCohort<C, P> createTable(DataAnalysesView<C, P> dataAnalysesView) {
		DataTableCohort<C, P> table = new DataTableCohort<C, P>(name, dataAnalysesView) {
			private static final long serialVersionUID = -6838046929095568195L;

			protected void setDefaultSorting(TableRowSorter<DataTableModel<Cohort, C, P>> sorter,
					List<SortKey> sortKeys) {
				if (getColumnCount() >= 4) {
					sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
					sorter.setSortable(1, false);
				}
			}
		};
		table.getModel().setColumnNames(new String[][] { {}, { "" }, { "", "" }, { "", "", "" },
				{ "Cohort attribute", "value range", "number of traces", "distance with rest of log" } });
		return table;
	}

	@Override
	public List<DataRowBlock<Cohort, C, P>> createRowBlocks(DataAnalysisTable<Cohort, C, P> table) {
		List<DataRowBlock<Cohort, C, P>> result = new ArrayList<>();
		result.add(new CohortRowBlock<C, P>(table));
		return result;
	}

	@Override
	public List<DataRowBlockComputer<Cohort, C, P>> createRowBlockComputers() {
		List<DataRowBlockComputer<Cohort, C, P>> result = new ArrayList<>();
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
		return true;
	}

	/**
	 * The cohort analysis tab needs to communicate selections to the
	 * controller. This subclass takes care of the handling.
	 * 
	 * @author sander
	 *
	 * @param <C>
	 * @param <P>
	 */
	public static class DataTableCohort<C, P> extends DataAnalysisTable<Cohort, C, P> {

		private static final long serialVersionUID = 3288401833656702004L;

		public DataTableCohort(String tabName, DataAnalysesView<C, P> dataAnalysesView) {
			super(tabName, dataAnalysesView);

			//enable and listen for selections, and pass the selection on to the trace attribute filter
			setColumnSelectionAllowed(false);
			setRowSelectionAllowed(true);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (EventQueue.getCurrentEvent() instanceof InputEvent) {
						if (((InputEvent) EventQueue.getCurrentEvent()).isShiftDown()) {
							highlightInCohort = false;
							((DataAnalysisTableCellRenderer) getCellRenderer(0, 0))
									.setSelectedBackgroundColour(Color.red);
						} else {
							highlightInCohort = true;
							((DataAnalysisTableCellRenderer) getCellRenderer(0, 0)).setSelectedBackgroundColour(null);
						}
						repaint();
					}
					if (!e.getValueIsAdjusting()) {
						selectionChanged();
					}
				}
			});
			/*
			 * The list selection listener leaves a gap: if the user
			 * shift-clicks an already selected row, this is not captured.
			 */
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					final int row = rowAtPoint(e.getPoint());
					if (row >= 0 && isRowSelected(row) && !isValueAdjusting) {
						if (e.isShiftDown()) {
							//System.out.println("mousePressed on selected row " + row);
							highlightInCohort = false;
							((DataAnalysisTableCellRenderer) getCellRenderer(0, 0))
									.setSelectedBackgroundColour(Color.red);
						} else {
							highlightInCohort = true;
							((DataAnalysisTableCellRenderer) getCellRenderer(0, 0)).setSelectedBackgroundColour(null);
						}
						selectionChanged();
						repaint();
					} else {
						//System.out.println("mousePressed on un-selected row " + row);
					}
				}
			});
		}

		private CohortAnalysis2HighlightingFilterHandler cohortAnalysis2HighlightingFilterHandler;
		private boolean highlightInCohort = true;
		private boolean isValueAdjusting = false;

		public void setCohortAnalysis2HighlightingFilterHandler(
				CohortAnalysis2HighlightingFilterHandler cohortAnalysis2HighlightingFilterHandler) {
			this.cohortAnalysis2HighlightingFilterHandler = cohortAnalysis2HighlightingFilterHandler;
		}

		@Override
		protected void setSorting() {
			super.setSorting();

			//we need to capture the isValueAdjusting of the selection-based events
			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					isValueAdjusting = e.getValueIsAdjusting();
				}
			});

		}

		private void selectionChanged() {
			int selectedRow = getSelectedRow();
			if (selectedRow == -1) {
				cohortAnalysis2HighlightingFilterHandler.setSelectedCohort(null, true);
				//System.out.println("set cohort none");
			} else {
				int selectedRowModel = convertRowIndexToModel(selectedRow);
				//System.out.println("set cohort " + selectedRowModel + " " + highlightInCohort);
				Cohort cohort = getModel().getRow(selectedRowModel).getPayload();
				cohortAnalysis2HighlightingFilterHandler.setSelectedCohort(cohort, highlightInCohort);
			}
		}
	}
}
