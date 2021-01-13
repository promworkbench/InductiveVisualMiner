package org.processmining.plugins.inductiveVisualMiner.chain;

public interface OnStatus {

	/**
	 * Will be executed in an arbitrary thread.
	 * 
	 * @param chainLink
	 */
	public void startComputation(DataChainLinkComputation chainLink);

	/**
	 * Will be executed in an arbitrary thread.
	 * 
	 * @param chainLink
	 */
	public void endComputation(DataChainLinkComputation chainLink);

}