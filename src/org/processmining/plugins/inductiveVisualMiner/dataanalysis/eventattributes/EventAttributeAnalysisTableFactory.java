package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

public class EventAttributeAnalysisTableFactory implements DataAnalysisTableFactory<EventAttributeAnalysis> {

	public static final String name = "Event attributes";
	public static final String explanation = "Attributes at the event level.\nIf traces are highlighted, attributes will be shown for highlighted and non-highlighted traces.";

	public DataAnalysisTable<EventAttributeAnalysis> create() {
		return new EventAttributeAnalysisTable();
	}

	public String getAnalysisName() {
		return name;
	}

	public String getExplanation() {
		return explanation;
	}

}
