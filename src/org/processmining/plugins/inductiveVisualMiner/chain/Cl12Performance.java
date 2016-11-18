package org.processmining.plugins.inductiveVisualMiner.chain;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCombination;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;

public class Cl12Performance extends ChainLink<Pair<IvMEfficientTree, IvMLog>, PerformanceWrapper> {

	protected Pair<IvMEfficientTree, IvMLog> generateInput(InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Pair.of(state.getTree(), (IvMLog) state.getIvMLogFiltered());
		} else {
			return null;
		}
	}

	protected PerformanceWrapper executeLink(Pair<IvMEfficientTree, IvMLog> input, IvMCanceller canceller) {
		if (input != null) {
			IvMEfficientTree tree = input.getA();
			IvMLog log = input.getB();
			TIntObjectMap<QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(tree, log);

			QueueLengths method = new QueueLengthsImplCombination(queueActivityLogs);

			//compute times
			TIntDoubleHashMap waitingTimes = new TIntDoubleHashMap(10, 0.5f, -1, -1);
			TIntDoubleHashMap queueingTimes = new TIntDoubleHashMap(10, 0.5f, -1, -1);
			TIntDoubleHashMap serviceTimes = new TIntDoubleHashMap(10, 0.5f, -1, -1);
			TIntDoubleHashMap sojournTimes = new TIntDoubleHashMap(10, 0.5f, -1, -1);
			for (TIntIterator it = queueActivityLogs.keySet().iterator(); it.hasNext();) {
				int unode = it.next();
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
		} else {
			return null;
		}
	}

	protected void processResult(PerformanceWrapper result, InductiveVisualMinerState state) {
		state.setPerformance(result);
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}
}
