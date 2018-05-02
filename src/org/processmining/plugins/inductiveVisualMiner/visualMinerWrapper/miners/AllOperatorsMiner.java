package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMfa;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class AllOperatorsMiner extends VisualMinerWrapper {

	public String toString() {
		return "all operators miner (IMfa)";
	}

	public IvMModel mine(IMLog log, IMLogInfo logInfo, VisualMinerParameters parameters, final IvMCanceller canceller) {

		//copy the relevant parameters
		MiningParameters miningParameters = new MiningParametersIMfa();
		miningParameters.setNoiseThreshold((float) (1 - parameters.getPaths()));

		ProcessTree tree = IMProcessTree.mineProcessTree(log, miningParameters, new Canceller() {
			public boolean isCancelled() {
				return canceller.isCancelled();
			}
		});

		if (tree != null) {
			return new IvMModel(ProcessTree2EfficientTree.convert(tree));
		} else {
			return null;
		}
	}

}
