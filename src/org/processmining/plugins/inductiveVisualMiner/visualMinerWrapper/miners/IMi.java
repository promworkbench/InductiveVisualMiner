package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class IMi extends VisualMinerWrapper {

	public String toString() {
		return "Log";
	}

	public ProcessTree mine(IMLog log, VisualMinerParameters parameters, Canceller canceller) {
		
		//copy the relevant parameters
		MiningParameters miningParameters = new MiningParametersIvM();
		miningParameters.setNoiseThreshold((float) (1 - parameters.getPaths()));
		
		return IMProcessTree.mineProcessTree(log, miningParameters);
	}
	
}