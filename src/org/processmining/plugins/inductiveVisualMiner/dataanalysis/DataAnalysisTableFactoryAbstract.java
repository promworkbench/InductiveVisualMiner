package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;

public abstract class DataAnalysisTableFactoryAbstract implements DataAnalysisTableFactory {

	public IvMObject<?>[] inputObjects;
	public IvMObject<?>[] optionalObjects;

	protected abstract IvMObject<?>[] createInputObjects();

	protected IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] {};
	}

	@Override
	public IvMObject<?>[] getInputObjects() {
		if (inputObjects == null) {
			inputObjects = createInputObjects();
		}
		return inputObjects;
	}

	@Override
	public IvMObject<?>[] getOptionalObjects() {
		if (optionalObjects == null) {
			optionalObjects = createOptionalObjects();
		}
		return optionalObjects;
	}
}
