package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableCellRenderer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

public class CohortAnalysisTable extends DataAnalysisTable {

	private static final long serialVersionUID = 5291003482927941387L;

	private AbstractTableModel model;
	private Cohorts cohorts;
	private boolean highlightInCohort = true;
	private CohortAnalysis2HighlightingFilterHandler cohortAnalysis2HighlightingFilterHandler;

	public CohortAnalysisTable() {
		//fill the table
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 6373287725995933319L;

			public String getColumnName(int column) {
				switch (column) {
					case 0 :
						return "Cohort attribute";
					case 1 :
						return "value range";
					case 2 :
						return "number of traces";
					default :
						return "distance with rest of log";
				}
			}

			public int getColumnCount() {
				if (cohorts == null) {
					return 0;
				}
				return 4;
			}

			public int getRowCount() {
				if (cohorts == null) {
					return 0;
				}
				return cohorts.size();
			}

			public Object getValueAt(int row, int column) {
				if (cohorts == null) {
					return "";
				}

				Cohort cohort = cohorts.get(row);

				switch (column) {
					case 0 :
						return cohort.getFeatures().iterator().next().getDescriptionField();
					case 1 :
						return DisplayType.html(cohort.getFeatures().iterator().next().getDescriptionSelector());
					case 2 :
						return DisplayType.numericUnpadded(cohort.getSize());
					default :
						return DisplayType.numeric(cohort.getDistance());
				}
			}
		};

		//enable and listen for selections, and pass the selection on to the trace attribute filter
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					selectionChanged();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				System.out.println("click");
				System.out.println(e.getModifiers());
				if (e.isShiftDown()) {
					System.out.println("shift");
					highlightInCohort = false;
					((DataAnalysisTableCellRenderer) getCellRenderer(0, 0)).setSelectedBackgroundColour(Color.red);
				} else {
					highlightInCohort = true;
					((DataAnalysisTableCellRenderer) getCellRenderer(0, 0)).setSelectedBackgroundColour(null);
				}
				repaint();

				//fire a selection event
				selectionChanged();
			}
		});

		setModel(model);

	}

	private void selectionChanged() {
		int selectedRow = getSelectedRow();
		if (selectedRow == -1) {
			cohortAnalysis2HighlightingFilterHandler.setSelectedCohort(null, true);
		} else {
			cohortAnalysis2HighlightingFilterHandler.setSelectedCohort(cohorts.get(selectedRow), highlightInCohort);
		}
	}

	public boolean setData(InductiveVisualMinerState state) {
		cohorts = state.getCohortAnalysis();
		model.fireTableStructureChanged();
		return cohorts != null;
	}

	public void invalidateData() {
		cohorts = null;
		model.fireTableStructureChanged();
	}

	public void setCohortAnalysis2HighlightingFilterHandler(
			CohortAnalysis2HighlightingFilterHandler cohortAnalysis2HighlightingFilterHandler) {
		this.cohortAnalysis2HighlightingFilterHandler = cohortAnalysis2HighlightingFilterHandler;
	}

}