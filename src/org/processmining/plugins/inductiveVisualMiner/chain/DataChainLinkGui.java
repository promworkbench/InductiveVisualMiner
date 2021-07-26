package org.processmining.plugins.inductiveVisualMiner.chain;

public interface DataChainLinkGui<C, P> extends DataChainLink<C> {

	/**
	 * Updates the gui by invalidation. (e.g. one of the inputs got replaced).
	 * Will be called on the GUI thread.
	 * 
	 * @param panel
	 */
	public void invalidate(P panel);

	public void updateGui(P panel, IvMObjectValues inputs) throws Exception;

}