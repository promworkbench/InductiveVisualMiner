package org.processmining.plugins.inductiveVisualMiner.chain;

import java.awt.Color;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMapAttribute;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl10TraceColouring extends ChainLink<Pair<IvMLogNotFiltered, TraceColourMapSettings>, TraceColourMap> {

	protected Pair<IvMLogNotFiltered, TraceColourMapSettings> generateInput(InductiveVisualMinerState state) {
		return Pair.of(state.getIvMLog(), state.getTraceColourMapSettings());
	}

	protected TraceColourMap executeLink(Pair<IvMLogNotFiltered, TraceColourMapSettings> input, IvMCanceller canceller)
			throws Exception {
		TraceColourMapSettings settings = input.getRight();
		if (settings == null || settings.numberOfColours < 1 || settings.attribute == null) {
			return new TraceColourMapFixed(Color.yellow);
		} else {
			return new TraceColourMapAttribute(input.getLeft(), settings);
		}
	}

	protected void processResult(TraceColourMap result, InductiveVisualMinerState state) {
		state.setTraceColourMap(result);
	}

}
