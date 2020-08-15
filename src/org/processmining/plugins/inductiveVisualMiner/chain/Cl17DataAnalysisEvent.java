package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl17DataAnalysisEvent extends
		IvMChainLink<Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, IvMAttributesInfo>, EventAttributeAnalysis> {

	public String getName() {
		return "data analysis - events";
	}

	protected Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, IvMAttributesInfo> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getModel(), state.getIvMLog(), (IvMLogFiltered) state.getIvMLogFiltered(),
				state.getAttributesInfoIvM());
	}

	protected EventAttributeAnalysis executeLink(
			Quadruple<IvMModel, IvMLogNotFiltered, IvMLogFiltered, IvMAttributesInfo> input, IvMCanceller canceller)
			throws Exception {
		return new EventAttributeAnalysis(input.getA(), input.getB(), input.getC(), input.getD(), canceller);
	}

	protected void processResult(EventAttributeAnalysis result, InductiveVisualMinerState state) {
		state.setEventAttributesAnalysis(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setTraceAttributesAnalysis(null);
	}

	public String getStatusBusyMessage() {
		return "Performing event analysis..";
	}

}