package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class DataRowBlockLogAttributesHighlighted<C, P> extends DataRowBlockComputer<C, P> {

	public String getName() {
		return "log-vir-att";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_filtered, IvMObject.aligned_log };
	}

	public List<DataRow> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		IvMLogNotFiltered log = inputs.get(IvMObject.aligned_log);
		IvMLogFilteredImpl logFiltered = inputs.get(IvMObject.aligned_log_filtered);

		ArrayList<DataRow> result = new ArrayList<>();

		//non-filtered log
		{
			int numberOfTraces = 0;
			int numberOfEvents = 0;
			for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
				numberOfTraces++;

				IvMTrace trace = it.next();
				for (IvMMove move : trace) {
					if (move.getAttributes() != null) {
						numberOfEvents++;
					}
				}

				if (canceller.isCancelled()) {
					return null;
				}
			}

			DisplayType x = DisplayType.numeric(numberOfTraces);
			result.add(new DataRow(x, "number of traces"));

			DisplayType y = DisplayType.numeric(numberOfEvents);
			result.add(new DataRow(y, "number of events"));

			DisplayType z = DisplayType.numeric(Fitness.compute(log));
			result.add(new DataRow(z, "fitness"));
		}

		if (logFiltered.isSomethingFiltered()) {
			//number of traces and events (highlighted)
			{
				int numberOfTraces = 0;
				int numberOfEvents = 0;
				for (IteratorWithPosition<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
					numberOfTraces++;

					IvMTrace trace = it.next();
					for (IvMMove move : trace) {
						if (move.getAttributes() != null) {
							numberOfEvents++;
						}
					}

					if (canceller.isCancelled()) {
						return null;
					}
				}

				DisplayType x = DisplayType.numeric(numberOfTraces);
				result.add(new DataRow(x, "number of traces (highlighted traces)"));

				DisplayType y = DisplayType.numeric(numberOfEvents);
				result.add(new DataRow(y, "number of events (highlighted traces)"));

				//fitness (highlighted)
				DisplayType z = DisplayType.numeric(Fitness.compute(logFiltered));
				result.add(new DataRow(z, "fitness (highlighted traces)"));
			}

			//not-highlighted
			{
				IvMLogFilteredImpl negativeLog = logFiltered.clone();
				negativeLog.invert();

				int numberOfTraces = 0;
				int numberOfEvents = 0;
				for (IteratorWithPosition<IvMTrace> it = negativeLog.iterator(); it.hasNext();) {
					numberOfTraces++;

					IvMTrace trace = it.next();
					for (IvMMove move : trace) {
						if (move.getAttributes() != null) {
							numberOfEvents++;
						}
					}

					if (canceller.isCancelled()) {
						return null;
					}
				}

				DisplayType x = DisplayType.numeric(numberOfTraces);
				result.add(new DataRow(x, "number of traces (not-highlighted traces)"));

				DisplayType y = DisplayType.numeric(numberOfEvents);
				result.add(new DataRow(y, "number of events (not-highlighted traces)"));

				DisplayType z = DisplayType.numeric(Fitness.compute(negativeLog));
				result.add(new DataRow(z, "fitness (not-highlighted traces)"));

				if (canceller.isCancelled()) {
					return null;
				}
			}
		}

		return result;
	}
}