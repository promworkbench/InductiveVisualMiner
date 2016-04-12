package org.processmining.plugins.inductiveVisualMiner.chain;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCombination;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl10Performance extends ChainLink<IvMLog, PerformanceWrapper> {

	protected IvMLog generateInput(InductiveVisualMinerState state) {
		return state.getIvMLogFiltered();
	}

	protected PerformanceWrapper executeLink(IvMLog input, ChainLinkCanceller canceller) {
		Map<UnfoldedNode, QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(input);

		QueueLengths method = new QueueLengthsImplCombination(queueActivityLogs);

		//compute times
		TObjectDoubleHashMap<UnfoldedNode> waitingTimes = new TObjectDoubleHashMap<UnfoldedNode>(10, 0.5f, -1);
		TObjectDoubleHashMap<UnfoldedNode> queueingTimes = new TObjectDoubleHashMap<UnfoldedNode>(10, 0.5f, -1);
		TObjectDoubleHashMap<UnfoldedNode> serviceTimes = new TObjectDoubleHashMap<UnfoldedNode>(10, 0.5f, -1);
		TObjectDoubleHashMap<UnfoldedNode> sojournTimes = new TObjectDoubleHashMap<UnfoldedNode>(10, 0.5f, -1);
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog activityLog = queueActivityLogs.get(unode);
			long sumWaiting = 0;
			int countWaiting = 0;
			long sumQueueing = 0;
			int countQueueing = 0;
			long sumService = 0;
			int countService = 0;
			long sumSojourn = 0;
			int countSojourn = 0;
			for (int i = 0; i < activityLog.size(); i++) {

				//waiting time
				if (activityLog.hasInitiate(i) && activityLog.hasStart(i)) {
					sumWaiting += activityLog.getStart(i) - activityLog.getInitiate(i);
					countWaiting++;
				}

				//queueing time
				if (activityLog.hasEnqueue(i) && activityLog.hasStart(i)) {
					sumQueueing += activityLog.getStart(i) - activityLog.getEnqueue(i);
					countQueueing++;
				}

				//service time
				if (activityLog.hasStart(i) && activityLog.hasComplete(i)) {
					sumService += activityLog.getComplete(i) - activityLog.getStart(i);
					countService++;
				}

				//sojourn time
				if (activityLog.hasInitiate(i) && activityLog.hasComplete(i)) {
					sumSojourn += activityLog.getComplete(i) - activityLog.getInitiate(i);
					countSojourn++;
				}
			}
			waitingTimes.put(unode, sumWaiting / (countWaiting * 1.0));
			queueingTimes.put(unode, sumQueueing / (countQueueing * 1.0));
			serviceTimes.put(unode, sumService / (countService * 1.0));
			sojournTimes.put(unode, sumSojourn / (countSojourn * 1.0));
		}

		return new PerformanceWrapper(method, queueActivityLogs, waitingTimes, queueingTimes, serviceTimes,
				sojournTimes);
	}

	protected void processResult(PerformanceWrapper result, InductiveVisualMinerState state) {
		state.setPerformance(result);
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}

	public void cancel() {

	}
}
