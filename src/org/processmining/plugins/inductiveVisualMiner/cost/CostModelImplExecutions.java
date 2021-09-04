package org.processmining.plugins.inductiveVisualMiner.cost;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Parameter structure: [0,n-1] log move cost on per activity that has at least
 * one log move; [n,n+m*x] per model node a few timing things cost.
 * 
 * @author sander
 *
 */
public class CostModelImplExecutions extends CostModelAbstract {

	private final int numberOfParameters;
	private final int[] node2index;
	private final TObjectIntMap<String> logMove2index = new TObjectIntHashMap<>();

	public static final String attribute = "cost";

	public static enum ParameterNodeType {
		synchronousMove {
			public String toString() {
				return "step in log and model";
			}
		},
		modelMove {
			public String toString() {
				return "skip step in model";
			}
		}
	}

	public CostModelImplExecutions(IvMModel model, IvMLogInfo logInfoFiltered) {
		super(model);

		int index = 0;
		for (String logMoveActivity : logInfoFiltered.getUnlabeledLogMoves()) {
			logMove2index.put(logMoveActivity, index);
			index += 1;
		}

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
			}
		}

		numberOfParameters = index;
	}

	@Override
	public String getName() {
		return "executions";
	}

	@Override
	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	@Override
	public double[] getInputs(int node, IvMMove initiate, IvMMove enqueue, IvMMove start, IvMMove complete) {
		double[] result = new double[numberOfParameters];

		if (complete != null && complete.isLogMove()) {
			//result[node2index[node] * 2] += 1;
			String activity = complete.getActivityEventClass().toString();
			result[logMove2index.get(activity)]++;
			return result;
		}

		if (!model.isActivity(node)) {
			return result;
		}

		if (complete.isModelMove()) {
			//model move
			result[node2index[node] + ParameterNodeType.modelMove.ordinal()]++;
		} else {
			//sync
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
		return parameters[node2index[node] + parameterType.ordinal()];
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

		for (String activity : logMove2index.keySet()) {
			int index = logMove2index.get(activity);
			result.add(
					new DataRow<Object>(DisplayType.numeric(parameters[index]), "cost of extra step in log", activity));
		}

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