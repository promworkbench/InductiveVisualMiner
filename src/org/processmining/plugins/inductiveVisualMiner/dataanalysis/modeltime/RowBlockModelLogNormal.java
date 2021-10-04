package org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Histogram;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;
import org.processmining.statisticaltests.helperclasses.Correlation;

import gnu.trove.list.TLongList;
import gnu.trove.map.TIntObjectMap;

public class RowBlockModelLogNormal<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "model-time-lognormal";
	}

	public String getStatusBusyMessage() {
		return "Fitting time lognormal";
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
						Pair<Double, Double> logNormal = fit(durations.get(node).toArray());
						if (!Double.isNaN(logNormal.getA()) && !Double.isNaN(logNormal.getB())) {
							result.add(new DataRow<Object>(DisplayType.numeric(logNormal.getA()),
									model.getActivityName(node), durationType.toString(), "lognormal μ"));
							result.add(new DataRow<Object>(DisplayType.numeric(logNormal.getB()),
									model.getActivityName(node), durationType.toString(), "lognormal σ"));

							BufferedImage image = Histogram.create(durations.get(node).toArray(),
									new LogNormalDistribution(logNormal.getA(), logNormal.getB()));
							result.add(new DataRow<Object>(DisplayType.image(image), model.getActivityName(node),
									durationType.toString(), "lognormal model"));
						} else {
							result.add(new DataRow<Object>(DisplayType.NA(), model.getActivityName(node),
									durationType.toString(), "lognormal μ"));
							result.add(new DataRow<Object>(DisplayType.NA(), model.getActivityName(node),
									durationType.toString(), "lognormal σ"));
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
		double[] logValues = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			if (array[i] <= 0) {
				return Pair.of(Double.NaN, Double.NaN);
			}
			logValues[i] = Math.log(array[i]);
		}

		BigDecimal mean = Correlation.mean(logValues);
		double stdev = Correlation.standardDeviation(logValues, mean);

		return Pair.of(mean.doubleValue(), stdev);
	}
}