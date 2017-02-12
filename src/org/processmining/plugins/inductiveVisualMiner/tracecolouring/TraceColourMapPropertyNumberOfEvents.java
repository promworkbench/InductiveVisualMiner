package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapPropertyNumberOfEvents extends TraceColourMapProperty {

	public TraceColourMapPropertyNumberOfEvents(IvMEfficientTree tree, IvMLogNotFiltered log, Color[] colours,
			double min, double max) {
		super(tree, log, colours, min, max);
	}

	protected double getProperty(IvMTrace trace) {
		return trace.getNumberOfEvents();
	}

	protected double getProperty(IMTrace trace) {
		return trace.size();
	}
}
