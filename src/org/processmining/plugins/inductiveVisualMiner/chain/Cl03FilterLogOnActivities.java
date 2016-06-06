package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;

public class Cl03FilterLogOnActivities
		extends
		ChainLink<Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, List<IvMFilter>>, Triple<IMLog, IMLogInfo, Set<XEventClass>>> {

	protected Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, List<IvMFilter>> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getLog(), state.getLogInfo(), state.getActivitiesThreshold(), state.getLog2logInfo(),
				state.getPreMiningFilters());
	}

	protected Triple<IMLog, IMLogInfo, Set<XEventClass>> executeLink(
			Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, List<IvMFilter>> input, IvMCanceller canceller) {
		if (isFilterEnabled(input.getE()) || input.getC() < 1.0) {
			IMLog newLog = input.getA().clone();
			Set<XEventClass> removedActivities = new HashSet<>();

			//apply activities slider
			if (input.getC() < 1.0) {
				removedActivities = FilterLeastOccurringActivities.filter(newLog, input.getB(), input.getC(),
						input.getD());
			}

			//apply pre-mining filters
			applyPreMiningFilter(newLog, input.getE(), canceller);

			IMLogInfo filteredLogInfo = input.getD().createLogInfo(newLog);
			return Triple.of(newLog, filteredLogInfo, removedActivities);
		} else {
			return new Triple<IMLog, IMLogInfo, Set<XEventClass>>(input.getA(), input.getB(),
					new HashSet<XEventClass>());
		}
	}

	protected void processResult(Triple<IMLog, IMLogInfo, Set<XEventClass>> result, InductiveVisualMinerState state) {
		state.setActivityFilteredIMLog(result.getA(), result.getB(), result.getC());
	}

	public static boolean isFilterEnabled(List<IvMFilter> filters) {
		for (IvMFilter filter : filters) {
			if (filter instanceof PreMiningTraceFilter && filter.isEnabledFilter()) {
				return true;
			}
			if (filter instanceof PreMiningEventFilter && filter.isEnabledFilter()) {
				return true;
			}
		}
		return false;
	}

	public static void applyPreMiningFilter(IMLog log, List<IvMFilter> filters, final IvMCanceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<PreMiningTraceFilter> enabledTraceFilters = new ArrayList<>();
		List<PreMiningEventFilter> enabledEventFilters = new ArrayList<>();
		for (IvMFilter filter : filters) {
			if (filter instanceof PreMiningTraceFilter && filter.isEnabledFilter()) {
				enabledTraceFilters.add((PreMiningTraceFilter) filter);
			}
			if (filter instanceof PreMiningEventFilter && filter.isEnabledFilter()) {
				enabledEventFilters.add((PreMiningEventFilter) filter);
			}
		}
		if (enabledTraceFilters.isEmpty() && enabledEventFilters.isEmpty()) {
			//no filter is enabled, just return
			return;
		}

		for (Iterator<IMTrace> it = log.iterator(); it.hasNext();) {
			IMTrace trace = it.next();

			//feed this trace to each enabled trace filter
			boolean removed = false;
			for (PreMiningTraceFilter filter : enabledTraceFilters) {
				if (!filter.staysInLog(trace)) {
					it.remove();
					removed = true;
					break;
				}
			}

			if (!removed) {
				for (Iterator<XEvent> it1 = trace.iterator(); it1.hasNext();) {
					XEvent event = it1.next();

					//feed this trace to each enabled event filter
					for (PreMiningEventFilter filter : enabledEventFilters) {
						if (!filter.staysInLog(event)) {
							it1.remove();
							break;
						}
					}

					if (canceller.isCancelled()) {
						return;
					}
				}
			}

			if (canceller.isCancelled()) {
				return;
			}
		}

		return;
	}
}
