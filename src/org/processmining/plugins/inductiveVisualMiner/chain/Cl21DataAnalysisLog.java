package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysis;

public class Cl21DataAnalysisLog extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	@Override
	public String getName() {
		return "data analysis log";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Performing log analysis..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.input_log };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		XLog log = inputs.get(IvMObject.input_log);

		LogAttributeAnalysis logAttributeAnalysis = new LogAttributeAnalysis(log, canceller);
		return new IvMObjectValues().//
				s(IvMObject.data_analysis_log, logAttributeAnalysis);
	}

}
