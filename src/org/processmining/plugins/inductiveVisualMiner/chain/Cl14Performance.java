package org.processmining.plugins.inductiveVisualMiner.chain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
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

public class Cl14Performance extends
		IvMChainLink<Quadruple<IvMModel, IvMLogFiltered, IvMLogInfo, Mode>, Pair<PerformanceWrapper, AlignedLogVisualisationData>> {

	protected Quadruple<IvMModel, IvMLogFiltered, IvMLogInfo, Mode> generateInput(InductiveVisualMinerState state) {
		if (!state.isIllogicalTimeStamps()) {
			return Quadruple.of(state.getModel(), (IvMLogFiltered) state.getIvMLogFiltered(),
					state.getIvMLogInfoFiltered(), state.getMode());
		} else {
			return null;
		}
	}

	protected Pair<PerformanceWrapper, AlignedLogVisualisationData> executeLink(
			Quadruple<IvMModel, IvMLogFiltered, IvMLogInfo, Mode> input, IvMCanceller canceller) {
		if (input != null) {
			IvMModel model = input.getA();
			IvMLogFiltered log = input.getB();
			IvMLogInfo logInfo = input.getC();
			Mode mode = input.getD();

			TIntObjectMap<QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(model, log);

			QueueLengths method = new QueueLengthsImplCombination(queueActivityLogs);

			PerformanceWrapper performance = new PerformanceWrapper(method, queueActivityLogs,
					model.getMaxNumberOfNodes());
			PerformanceWrapperTraces resultTraces = new PerformanceWrapperTraces();

			//compute node times
			for (TIntIterator it = queueActivityLogs.keySet().iterator(); it.hasNext();) {
				int unode = it.next();

				PrintWriter writerSojourn = null;
				PrintWriter writerService = null;
				if (model.isActivity(unode)) {
					File fileSojourn = new File(
							"/home/sander/Desktop/sojournTime" + model.getActivityName(unode) + ".csv");
					File fileService = new File(
							"/home/sander/Desktop/serviceTime" + model.getActivityName(unode) + ".csv");
					try {
						writerSojourn = new PrintWriter(fileSojourn);
						writerService = new PrintWriter(fileService);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				QueueActivityLog activityLog = queueActivityLogs.get(unode);
				for (int i = 0; i < activityLog.size(); i++) {

					//waiting time
					if (activityLog.hasInitiate(i) && activityLog.hasStart(i)) {
						long waiting = activityLog.getStart(i) - activityLog.getInitiate(i);
						performance.addNodeValue(TypeNode.waiting, unode, waiting);
						resultTraces.addValue(Type.waiting, activityLog.getTraceIndex(i), waiting);
					}

					//queueing time
					if (activityLog.hasEnqueue(i) && activityLog.hasStart(i)) {
						long queueing = activityLog.getStart(i) - activityLog.getEnqueue(i);
						performance.addNodeValue(TypeNode.queueing, unode, queueing);
						resultTraces.addValue(Type.queueing, activityLog.getTraceIndex(i), queueing);
					}

					//service time
					if (activityLog.hasStart(i) && activityLog.hasComplete(i)) {
						long service = activityLog.getComplete(i) - activityLog.getStart(i);
						performance.addNodeValue(TypeNode.service, unode, service);
						resultTraces.addValue(Type.service, activityLog.getTraceIndex(i), service);

						writerService.println(service);
					}

					//sojourn time
					if (activityLog.hasInitiate(i) && activityLog.hasComplete(i)) {
						long sojourn = activityLog.getComplete(i) - activityLog.getInitiate(i);
						performance.addNodeValue(TypeNode.sojourn, unode, sojourn);
						writerSojourn.println(sojourn);

						/**
						 * We could technically show trace sojourn time, but
						 * this would cause confusion with the trace duration.
						 */
						//resultTraces.addValue(Type.sojourn, activityLog.getTraceIndex(i), sojourn);
					}

					//elapsed time
					if (activityLog.hasStartTrace(i) && activityLog.hasStart(i)) {
						performance.addNodeValue(TypeNode.elapsed, unode,
								activityLog.getStart(i) - activityLog.getStartTrace(i));
					} else if (activityLog.hasStartTrace(i) && activityLog.hasComplete(i)) {
						performance.addNodeValue(TypeNode.elapsed, unode,
								activityLog.getComplete(i) - activityLog.getStartTrace(i));
					}

					//remaining time
					if (activityLog.hasEndTrace(i) && activityLog.hasComplete(i)) {
						performance.addNodeValue(TypeNode.remaining, unode,
								activityLog.getEndTrace(i) - activityLog.getComplete(i));
					} else if (activityLog.hasEndTrace(i) && activityLog.hasStart(i)) {
						performance.addNodeValue(TypeNode.remaining, unode,
								activityLog.getEndTrace(i) - activityLog.getStart(i));
					}
				}
				if (model.isActivity(unode)) {
					writerSojourn.close();
					writerService.close();
				}
			}

			resultTraces.finalise(performance);

			//compute global times
			for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
				IvMTrace trace = it.next();
				if (trace.getRealStartTime() != null && trace.getRealEndTime() != null) {
					performance.addGlobalValue(TypeGlobal.duration, trace.getRealEndTime() - trace.getRealStartTime());
				}
			}

			performance.finalise();

			//compute model visualisation data
			AlignedLogVisualisationData visualisationData = mode.getVisualisationData(model, log, logInfo,
					queueActivityLogs, performance);

			return Pair.of(performance, visualisationData);
		} else

		{
			return null;
		}
	}

	protected void processResult(Pair<PerformanceWrapper, AlignedLogVisualisationData> result,
			InductiveVisualMinerState state) {
		state.setPerformance(result.getA());
		state.setVisualisationData(result.getB());
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
