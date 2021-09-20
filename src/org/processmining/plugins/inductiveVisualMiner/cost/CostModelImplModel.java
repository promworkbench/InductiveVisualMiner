package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;

public class CostModelImplModel extends CostModelAbstract {

	private final int numberOfParameters;
	private final int[] node2index;

	public static enum ParameterNodeType {
		synchronousMove {
			public String toString() {
				return "step in model";
			}
		},
	}

	public CostModelImplModel(IvMModel model) {
		super(model);

		int index = 0;

		//count nodes in model
		int nodes = 0;
		{
			for (Iterator<Integer> it = model.getAllNodes().iterator(); it.hasNext();) {
				it.next();
				nodes++;
			}
		}

		node2index = new int[nodes];
		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				node2index[node] = index;
				index += ParameterNodeType.values().length;
			} else {
				node2index[node] = -1;
			}
		}

		numberOfParameters = index;
	}

	@Override
	public String getName() {
		return "model";
	}

	@Override
	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	@Override
	public double[] getInputs(IvMMove startTrace,
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove endTrace) {
		int node = instance.getA();
		IvMMove complete = instance.getF();

		double[] result = new double[numberOfParameters];

		if (node < 0 || !model.isActivity(node)) {
			return result;
		}

		if (complete.isModelSync()) {
			result[node2index[node] + ParameterNodeType.synchronousMove.ordinal()]++;
		}

		return result;
	}

	/**
	 * Times/durations will be in ms.
	 * 
	 * @param node
	 * @param parameterType
	 * @return
	 */
	public double getNodeParameter(int node, ParameterNodeType parameterType) {
		if (node2index[node] >= 0) {
			return parameters[node2index[node] + parameterType.ordinal()];
		} else {
			return 0;
		}
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

	@Override
	public Pair<Long, String> getNodeRepresentationModel(int node) {
		return Pair.of( //
				(long) (getNodeParameter(node, ParameterNodeType.synchronousMove)), //
				format(getNodeParameter(node, ParameterNodeType.synchronousMove)));
	}

	@Override
	public String[][] getNodeRepresentationPopup(int node) {
		List<String[]> result = new ArrayList<>();
		for (ParameterNodeType parameterType : ParameterNodeType.values()) {
			if (getNodeParameter(node, parameterType) != 0) {
				result.add(new String[] { "cost " + parameterType.toString(),
						format(getNodeParameter(node, parameterType)) });
			}
		}

		String[][] r = new String[result.size()][2];
		return result.toArray(r);
	}

	@Override
	public List<DataRow<Object>> getModelRepresentation() {
		List<DataRow<Object>> result = new ArrayList<>();

		result.addAll(super.getModelRepresentation());

		for (int node = 0; node < node2index.length; node++) {
			if (node2index[node] >= 0) {
				for (ParameterNodeType parameterType : ParameterNodeType.values()) {
					double value = getNodeParameter(node, parameterType);
					result.add(new DataRow<Object>(DisplayType.numeric(value), "cost of " + parameterType.toString(),
							model.getActivityName(node)));
				}
			}

		}

		return result;
	}

}
