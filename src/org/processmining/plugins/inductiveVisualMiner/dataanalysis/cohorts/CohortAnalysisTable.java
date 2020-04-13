package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import javax.swing.table.AbstractTableModel;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;

public class CohortAnalysisTable extends DataAnalysisTable<Cohorts> {

	private static final long serialVersionUID = 5291003482927941387L;

	private AbstractTableModel model;
	private Cohorts cohorts;

	public CohortAnalysisTable() {
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 6373287725995933319L;

			public String getColumnName(int column) {
				switch (column) {
					case 0 :
						return "Cohort attribute";
					case 1 :
						return "Cohort value range";
					case 2 :
						return "Number of traces";
					default :
						return "Distance with rest of the log";
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
						return "<html>" + cohort.getFeatures().iterator().next().getDescriptionSelector() + "</html>";
					case 2 :
						return DisplayType.numericUnpadded(cohort.getSize());
					default :
						return DisplayType.numeric(cohort.getDistance());
				}
			}
		};
		setModel(model);
	}

	public void setAttributesInfo(AttributesInfo attributesInfo) {

	}

	public void setData(Cohorts cohorts) {
		this.cohorts = cohorts;
		model.fireTableStructureChanged();
	}

}