package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public class Cl08ApplyHighlighting extends ChainLink<Object, Object> {

	protected Object generateInput(InductiveVisualMinerState state) {
		return null;
	}

	protected Object executeLink(Object input) {
		return null;
	}

	protected void processResult(Object result, InductiveVisualMinerState state) {
		state.setVisualisationData(state.getColourMode().getVisualisationData(state));
	}

	public void cancel() {

	}

}