package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapPropertyDuration extends TraceColourMapProperty {

	public TraceColourMapPropertyDuration(IvMEfficientTree tree, IvMLogNotFiltered log, Color[] colours,
			double min, double max) {
		super(tree, log, colours, min, max);
	}

	protected double getProperty(IvMTrace trace) {
		double min = Long.MAX_VALUE;
		double max = Long.MIN_VALUE;
		for (IvMMove move : trace) {
			if (move.getLogTimestamp() != null) {
				min = Math.min(min, move.getLogTimestamp());
				max = Math.max(max, move.getLogTimestamp());
			}
		}
		if (max == Long.MIN_VALUE) {
			return max;
		}
		return max - min;
	}

	protected double getProperty(IMTrace trace) {
		return 0;
	}
	
	@Override
	public String getValue(IvMTrace trace) {
		if (getProperty(trace) > Double.MIN_VALUE) {
			return "\u2588 " + ResourceTimeUtils.getDuration(getProperty(trace));
		} else {
			return "";
		}
	}

}
