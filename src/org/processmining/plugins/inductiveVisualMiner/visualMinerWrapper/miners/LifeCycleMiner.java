package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.Log2DfgLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class LifeCycleMiner extends VisualMinerWrapper {
	
	public String toString() {
		return "Lifecycle miner";
	}

	public ProcessTree mine(IMLog2 log, VisualMinerParameters parameters, Canceller canceller) {
		
		//copy the relevant parameters
		DfgMiningParameters miningParameters = new DfgMiningParametersIvMLifecycle();
		miningParameters.setNoiseThreshold((float) (1 - parameters.getPaths()));
		
		Dfg dfg = Log2DfgLifeCycle.log2Dfg(log);
		dfg.collapseParallelIntoDirectly();
		return IMdProcessTree.mineProcessTree(dfg, miningParameters);
	}

}
