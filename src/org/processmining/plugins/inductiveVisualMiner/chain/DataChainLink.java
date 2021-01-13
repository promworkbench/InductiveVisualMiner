package org.processmining.plugins.inductiveVisualMiner.chain;

public interface DataChainLink {
	public String[] getInputNames();

	/**
	 * 
	 * @return the name of the chain link for debug purposes
	 */
	public String getName();
}
