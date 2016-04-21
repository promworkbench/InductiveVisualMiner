package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper;

import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.processtree.ProcessTree;

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
	 * Perform the mining. Will be called asynchronously, so should be thread-safe.
	 * @param log
	 * @param parameters
	 * @param canceller 
	 * @return
	 */
	public abstract ProcessTree mine(IMLog log, VisualMinerParameters parameters, IvMCanceller canceller);

	public int compareTo(VisualMinerWrapper o) {
		return this.toString().compareTo(o.toString());
	}
}
