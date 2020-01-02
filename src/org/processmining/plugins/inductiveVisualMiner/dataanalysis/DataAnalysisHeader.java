package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;

public class DataAnalysisHeader extends IvMPanel {

	private PieChart pieChart = new PieChart();

	public DataAnalysisHeader() {
		//add(new JLabel("hoi"));
		add(pieChart);
	}

	public PieChart getPieChart() {
		return pieChart;
	}
	
	
	
	
}
