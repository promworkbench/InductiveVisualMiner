package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.EnumMap;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public abstract class CostModelAbstract implements CostModel {

	public abstract double getCost(int node, IvMMove initiate, IvMMove enqueue, IvMMove start, IvMMove complete);

	TIntObjectMap<EnumMap<TypeNode, Double>> parameter_time = new TIntObjectHashMap<>();
	TIntDoubleMap parameter_syncMove = new TIntDoubleHashMap();
	TIntDoubleMap parameter_logMove = new TIntDoubleHashMap();
	TIntDoubleMap parameter_modelMove = new TIntDoubleHashMap();

	public double getCost(IvMModel model, IvMTrace trace) {
		ActivityInstanceIterator it = trace.activityInstanceIterator(model);

		double result = 0;

		//find the start timestamp of the trace
		Long startTrace = Long.MAX_VALUE;
		Long endTrace = Long.MIN_VALUE;
		for (IvMMove move : trace) {
			if (move.getLogTimestamp() != null) {
				startTrace = Math.min(startTrace, move.getLogTimestamp());
				endTrace = Math.max(endTrace, move.getLogTimestamp());
			}
		}

		if (startTrace == Long.MAX_VALUE) {
			startTrace = null;
		}
		if (endTrace == Long.MIN_VALUE) {
			endTrace = null;
		}

		while (it.hasNext()) {
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> activityInstance = it.next();

			if (activityInstance != null) {

				int node = activityInstance.getA();

				if (model.isActivity(node)) {

					Long initiate = null;
					Long enqueue = null;
					Long start = null;
					Long complete = null;
					IvMMove initiateMove = null;
					IvMMove enqueueMove = null;
					IvMMove startMove = null;
					IvMMove completeMove = null;

					if (activityInstance.getC() != null) {
						initiate = activityInstance.getC().getLogTimestamp();
						initiateMove = activityInstance.getC();
					}
					if (activityInstance.getD() != null) {
						enqueue = activityInstance.getD().getLogTimestamp();
						enqueueMove = activityInstance.getD();
					}
					if (activityInstance.getE() != null) {
						start = activityInstance.getE().getLogTimestamp();
						startMove = activityInstance.getE();
					}
					if (activityInstance.getF() != null) {
						complete = activityInstance.getF().getLogTimestamp();
						completeMove = activityInstance.getF();
					}

					result += getCost(node, initiateMove, enqueueMove, startMove, completeMove);
				}
			}
		}

		return result;
	}
}
