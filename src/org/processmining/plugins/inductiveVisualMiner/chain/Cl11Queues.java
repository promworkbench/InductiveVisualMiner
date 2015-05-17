package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsUsingResources;
import org.processmining.processtree.ProcessTree;

public class Cl11Queues extends ChainLink<Pair<ProcessTree, IvMLog>, QueueLengths> {

	protected Pair<ProcessTree, IvMLog> generateInput(InductiveVisualMinerState state) {
		return Pair.of(state.getTree(), state.getTimedLog());
	}

	protected QueueLengths executeLink(Pair<ProcessTree, IvMLog> input) {
		return new QueueLengthsUsingResources(input.getA(), input.getB());
	}

	protected void processResult(QueueLengths result, InductiveVisualMinerState state) {
		state.setQueueLengths(result);
	}

	public void cancel() {

	}
}
