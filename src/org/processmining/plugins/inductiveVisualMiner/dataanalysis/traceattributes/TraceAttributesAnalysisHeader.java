package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;

public class TraceAttributesAnalysisHeader extends IvMPanel {

	private static final long serialVersionUID = -962748281721857167L;
	private PieChart pieChart = new PieChart();

	public TraceAttributesAnalysisHeader() {
		//add(new JLabel("hoi"));
		add(pieChart);
	}

	public PieChart getPieChart() {
		return pieChart;
	}

}
