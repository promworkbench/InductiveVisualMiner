package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

public interface DataChainLinkComputation extends DataChainLink {

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
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception;

	/**
	 * 
	 * @return The text that is to be shown when this chainlink is executing,
	 *         with two postfix dots.
	 */
	public String getStatusBusyMessage();

}