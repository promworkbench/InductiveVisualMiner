package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class TraceMiner extends VisualMinerWrapper {

	public String toString() {
		return "trace miner";
	}

	public ProcessTree mine(IMLog log, VisualMinerParameters parameters, IvMCanceller canceller) {
		return EfficientTree2processTree.convert(org.processmining.plugins.flowerMiner.TraceMiner.mineTraceModel(
				log.toXLog(), log.getClassifier()));
	}

}
