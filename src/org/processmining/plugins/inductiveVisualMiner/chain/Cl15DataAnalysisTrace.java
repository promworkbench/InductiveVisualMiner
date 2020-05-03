package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class Cl15DataAnalysisTrace extends
		ChainLink<Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo>, TraceAttributeAnalysis> {

	public String getName() {
		return "data analysis - traces";
	}

	protected Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getModel(), state.getIvMLog(), (IvMLogFiltered) state.getIvMLogFiltered(),
				state.getAttributesInfo());
	}

	protected TraceAttributeAnalysis executeLink(
			Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> input, IvMCanceller canceller)
			throws Exception {
		return new TraceAttributeAnalysis(input.getA(), input.getB(), input.getC(), input.getD(), canceller);
	}

	protected void processResult(TraceAttributeAnalysis result, InductiveVisualMinerState state) {
		state.setTraceAttributesAnalysis(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setTraceAttributesAnalysis(null);
	}

	public String getStatusBusyMessage() {
		return "Performing data analysis..";
	}

}