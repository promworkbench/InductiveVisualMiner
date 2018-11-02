package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapPropertyNumberOfEvents extends TraceColourMapProperty {

	public TraceColourMapPropertyNumberOfEvents(IvMModel tree, IvMLogNotFiltered log, ColourMap colourMap,
			double min, double max) {
		super(tree, log, colourMap, min, max);
	}

	protected double getProperty(IvMTrace trace) {
		return trace.getNumberOfEvents();
	}

	protected double getProperty(IMTrace trace) {
		return trace.size();
	}
}
