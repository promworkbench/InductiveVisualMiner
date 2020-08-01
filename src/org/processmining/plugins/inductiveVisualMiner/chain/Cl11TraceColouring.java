package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;

public class Cl11TraceColouring
		extends IvMChainLink<Triple<IvMModel, IvMLogNotFiltered, TraceColourMapSettings>, TraceColourMap> {

	protected Triple<IvMModel, IvMLogNotFiltered, TraceColourMapSettings> generateInput(
			InductiveVisualMinerState state) {
		return Triple.of(state.getModel(), state.getIvMLog(), state.getTraceColourMapSettings());
	}

	protected TraceColourMap executeLink(Triple<IvMModel, IvMLogNotFiltered, TraceColourMapSettings> input,
			IvMCanceller canceller) throws Exception {
		TraceColourMapSettings settings = input.getC();
		if (settings == null) {
			return new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
		} else {
			return settings.getTraceColourMap(input.getA(), input.getB());
		}
	}

	protected void processResult(TraceColourMap result, InductiveVisualMinerState state) {
		state.setTraceColourMap(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setTraceColourMap(new TraceColourMapFixed(RendererFactory.defaultTokenFillColour));
	}

	public String getName() {
		return "colour traces";
	}

	public String getStatusBusyMessage() {
		return "Colouring traces..";
	}
}
