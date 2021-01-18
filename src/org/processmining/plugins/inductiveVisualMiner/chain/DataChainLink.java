package org.processmining.plugins.inductiveVisualMiner.chain;

public interface DataChainLink {

	/**
	 * If all of the inputs are available, the computation is executed.
	 * 
	 * @return
	 */
	public IvMObject<?>[] getInputObjects();

	/**
	 * If any of the trigger objects becomes available, the computation is
	 * executed (provided all input objects are available as well). Thus, not
	 * all trigger objects are guaranteed to be available on execution.
	 * 
	 * @return
	 */
	public IvMObject<?>[] getTriggerObjects();

	/**
	 * 
	 * @return the name of the chain link for debug purposes
	 */
	public String getName();
}
