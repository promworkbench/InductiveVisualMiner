package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainLinkComputationAbstract extends DataChainLinkAbstract
		implements DataChainLinkComputation {

	private IvMObject<?>[] outputObjects;

	public abstract IvMObject<?>[] createOutputObjects();

	@Override
	public IvMObject<?>[] getOutputNames() {
		if (outputObjects == null) {
			outputObjects = createOutputObjects();
		}
		return outputObjects;
	}
}