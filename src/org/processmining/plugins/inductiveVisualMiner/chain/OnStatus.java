package org.processmining.plugins.inductiveVisualMiner.chain;

public interface OnStatus<C> {

	/**
	 * Will be executed in an arbitrary thread.
	 * 
	 * @param chainLink
	 */
	public void startComputation(DataChainLinkComputation<C> chainLink);

	/**
	 * Will be executed in an arbitrary thread.
	 * 
	 * @param chainLink
	 */
	public void endComputation(DataChainLinkComputation<C> chainLink);

}