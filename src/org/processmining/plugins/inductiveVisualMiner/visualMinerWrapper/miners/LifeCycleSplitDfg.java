package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class LifeCycleSplitDfg extends VisualMinerWrapper {
	
	public String toString() {
		return "lifecycle; split dfg";
	}

	public ProcessTree mine(IMLog log, VisualMinerParameters parameters, Canceller canceller) {
		
		//copy the relevant parameters
		MiningParameters miningParameters = new MiningParametersIvM();
		miningParameters.setNoiseThreshold((float) (1 - parameters.getPaths()));
		miningParameters.setLogConverter(new IMLog2IMLogInfoLifeCycle());
		
		return IMProcessTree.mineProcessTree(log, miningParameters);
	}

}
