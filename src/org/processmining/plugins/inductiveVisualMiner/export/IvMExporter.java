package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;

public abstract class IvMExporter extends Exporter {

	private IvMObject<?>[] inputObjects;
	private IvMObject<?>[] triggerObjects;
	private IvMObjectValues inputs = null;

	protected abstract IvMObject<?>[] createInputObjects();

	protected abstract IvMObject<?>[] createTriggerObjects();

	public abstract void export(IvMObjectValues inputs, InductiveVisualMinerAnimationPanel panel, File file)
			throws Exception;

	@Override
	public final void export(NavigableSVGPanel panel, File file) throws Exception {
		assert inputs != null;
		export(inputs, (InductiveVisualMinerAnimationPanel) panel, file);
	}

	public void setInputs(IvMObjectValues inputs) {
		this.inputs = inputs;
	}

	public IvMObject<?>[] getInputObjects() {
		if (inputObjects == null) {
			inputObjects = createInputObjects();
		}
		return inputObjects;
	}

	public IvMObject<?>[] getTriggerObjects() {
		if (triggerObjects == null) {
			triggerObjects = createTriggerObjects();
		}
		return triggerObjects;
	}
}