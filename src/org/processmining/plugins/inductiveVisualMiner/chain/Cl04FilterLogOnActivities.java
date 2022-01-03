package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTree;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;

public class Cl04FilterLogOnActivities extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	public String getName() {
		return "filter log";
	}

	public String getStatusBusyMessage() {
		return "Filtering activities..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.imlog, IvMObject.imlog_info, IvMObject.selected_activities_threshold,
				IvMObject.selected_miner, IvMObject.pre_mining_filter_tree_event,
				IvMObject.pre_mining_filter_tree_trace };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.imlog_activity_filtered, IvMObject.imlog_info_activity_filtered,
				IvMObject.filtered_activities };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {

		IMLog imLog = inputs.get(IvMObject.imlog);
		IMLogInfo imLogInfo = inputs.get(IvMObject.imlog_info);
		double activitiesThreshold = inputs.get(IvMObject.selected_activities_threshold);
		IMLog2IMLogInfo log2logInfo = inputs.get(IvMObject.selected_miner).getLog2logInfo();
		@SuppressWarnings("unchecked")
		IvMFilterTree<XEvent> filterEvent = inputs.get(IvMObject.pre_mining_filter_tree_event);
		@SuppressWarnings("unchecked")
		IvMFilterTree<IMTrace> filterTrace = inputs.get(IvMObject.pre_mining_filter_tree_trace);

		if (filterEvent.couldSomethingBeFiltered() || filterTrace.couldSomethingBeFiltered()
				|| activitiesThreshold < 1.0) {
			IMLog newLog = imLog.clone();
			Set<XEventClass> removedActivities = new HashSet<>();

			//apply activities slider
			if (activitiesThreshold < 1.0) {
				removedActivities = FilterLeastOccurringActivities.filter(newLog, imLogInfo, activitiesThreshold,
						log2logInfo);
			}

			//apply pre-mining filters
			filterTrace.filter(newLog.iterator(), canceller);

			if (filterEvent.couldSomethingBeFiltered()) {
				for (IMTrace trace : newLog) {
					filterEvent.filter(trace.iterator(), canceller);
				}
			}

			IMLogInfo filteredLogInfo = log2logInfo.createLogInfo(newLog);

			return new IvMObjectValues().// 
					s(IvMObject.imlog_activity_filtered, newLog).// 
					s(IvMObject.imlog_info_activity_filtered, filteredLogInfo).//
					s(IvMObject.filtered_activities, removedActivities);
		} else {
			return new IvMObjectValues().// 
					s(IvMObject.imlog_activity_filtered, imLog).// 
					s(IvMObject.imlog_info_activity_filtered, imLogInfo).//
					s(IvMObject.filtered_activities, new HashSet<XEventClass>());
		}
	}
}