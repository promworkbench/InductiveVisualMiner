package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import org.processmining.directlyfollowsmodelminer.mining.DFMMiner;
import org.processmining.directlyfollowsmodelminer.mining.DFMMiningParametersAbstract;
import org.processmining.directlyfollowsmodelminer.mining.variants.DFMMiningParametersDefault;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveminer2.logs.IMLogImpl;

public class DfgMiner extends VisualMinerWrapper {
	//public class DfgMiner {

	public String toString() {
		return "directly-follows miner";
	}

	public IvMModel mine(IMLog log, IMLogInfo logInfo, VisualMinerParameters parameters, final IvMCanceller canceller) {
		//copy the relevant parameters
		DFMMiningParametersAbstract miningParameters = new DFMMiningParametersDefault();
		miningParameters.setNoiseThreshold(parameters.getPaths());

		Canceller canceller2 = new Canceller() {
			public boolean isCancelled() {
				return canceller.isCancelled();
			}
		};

		IMLogImpl log2 = new IMLogImpl(log.toXLog(), log.getClassifier(), log.getLifeCycleClassifier());
		DirectlyFollowsModel dfg = DFMMiner.mine(log2, miningParameters, canceller2);

		if (dfg != null) {
			return new IvMModel(dfg);
		} else {
			return null;
		}
	}

	public XLifeCycleClassifier getLifeCycleClassifier() {
		return new DFMMiningParametersDefault().getLifeCycleClassifier();
	}

	public IMLog2IMLogInfo getLog2logInfo() {
		return new IMLog2IMLogInfoLifeCycle();
	}
}
