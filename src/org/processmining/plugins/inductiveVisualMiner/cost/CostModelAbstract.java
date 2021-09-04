package org.processmining.plugins.inductiveVisualMiner.cost;

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

	protected double[] parameters;
	protected final IvMModel model;
	private List<Pair<String, Double>> qualityMetrics;
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
	public abstract double[] getInputs(int node, IvMMove initiate, IvMMove enqueue, IvMMove start, IvMMove complete);

	public abstract int getNumberOfParameters();

	public Pair<double[], Double> getInputsAndCost(IvMTrace trace, IvMCanceller canceller) {
		double[] inputs = new double[getNumberOfParameters()];

		double traceCost = getCost(trace);

		if (Double.isNaN(traceCost)) {
			return null;
		}

		//capture log moves
		for (IvMMove move : trace) {
			if (move.isLogMove() && !move.isIgnoredLogMove()) {
				double[] inputsA = getInputs(move.getLogMoveUnode(), null, null, null, move);

				merge(inputs, inputsA);
			}
		}

		//capture activity instances
		ActivityInstanceIterator it = trace.activityInstanceIterator(model);
		while (it.hasNext()) {

			if (canceller.isCancelled()) {
				return null;
			}

			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> a = it.next();

			if (a == null) {
				if (trace.size() <= 9) {
					//System.out.println("stop");
				}
			} else {
				double[] inputsA = getInputs(a.getA(), a.getC(), a.getD(), a.getE(), a.getF());

				merge(inputs, inputsA);
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

	public void setQualityMetrics(List<Pair<String, Double>> qualityMetrics) {
		this.qualityMetrics = qualityMetrics;
	}

	public List<Pair<String, Double>> getQualityMetrics() {
		return qualityMetrics;
	}

	public List<DataRow<Object>> getModelRepresentation() {
		List<DataRow<Object>> result = new ArrayList<>();
		result.addAll(getModelProperties());
		for (Pair<String, Double> p : qualityMetrics) {
			result.add(new DataRow<Object>("cost model", p.getA(), DisplayType.numeric(p.getB())));
		}
		result.add(new DataRow<Object>("cost model", "number of parameters", DisplayType.numeric(parameters.length)));
		return result;
	}

	public List<DataRow<Object>> getModelProperties() {
		return modelProperties;
	}

	public void setModelProperties(List<DataRow<Object>> modelProperties) {
		this.modelProperties = modelProperties;
	}
}
