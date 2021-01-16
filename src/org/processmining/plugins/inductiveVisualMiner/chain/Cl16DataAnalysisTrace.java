package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl16DataAnalysisTrace implements DataChainLinkComputation {

	@Override
	public String getName() {
		return "data analysis - traces";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Performing trace analysis..";
	}

	@Override
	public IvMObject<?>[] getInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log, IvMObject.aligned_log_filtered,
				IvMObject.ivm_attributes_info };
	}

	@Override
	public IvMObject<?>[] getOutputNames() {
		return new IvMObject<?>[] { IvMObject.data_analysis_trace };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);
		IvMLogFiltered aLogFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributesInfo = inputs.get(IvMObject.ivm_attributes_info);

		TraceAttributeAnalysis result = new TraceAttributeAnalysis(model, aLog, aLogFiltered, attributesInfo,
				canceller);

		return new IvMObjectValues().//
				s(IvMObject.data_analysis_trace, result);
	}

}