package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCompleteResource;

public class Cl11Queues extends ChainLink<IvMLog, QueueLengths> {

	protected IvMLog generateInput(InductiveVisualMinerState state) {
		return state.getIvMLog();
	}

	protected QueueLengths executeLink(IvMLog input) {
		return new QueueLengthsImplCompleteResource(input);
	}

	protected void processResult(QueueLengths result, InductiveVisualMinerState state) {
		state.setQueueLengths(result);
	}

	public void cancel() {

	}
}
