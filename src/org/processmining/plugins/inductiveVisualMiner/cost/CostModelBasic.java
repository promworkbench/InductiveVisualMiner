package org.processmining.plugins.inductiveVisualMiner.cost;

import java.text.DecimalFormat;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;

public class CostModelBasic extends CostModelAbstract {

	private final int numberOfParameters;
	private final int[] node2index;

	public static final String attribute = "cost";

	public CostModelBasic(IvMModel model) {
		super(model);

		{
			int result = 0;
			int nodes = 0;
			for (int node : model.getAllNodes()) {
				if (model.isActivity(node)) {
					result++;
				}
				nodes++;
			}
			numberOfParameters = result * 3;

			node2index = new int[nodes];
			int index = 0;
			for (int node : model.getAllNodes()) {
				if (model.isActivity(node)) {
					node2index[node] = index;
					index++;
				}
			}
		}
	}

	@Override
	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	@Override
	public double[] getInputs(int node, IvMMove initiate, IvMMove enqueue, IvMMove start, IvMMove complete) {
		double[] result = new double[numberOfParameters];

		if (!model.isActivity(node)) {
			return result;
		}

		if (complete.isLogMove()) {
			result[node2index[node] * 3] += 1;
		} else if (complete.isModelMove()) {
			result[node2index[node] * 3 + 1] += 1;
		} else {
			//sync
			result[node2index[node] * 3 + 2] += 1;
		}
		return result;
	}

	@Override
	public double getCost(IvMTrace trace) {
		if (!trace.hasAttributes()) {
			return Double.NaN;
		}

		if (!trace.getAttributes().containsKey(attribute)) {
			return Double.NaN;
		}

		XAttribute att = trace.getAttributes().get(attribute);
		double value = AttributeUtils.parseDoubleFast(att);
		if (value == -Double.MAX_VALUE) {
			return Double.NaN;
		}

		return value;
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

	public Pair<Long, String> getNodeRepresentationModel(int node) {
		return Pair.of( //
				(long) (parameters[node2index[node] + 2]), //
				format(parameters[node2index[node] + 2]));
	}

	public String[][] getNodeRepresentationPopup(int node) {
		return new String[][] { //
				{ "cost                   " + parameters[node2index[node] + 2] }, //
				{ "cost skip model        " + parameters[node2index[node] + 1] }, //
				{ "cost extra log event   " + parameters[node2index[node]] }, //
		};
	}
}