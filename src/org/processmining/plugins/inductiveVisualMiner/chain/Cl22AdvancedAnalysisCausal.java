package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.causal.CausalAnalysis;
import org.processmining.plugins.inductiveVisualMiner.causal.CausalAnalysisResult;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl22AdvancedAnalysisCausal<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl22 causal";
	}

	public String getStatusBusyMessage() {
		return "Computing causal relations..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.data_analyses_delay,
				IvMObject.selected_causal_enabled };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_causal };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		if (inputs.get(IvMObject.selected_causal_enabled)) {
			IvMModel model = inputs.get(IvMObject.model);
			IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);

			CausalAnalysisResult result = CausalAnalysis.analyse(model, logFiltered, canceller);
			
			System.out.println(result);

			return new IvMObjectValues().//
					s(IvMObject.data_analysis_causal, result);

		} else {
			return null;
		}
	}
}