package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl17DataAnalysisEvent extends DataChainLinkComputationAbstract {

	@Override
	public String getName() {
		return "data analysis - events";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Performing event analysis..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log, IvMObject.aligned_log_filtered,
				IvMObject.ivm_attributes_info };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_event };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);
		IvMLogFiltered aLogFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributesInfo = inputs.get(IvMObject.ivm_attributes_info);

		EventAttributeAnalysis result = new EventAttributeAnalysis(model, aLog, aLogFiltered, attributesInfo,
				canceller);

		return new IvMObjectValues().//
				s(IvMObject.data_analysis_event, result);
	}

}