package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class TraceColourMapPropertyDuration extends TraceColourMapProperty {

	public TraceColourMapPropertyDuration(IvMModel model, IvMLogNotFiltered log, ColourMap colourMap, double min,
			double max) {
		super(model, log, colourMap, min, max);
	}

	protected double getProperty(IvMTrace trace) {
		return getTraceDuration(trace);
	}

	public static long getTraceDuration(IvMTrace trace) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
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

	public static long getTraceDuration(IMTrace trace) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		for (XEvent event : trace) {
			if (ResourceTimeUtils.getTimestamp(event) != null) {
				min = Math.min(min, ResourceTimeUtils.getTimestamp(event));
				max = Math.max(max, ResourceTimeUtils.getTimestamp(event));
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
