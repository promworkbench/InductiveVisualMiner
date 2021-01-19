package org.processmining.plugins.inductiveVisualMiner.chain;

public interface DataChainLink {

	/**
	 * If all of the inputs are available, the computation is executed.
	 * 
	 * @return
	 */
	public IvMObject<?>[] getRequiredObjects();

	/**
	 * If any of the trigger objects becomes available, the computation is
	 * executed (provided all input objects are available as well). Thus, not
	 * all trigger objects are guaranteed to be available on execution.
	 * 
	 * @return
	 */
	public IvMObject<?>[] getOptionalObjects();

	/**
	 * If any of the non-trigger objects is available when the computation is
	 * executed, it will be provided. However, the computation will not be
	 * triggered by the object.
	 * 
	 * @return
	 */
	public IvMObject<?>[] getNonTriggerObjects();

	/**
	 * 
	 * @return the name of the chain link for debug purposes
	 */
	public String getName();
}
