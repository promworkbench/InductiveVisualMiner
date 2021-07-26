package org.processmining.plugins.inductiveVisualMiner.chain;

public interface DataChainLinkComputation<C> extends DataChainLink<C> {

	public IvMObject<?>[] getOutputObjects();

	/**
	 * Performs the computation, given the input. Side-effects not allowed;
	 * should be thread-safe.
	 * 
	 * @param inputs
	 * @param canceller
	 * @return
	 * @throws Exception
	 */
	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception;

	/**
	 * 
	 * @return The text that is to be shown when this chainlink is executing,
	 *         with two postfix dots.
	 */
	public String getStatusBusyMessage();

}