package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper;

import org.processmining.framework.plugin.annotations.KeepInProMCache;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

@KeepInProMCache
@VisualMinerWrapperAnnotation
public abstract class VisualMinerWrapper implements Comparable<VisualMinerWrapper> {

	/**
	 * Constructor. User is waiting when this function is called.
	 */
	public VisualMinerWrapper() {

	}

	/**
	 * Returns the name of this miner, to be displayed in gui.
	 * 
	 * @return
	 */
	public abstract String toString();

	/**
	 * Perform the mining. Will be called asynchronously, so should be
	 * thread-safe.
	 * 
	 * @param log
	 * @param parameters
	 * @param canceller
	 * @return
	 */
	public abstract IvMModel mine(IMLog log, IMLogInfo logInfo, VisualMinerParameters parameters,
			IvMCanceller canceller);

	public int compareTo(VisualMinerWrapper o) {
		return this.toString().compareTo(o.toString());
	}
	
	public abstract XLifeCycleClassifier getLifeCycleClassifier();
	
	public abstract IMLog2IMLogInfo getLog2logInfo();

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VisualMinerWrapper) {
			return this.toString().equals(obj.toString());
		}
		return false;
	}
}
