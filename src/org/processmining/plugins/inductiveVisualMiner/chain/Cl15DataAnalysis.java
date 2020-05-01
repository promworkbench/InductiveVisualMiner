package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class Cl15DataAnalysis extends
		ChainLink<Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo>, Pair<EventAttributeAnalysis, TraceAttributeAnalysis>> {

	public String getName() {
		return "data analysis";
	}

	protected Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getModel(), state.getIvMLog(), (IvMLogFiltered) state.getIvMLogFiltered(),
				state.getAttributesInfo());
	}

	protected Pair<EventAttributeAnalysis, TraceAttributeAnalysis> executeLink(
			Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, AttributesInfo> input, IvMCanceller canceller)
			throws Exception {
		TraceAttributeAnalysis t = new TraceAttributeAnalysis(input.getA(), input.getB(), input.getC(), input.getD(),
				canceller);
		EventAttributeAnalysis e = new EventAttributeAnalysis(input.getA(), input.getB(), input.getC(), input.getD(),
				canceller);
		return Pair.of(e, t);
	}

	protected void processResult(Pair<EventAttributeAnalysis, TraceAttributeAnalysis> result,
			InductiveVisualMinerState state) {
		state.setEventAttributesAnalysis(result.getA());
		state.setTraceAttributesAnalysis(result.getB());
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setTraceAttributesAnalysis(null);
	}

	public String getStatusBusyMessage() {
		return "Performing data analysis..";
	}

}