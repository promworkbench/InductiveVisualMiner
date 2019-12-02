package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeGlobal;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapperTraces;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapperTraces.Type;
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
			PerformanceWrapperTraces resultTraces = new PerformanceWrapperTraces();

			//compute node times
			for (TIntIterator it = queueActivityLogs.keySet().iterator(); it.hasNext();) {
				int unode = it.next();
				QueueActivityLog activityLog = queueActivityLogs.get(unode);
				for (int i = 0; i < activityLog.size(); i++) {

					//waiting time
					if (activityLog.hasInitiate(i) && activityLog.hasStart(i)) {
						long waiting = activityLog.getStart(i) - activityLog.getInitiate(i);
						result.addNodeValue(TypeNode.waiting, unode, waiting);
						resultTraces.addValue(Type.waiting, activityLog.getTraceIndex(i), waiting);
					}

					//queueing time
					if (activityLog.hasEnqueue(i) && activityLog.hasStart(i)) {
						long queueing = activityLog.getStart(i) - activityLog.getEnqueue(i);
						result.addNodeValue(TypeNode.queueing, unode, queueing);
						resultTraces.addValue(Type.queueing, activityLog.getTraceIndex(i), queueing);
					}

					//service time
					if (activityLog.hasStart(i) && activityLog.hasComplete(i)) {
						long service = activityLog.getComplete(i) - activityLog.getStart(i);
						result.addNodeValue(TypeNode.service, unode, service);
						resultTraces.addValue(Type.service, activityLog.getTraceIndex(i), service);
					}

					//sojourn time
					if (activityLog.hasInitiate(i) && activityLog.hasComplete(i)) {
						long sojourn = activityLog.getComplete(i) - activityLog.getInitiate(i);
						result.addNodeValue(TypeNode.sojourn, unode, sojourn);

						/**
						 * We could technically show trace sojourn time, but
						 * this would cause confusion with the trace duration.
						 */
						//resultTraces.addValue(Type.sojourn, activityLog.getTraceIndex(i), sojourn);
					}

					//elapsed time
					if (activityLog.hasStartTrace(i) && activityLog.hasStart(i)) {
						result.addNodeValue(TypeNode.elapsed, unode,
								activityLog.getStart(i) - activityLog.getStartTrace(i));
					} else if (activityLog.hasStartTrace(i) && activityLog.hasComplete(i)) {
						result.addNodeValue(TypeNode.elapsed, unode,
								activityLog.getComplete(i) - activityLog.getStartTrace(i));
					}

					//remaining time
					if (activityLog.hasEndTrace(i) && activityLog.hasComplete(i)) {
						result.addNodeValue(TypeNode.remaining, unode,
								activityLog.getEndTrace(i) - activityLog.getComplete(i));
					} else if (activityLog.hasEndTrace(i) && activityLog.hasStart(i)) {
						result.addNodeValue(TypeNode.remaining, unode,
								activityLog.getEndTrace(i) - activityLog.getStart(i));
					}
				}
			}

			resultTraces.finalise(result);

			//compute global times
			for (IvMTrace trace : log) {
				if (trace.getRealStartTime() != null && trace.getRealEndTime() != null) {
					result.addGlobalValue(TypeGlobal.duration, trace.getRealEndTime() - trace.getRealStartTime());
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

	public String getStatusBusyMessage() {
		return "Measuring performance..";
	}
}
