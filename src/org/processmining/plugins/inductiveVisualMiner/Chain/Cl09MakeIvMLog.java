package org.processmining.plugins.inductiveVisualMiner.Chain;

import org.deckfour.xes.info.XLogInfo;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeTimedLog;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;

public class Cl09MakeIvMLog extends ChainLink<Triple<AlignedLog, IMLog, XLogInfo>, IvMLog> {

	protected Triple<AlignedLog, IMLog, XLogInfo> generateInput(InductiveVisualMinerState state) {
		return Triple.of(state.getAlignedFilteredLog(), state.getAlignedFilteredXLog(), state.getXLogInfoPerformance());
	}

	protected IvMLog executeLink(Triple<AlignedLog, IMLog, XLogInfo> input) {
		return ComputeTimedLog.computeTimedLog(input.getA(), input.getB(), input.getC(), canceller);
	}

	protected void processResult(IvMLog result, InductiveVisualMinerState state) {
		state.setTimedLog(result);
	}

}
