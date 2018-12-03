package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMflc;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

public class DfgMiner extends VisualMinerWrapper {
//public class DfgMiner {

	public String toString() {
		return "directly-follows miner";
	}

	public IvMModel mine(IMLog log, IMLogInfo logInfo, VisualMinerParameters parameters, final IvMCanceller canceller) {
		return new IvMModel(logInfo.getDfg());
	}

	public XLifeCycleClassifier getLifeCycleClassifier() {
		return new MiningParametersIMflc().getLifeCycleClassifier();
	}
	
	public IMLog2IMLogInfo getLog2logInfo() {
		return new IMLog2IMLogInfoLifeCycle();
	}
}
