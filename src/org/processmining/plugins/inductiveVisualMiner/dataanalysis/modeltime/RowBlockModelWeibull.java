package org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;

import gnu.trove.list.TLongList;
import gnu.trove.map.TIntObjectMap;

public class RowBlockModelWeibull<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "model-time-weibull";
	}

	public String getStatusBusyMessage() {
		return "Fitting time Weibull";
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

	private List<DataRow<Object>> createAttributeData(IvMModel model, IvMLogFiltered log, IvMCanceller canceller) {
		List<DataRow<Object>> result = new ArrayList<>();

		for (DurationType durationType : DurationType.valuesAt(Level.activity)) {
			TIntObjectMap<TLongList> durations = RowBlockModelHistogram.getDurations(model, log, durationType,
					canceller);

			if (canceller.isCancelled()) {
				return null;
			}

			for (int node : model.getAllNodes()) {
				if (model.isActivity(node)) {
					if (!durations.get(node).isEmpty()) {
						Pair<Double, Double> weibull = fit(durations.get(node).toArray());
						if (!Double.isNaN(weibull.getA()) && !Double.isNaN(weibull.getB())) {
							result.add(new DataRow<Object>(DisplayType.numeric(weibull.getA()),
									model.getActivityName(node), durationType.toString(), "Weibull-shape (λ)"));
							result.add(new DataRow<Object>(DisplayType.numeric(weibull.getB()),
									model.getActivityName(node), durationType.toString(), "Weibull-scale (k)"));
						} else {
							result.add(new DataRow<Object>(DisplayType.NA(), model.getActivityName(node),
									durationType.toString(), "Weibull shape (λ)"));
							result.add(new DataRow<Object>(DisplayType.NA(), model.getActivityName(node),
									durationType.toString(), "Weibull scale (k)"));
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Idea:
	 * https://www.real-statistics.com/distribution-fitting/fitting-weibull-regression/
	 * 
	 * @param array
	 * @return pair of shape (k), scale (lambda)
	 */
	private Pair<Double, Double> fit(long[] array) {
		Arrays.sort(array);

		SimpleRegression regression = new SimpleRegression();

		for (int i = 0; i < array.length; i++) {
			double x = Math.log(array[i]);
			double y = Math.log(-Math.log(1 - (i + 1 - 0.5) / array.length));

			regression.addData(x, y);
		}

		double slope = regression.getSlope();
		double intercept = regression.getIntercept();

		double k = slope;
		double lambda = Math.exp(-intercept / slope);

		return Pair.of(lambda, k);
	}
}