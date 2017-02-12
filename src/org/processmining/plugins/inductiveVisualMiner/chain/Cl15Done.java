package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public class Cl15Done extends ChainLink<Object, Object> {

	protected Object generateInput(InductiveVisualMinerState state) {
		return null;
	}

	protected Object executeLink(Object input, IvMCanceller canceller) throws Exception {
		Thread.sleep(5000);
		return null;
	}

	protected void processResult(Object result, InductiveVisualMinerState state) {
		
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		
	}

}
