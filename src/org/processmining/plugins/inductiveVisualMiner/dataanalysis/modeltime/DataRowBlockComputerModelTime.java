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

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DataRowBlockComputerModelTime<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "model-time";
	}

	public String getStatusBusyMessage() {
		return "Plotting model time..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered };
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

		TIntObjectMap<TLongList> serviceTimes = getServiceTimes(model, log, canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				BufferedImage image = Histogram.create(100, serviceTimes.get(node).toArray());
				result.add(new DataRow<Object>(DisplayType.image(image), model.getActivityName(node), "service time"));
			}
		}

		return result;
	}

	private static TIntObjectMap<TLongList> getServiceTimes(IvMModel model, IvMLogFiltered log,
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

			//capture activity instances
			ActivityInstanceIterator it = trace.activityInstanceIterator(model);
			while (it.hasNext()) {

				if (canceller.isCancelled()) {
					return null;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> a = it.next();

				if (a != null) {
					IvMMove start = a.getE();
					IvMMove complete = a.getF();
					if (start != null && start.getLogTimestamp() != null & complete != null
							&& complete.getLogTimestamp() != null) {
						result.get(a.getA()).add(complete.getLogTimestamp() - start.getLogTimestamp());
					}
				}
			}
		}
		return result;
	}
}
