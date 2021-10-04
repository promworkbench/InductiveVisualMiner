package org.processmining.plugins.inductiveVisualMiner.chain;

/**
 * Give the user the filtered view as soon as possible, by slightly delaying the advanced analyses.
 * @author sander
 *
 * @param <C>
 */
public class Cl21AdvancedAnalysesDelay<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl21 delay";
	}

	public String getStatusBusyMessage() {
		return "Delay for advanced analyses";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_filtered };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analyses_delay };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		Thread.sleep(1000);
		return new IvMObjectValues().//
				s(IvMObject.data_analyses_delay, "");
	}
}