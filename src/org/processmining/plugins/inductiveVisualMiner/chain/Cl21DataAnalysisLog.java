package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysis;

public class Cl21DataAnalysisLog implements DataChainLinkComputation {

	public String getName() {
		return "data analysis log";
	}

	public String getStatusBusyMessage() {
		return "Performing log analysis..";
	}

	public IvMObject<?>[] getInputNames() {
		return new IvMObject<?>[] { IvMObject.input_log };
	}

	public IvMObject<?>[] getOutputNames() {
		return new IvMObject<?>[] { IvMObject.data_analysis_log };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		XLog log = inputs.get(IvMObject.input_log);

		LogAttributeAnalysis logAttributeAnalysis = new LogAttributeAnalysis(log, canceller);
		return new IvMObjectValues().//
				s(IvMObject.data_analysis_log, logAttributeAnalysis);
	}

}
