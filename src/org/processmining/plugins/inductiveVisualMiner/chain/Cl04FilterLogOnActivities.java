package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;

public class Cl04FilterLogOnActivities extends
		ChainLink<Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, IvMFiltersController>, Triple<IMLog, IMLogInfo, Set<XEventClass>>> {

	protected Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, IvMFiltersController> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getLog(), state.getLogInfo(), state.getActivitiesThreshold(), state.getLog2logInfo(),
				state.getFiltersController());
	}

	protected Triple<IMLog, IMLogInfo, Set<XEventClass>> executeLink(
			Quintuple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo, IvMFiltersController> input, IvMCanceller canceller) {
		if (input.getE().isAPreMiningFilterEnabled() || input.getC() < 1.0) {
			IMLog newLog = input.getA().clone();
			Set<XEventClass> removedActivities = new HashSet<>();

			//apply activities slider
			if (input.getC() < 1.0) {
				removedActivities = FilterLeastOccurringActivities.filter(newLog, input.getB(), input.getC(),
						input.getD());
			}

			//apply pre-mining filters
			input.getE().applyPreMiningFilters(newLog, canceller);

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

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setActivityFilteredIMLog(null, null, null);
	}

}
