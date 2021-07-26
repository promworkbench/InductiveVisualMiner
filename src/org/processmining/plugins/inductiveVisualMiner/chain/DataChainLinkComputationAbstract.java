package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainLinkComputationAbstract<C> extends DataChainLinkAbstract<C>
		implements DataChainLinkComputation<C> {

	private IvMObject<?>[] outputObjects;

	public abstract IvMObject<?>[] createOutputObjects();

	@Override
	public IvMObject<?>[] getOutputObjects() {
		if (outputObjects == null) {
			outputObjects = createOutputObjects();
		}
		return outputObjects;
	}
}