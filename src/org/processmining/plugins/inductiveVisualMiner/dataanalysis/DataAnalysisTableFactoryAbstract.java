package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;

public abstract class DataAnalysisTableFactoryAbstract implements DataAnalysisTableFactory {

	public IvMObject<?>[] inputObjects;
	public IvMObject<?>[] triggerObjects;

	protected abstract IvMObject<?>[] createInputObjects();

	protected IvMObject<?>[] createTriggerObjects() {
		return new IvMObject<?>[] {};
	}

	public IvMObject<?>[] getInputObjects() {
		if (inputObjects == null) {
			inputObjects = createInputObjects();
		}
		return inputObjects;
	}

	public IvMObject<?>[] getTriggerObjects() {
		if (triggerObjects == null) {
			triggerObjects = createInputObjects();
		}
		return triggerObjects;
	}
}
