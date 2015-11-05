package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;

public class Cl02FilterLogOnActivities extends
		ChainLink<Quadruple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo>, Triple<IMLog, IMLogInfo, Set<XEventClass>>> {

	public Cl02FilterLogOnActivities(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Quadruple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo> generateInput(InductiveVisualMinerState state) {
		return new Quadruple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo>(state.getLog(), state.getLogInfo(),
				state.getActivitiesThreshold(), state.getLog2logInfo());
	}

	protected Triple<IMLog, IMLogInfo, Set<XEventClass>> executeLink(
			Quadruple<IMLog, IMLogInfo, Double, IMLog2IMLogInfo> input) {
		if (input.getC() < 1.0) {
			return FilterLeastOccurringActivities.filter(input.getA(), input.getB(), input.getC(), input.getD());
		} else {
			return new Triple<IMLog, IMLogInfo, Set<XEventClass>>(input.getA(), input.getB(),
					new HashSet<XEventClass>());
		}
	}

	protected void processResult(Triple<IMLog, IMLogInfo, Set<XEventClass>> result, InductiveVisualMinerState state) {
		state.setActivityFilteredIMLog(result.getA(), result.getB(), result.getC());
	}

}
