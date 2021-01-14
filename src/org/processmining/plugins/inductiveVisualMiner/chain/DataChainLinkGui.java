package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;

public interface DataChainLinkGui extends DataChainLink {

	/**
	 * Updates the gui by invalidation. (e.g. one of the inputs got replaced).
	 * Will be called on the GUI thread.
	 * 
	 * @param panel
	 */
	public void invalidate(InductiveVisualMinerPanel panel);

	public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception;

}