package org.processmining.plugins.inductiveVisualMiner.cost;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

public abstract class CostModelAbstract implements CostModel {

	public static final String attribute = "cost";
	public static final int ms2hr = 1000 * 60 * 60;

	protected double[] parameters;
	protected final IvMModel model;
	private List<DataRow<Object>> modelProperties = new ArrayList<>();

	public CostModelAbstract(IvMModel model) {
		this.model = model;
	}

	/**
	 * No side effects allowed. Will only be called for non-silent activities.
	 * 
	 * @param node
	 * @param initiate
	 * @param enqueue
	 * @param start
	 * @param complete
	 * @return
	 */
	public abstract double[] getInputs(IvMMove startTrace,
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove endTrace);

	public abstract int getNumberOfParameters();

	public Pair<double[], Double> getInputsAndCost(IvMTrace trace, IvMCanceller canceller) {
		double[] inputs = new double[getNumberOfParameters()];

		double traceCost = getCost(trace);

		if (Double.isNaN(traceCost)) {
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

		//capture log moves
		for (IvMMove move : trace) {
			if (move.isLogMove() && !move.isIgnoredLogMove()) {
				double[] inputsA = getInputs(startTrace, Sextuple.of(-1, null, null, null, null, move), endTrace);

				merge(inputs, inputsA);
			}
		}

		//capture activity instances
		{
			ActivityInstanceIterator it = trace.activityInstanceIterator(model);
			while (it.hasNext()) {

				if (canceller.isCancelled()) {
					return null;
				}

				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> a = it.next();

				if (a != null) {
					double[] inputsA = getInputs(startTrace, a, endTrace);

					merge(inputs, inputsA);
				}
			}
		}

		return Pair.of(inputs, traceCost);
	}

	private void merge(double[] inputs, double[] inputsA) {
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] += inputsA[i];
		}
	}

	public double[] getParameters() {
		return parameters;
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public List<DataRow<Object>> getModelRepresentation() {
		List<DataRow<Object>> result = new ArrayList<>();
		result.addAll(getModelProperties());
		result.add(new DataRow<Object>("cost model", "number of parameters", DisplayType.numeric(parameters.length)));
		return result;
	}

	public List<DataRow<Object>> getModelProperties() {
		return modelProperties;
	}

	public void setModelProperties(List<DataRow<Object>> modelProperties) {
		this.modelProperties = modelProperties;
	}

	public static final DecimalFormat formatE = new DecimalFormat("0.###E0");
	public static final DecimalFormat format = new DecimalFormat("0.##");

	public static final String format(double value) {
		if (value == 0) {
			return "0";
		}
		if (Math.abs(value) > 9999999 || Math.abs(value) < 0.01) {
			return formatE.format(value);
		}
		return format.format(value);
	}
}
