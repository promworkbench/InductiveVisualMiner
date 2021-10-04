package org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Histogram;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class RowBlockModelHistogram<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "model-time-hist";
	}

	public String getStatusBusyMessage() {
		return "Plotting model time..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMModel model = inputs.get(IvMObject.model);

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			return TraceDataRowBlock.merge(createAttributeData(model, logFiltered, canceller),
					createAttributeData(model, negativeLog, canceller), canceller);
		} else {
			return createAttributeData(model, logFiltered, canceller);
		}
	}

	public static List<DataRow<Object>> createAttributeData(IvMModel model, IvMLogFiltered log,
			IvMCanceller canceller) {
		List<DataRow<Object>> result = new ArrayList<>();

		for (DurationType durationType : DurationType.valuesAt(Level.activity)) {
			TIntObjectMap<TLongList> durations = getDurations(model, log, durationType, canceller);

			if (canceller.isCancelled()) {
				return null;
			}

			for (int node : model.getAllNodes()) {
				if (model.isActivity(node)) {
					if (!durations.get(node).isEmpty()) {
						BufferedImage image = Histogram.create(durations.get(node).toArray());
						result.add(new DataRow<Object>(DisplayType.image(image), model.getActivityName(node),
								durationType.toString(), "histogram"));
					}
				}
			}
		}
		return result;
	}

	public static TIntObjectMap<TLongList> getDurations(IvMModel model, IvMLogFiltered log, DurationType durationType,
			IvMCanceller canceller) {
		//init
		TIntObjectMap<TLongList> result = new TIntObjectHashMap<>(10, 0.5f, -1);
		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				result.put(node, new TLongArrayList());
			}
		}

		//gather
		for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
			IvMTrace trace = traceIt.next();

			if (canceller.isCancelled()) {
				return null;
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

			//capture activity instances
			ActivityInstanceIterator it = trace.activityInstanceIterator(model);
			while (it.hasNext()) {

				if (canceller.isCancelled()) {
					return null;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance = it.next();

				if (instance != null && durationType.applies(startTrace, instance, endTrace)) {
					result.get(instance.getA()).add(durationType.getDistance(startTrace, instance, endTrace));
				}
			}
		}
		return result;
	}
}
