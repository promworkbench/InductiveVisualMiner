package org.processmining.plugins.inductiveVisualMiner.chain2;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;

public class Cl11TraceColouring extends ChainLink2<Triple<IvMEfficientTree, IvMLogNotFiltered, TraceColourMapSettings>, TraceColourMap> {

	protected Triple<IvMEfficientTree, IvMLogNotFiltered, TraceColourMapSettings> generateInput(
			InductiveVisualMinerState state) {
		return Triple.of(state.getTree(), state.getIvMLog(), state.getTraceColourMapSettings());
	}

	protected TraceColourMap executeLink(Triple<IvMEfficientTree, IvMLogNotFiltered, TraceColourMapSettings> input,
			IvMCanceller canceller) throws Exception {
		TraceColourMapSettings settings = input.getC();
		if (settings == null) {
			return new TraceColourMapFixed(TraceColourMapSettings.defaultColour);
		} else {
			return settings.getTraceColourMap(input.getA(), input.getB());
		}
	}

	protected void processResult(TraceColourMap result, InductiveVisualMinerState state) {
		state.setTraceColourMap(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setTraceColourMap(new TraceColourMapFixed(TraceColourMapSettings.defaultColour));
	}

}
