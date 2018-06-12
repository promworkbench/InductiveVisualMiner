package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;

//public class DfgMiner extends VisualMinerWrapper {
public class DfgMiner {

	public String toString() {
		return "directly-follows miner";
	}

	public IvMModel mine(IMLog log, IMLogInfo logInfo, VisualMinerParameters parameters, final IvMCanceller canceller) {
		return new IvMModel(logInfo.getDfg());
	}
}
