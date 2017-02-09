package org.processmining.plugins.inductiveVisualMiner.chain;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;

public class Cl10TraceColouring extends ChainLink<Pair<IvMLogNotFiltered, TraceColourMapSettings>, TraceColourMap> {

	protected Pair<IvMLogNotFiltered, TraceColourMapSettings> generateInput(InductiveVisualMinerState state) {
		return Pair.of(state.getIvMLog(), state.getTraceColourMapSettings());
	}

	protected TraceColourMap executeLink(Pair<IvMLogNotFiltered, TraceColourMapSettings> input, IvMCanceller canceller)
			throws Exception {
		TraceColourMapSettings settings = input.getRight();
		if (settings == null) {
			return new TraceColourMapFixed(TraceColourMapSettings.defaultColour);
		} else {
			return settings.getTraceColourMap(input.getLeft());
		}
	}

	protected void processResult(TraceColourMap result, InductiveVisualMinerState state) {
		state.setTraceColourMap(result);
	}

}
