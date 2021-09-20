package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.EnumMap;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class PerformanceComputer {

	public static Performance compute(IvMLogFiltered log, IvMModel model, QueueLengths lengths,
			TIntObjectMap<QueueActivityLog> queueActivityLogs, IvMCanceller canceller) {
		PerformanceImpl result = new PerformanceImpl(lengths, queueActivityLogs, model.getMaxNumberOfNodes());

		computeNodeMeasures(log, model, canceller, result);

		computeProcessMeasures(log, model, canceller, result);

		if (canceller.isCancelled()) {
			return null;
		}

		return result;
	}

	private static void computeProcessMeasures(IvMLogFiltered log, IvMModel model, IvMCanceller canceller,
			PerformanceImpl result) {
		//initialise
		EnumMap<DurationType, TLongList> durationValues = new EnumMap<>(DurationType.class);
		for (DurationType duration : DurationType.valuesAt(Level.process)) {
			durationValues.put(duration, new TLongArrayList());
		}

		//gather
		for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
			IvMTrace trace = traceIt.next();

			if (canceller.isCancelled()) {
				return;
			}

			//find the start timestamp of the trace
			IvMMove startTrace = null;
			IvMMove endTrace = null;
			{
				for (IvMMove move : trace) {
					if (move.getLogTimestamp() != null) {
						if (startTrace == null || move.getLogTimestamp() < startTrace.getLogTimestamp()) {
							startTrace = move;
						}
						if (endTrace == null || move.getLogTimestamp() > endTrace.getLogTimestamp()) {
							endTrace = move;
						}
					}
				}
			}

			if (canceller.isCancelled()) {
				return;
			}

			//capture activity instances
			ActivityInstanceIterator it = trace.activityInstanceIterator(model);
			while (it.hasNext()) {

				if (canceller.isCancelled()) {
					return;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance = it.next();

				if (instance != null) {
					for (DurationType durationType : DurationType.valuesAt(Level.process)) {
						if (instance != null && durationType.applies(startTrace, instance, endTrace)) {
							long duration = durationType.getDistance(startTrace, instance, endTrace);
							durationValues.get(durationType).add(duration);
						}
					}
				}
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		//finalise
		for (DurationType durationType : DurationType.valuesAt(Level.process)) {
			long[] arr = durationValues.get(durationType).toArray();
			for (Aggregate gather : Aggregate.values()) {
				long value = gather.finalise(arr);
				if (value != Long.MIN_VALUE) {
					result.setProcessMeasure(durationType, gather, value);
				} else {
					result.setProcessMeasure(durationType, gather, -1);
				}
			}
		}
	}

	private static void computeNodeMeasures(IvMLogFiltered log, IvMModel model, IvMCanceller canceller,
			PerformanceImpl result) {
		//initialise
		TIntObjectMap<EnumMap<DurationType, TLongList>> durationValues = new TIntObjectHashMap<>();
		for (int node : model.getAllNodes()) {
			EnumMap<DurationType, TLongList> d = new EnumMap<>(DurationType.class);
			for (DurationType duration : DurationType.valuesAt(Level.activity)) {
				d.put(duration, new TLongArrayList());
			}
			durationValues.put(node, d);
		}

		//gather
		for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
			IvMTrace trace = traceIt.next();

			if (canceller.isCancelled()) {
				return;
			}

			//find the start timestamp of the trace
			IvMMove startTrace = null;
			IvMMove endTrace = null;
			{
				for (IvMMove move : trace) {
					if (move.getLogTimestamp() != null) {
						if (startTrace == null || move.getLogTimestamp() < startTrace.getLogTimestamp()) {
							startTrace = move;
						}
						if (endTrace == null || move.getLogTimestamp() > endTrace.getLogTimestamp()) {
							endTrace = move;
						}
					}
				}
			}

			if (canceller.isCancelled()) {
				return;
			}

			//capture activity instances
			ActivityInstanceIterator it = trace.activityInstanceIterator(model);
			while (it.hasNext()) {

				if (canceller.isCancelled()) {
					return;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance = it.next();

				if (instance != null) {
					int node = instance.getA();

					for (DurationType durationType : DurationType.valuesAt(Level.activity)) {
						if (instance != null && durationType.applies(startTrace, instance, endTrace)) {
							long duration = durationType.getDistance(startTrace, instance, endTrace);
							durationValues.get(node).get(durationType).add(duration);
						}
					}
				}
			}
		}

		if (canceller.isCancelled()) {
			return;
		}

		//finalise
		for (int node : model.getAllNodes()) {
			for (DurationType durationType : DurationType.valuesAt(Level.activity)) {
				long[] arr = durationValues.get(node).get(durationType).toArray();
				for (Aggregate gather : Aggregate.values()) {
					long value = gather.finalise(arr);
					if (value != Long.MIN_VALUE) {
						result.setNodeMeasure(durationType, gather, node, value);
					} else {
						result.setNodeMeasure(durationType, gather, node, -1);
					}
				}
			}
		}
	}
}