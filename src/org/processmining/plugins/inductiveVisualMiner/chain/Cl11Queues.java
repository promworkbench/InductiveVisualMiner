package org.processmining.plugins.inductiveVisualMiner.chain;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCombination;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl11Queues extends ChainLink<IvMLog, QueueLengthsWrapper> {

	protected IvMLog generateInput(InductiveVisualMinerState state) {
		return state.getIvMLog();
	}

	protected QueueLengthsWrapper executeLink(IvMLog input) {
		Map<UnfoldedNode, QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(input);

		QueueLengths method = new QueueLengthsImplCombination(queueActivityLogs);
		
		//compute sojourn times
		TObjectDoubleHashMap<UnfoldedNode> sojournTimes = new TObjectDoubleHashMap<ProcessTree2Petrinet.UnfoldedNode>(10, 0.5f, -1);
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog activityLog = queueActivityLogs.get(unode);
			long sum = 0;
			int count = 0;
			for (int i = 0; i < activityLog.size(); i++) {
				if (activityLog.hasInitiate(i) && activityLog.hasComplete(i)) {
					sum += activityLog.getComplete(i) - activityLog.getInitiate(i);
					count++;
				}
			}
			sojournTimes.put(unode, sum / (count * 1.0));
		}
		
		return new QueueLengthsWrapper(method, queueActivityLogs, sojournTimes);
	}

	protected void processResult(QueueLengthsWrapper result, InductiveVisualMinerState state) {
		state.setQueueLengths(result);
		state.setVisualisationData(state.getColourMode().getVisualisationData(state));
	}

	public void cancel() {

	}
}
