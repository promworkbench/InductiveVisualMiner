package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Type;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCombination;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;

public class Cl13Performance extends ChainLink<Pair<IvMModel, IvMLog>, PerformanceWrapper> {

	protected Pair<IvMModel, IvMLog> generateInput(InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Pair.of(state.getModel(), (IvMLog) state.getIvMLogFiltered());
		} else {
			return null;
		}
	}

	protected PerformanceWrapper executeLink(Pair<IvMModel, IvMLog> input, IvMCanceller canceller) {
		if (input != null) {
			IvMModel model = input.getA();
			IvMLog log = input.getB();
			TIntObjectMap<QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(model, log);

			QueueLengths method = new QueueLengthsImplCombination(queueActivityLogs);

			PerformanceWrapper result = new PerformanceWrapper(method, queueActivityLogs, model.getMaxNumberOfNodes());

			//compute times
			for (TIntIterator it = queueActivityLogs.keySet().iterator(); it.hasNext();) {
				int unode = it.next();
				QueueActivityLog activityLog = queueActivityLogs.get(unode);
				for (int i = 0; i < activityLog.size(); i++) {

					//waiting time
					if (activityLog.hasInitiate(i) && activityLog.hasStart(i)) {
						result.addValue(Type.waiting, unode, activityLog.getStart(i) - activityLog.getInitiate(i));
					}

					//queueing time
					if (activityLog.hasEnqueue(i) && activityLog.hasStart(i)) {
						result.addValue(Type.queueing, unode, activityLog.getStart(i) - activityLog.getEnqueue(i));
					}

					//service time
					if (activityLog.hasStart(i) && activityLog.hasComplete(i)) {
						result.addValue(Type.service, unode, activityLog.getComplete(i) - activityLog.getStart(i));
					}

					//sojourn time
					if (activityLog.hasInitiate(i) && activityLog.hasComplete(i)) {
						result.addValue(Type.sojourn, unode, activityLog.getComplete(i) - activityLog.getInitiate(i));
					}
				}
			}

			result.finalise();
			return result;
		} else {
			return null;
		}
	}

	protected void processResult(PerformanceWrapper result, InductiveVisualMinerState state) {
		state.setPerformance(result);
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setPerformance(null);
		state.setVisualisationData(null);
	}

	public String getName() {
		return "measure performance";
	}
}
