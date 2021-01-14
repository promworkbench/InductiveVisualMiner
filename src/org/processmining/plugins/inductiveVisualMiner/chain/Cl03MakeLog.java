package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl03MakeLog implements DataChainLinkComputation {

	public String getName() {
		return "make log";
	}

	public String getStatusBusyMessage() {
		return "Making log..";
	}

	public IvMObject<?>[] getInputNames() {
		return new IvMObject<?>[] { IvMObject.sorted_log, IvMObject.selected_classifier, IvMObject.selected_miner };
	}

	public IvMObject<?>[] getOutputNames() {
		return new IvMObject<?>[] { IvMObject.imlog, IvMObject.imlog_info, IvMObject.xlog_info,
				IvMObject.xlog_info_performance };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		XLog log = inputs.get(IvMObject.sorted_log);
		XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(
				AttributeClassifiers.constructClassifier(inputs.get(IvMObject.selected_classifier)));
		IMLog2IMLogInfo miner = inputs.get(IvMObject.selected_miner).getLog2logInfo();
		XLifeCycleClassifier lifeCycleClassifier = inputs.get(IvMObject.selected_miner).getLifeCycleClassifier();

		IMLog imLog = new IMLogImpl(log, performanceClassifier.getActivityClassifier(), lifeCycleClassifier);
		IMLogInfo imLogInfo = miner.createLogInfo(imLog);
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log, performanceClassifier.getActivityClassifier());
		XLogInfo xLogInfoPerformance = XLogInfoFactory.createLogInfo(log, performanceClassifier);

		return new IvMObjectValues().//
				s(IvMObject.imlog, imLog).//
				s(IvMObject.imlog_info, imLogInfo).//
				s(IvMObject.xlog_info, xLogInfo).//
				s(IvMObject.xlog_info_performance, xLogInfoPerformance);
	}
}
