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
 * one log move; [n,n+m*2] per model node (0) move model cost and (1) move sync
 * cost.
 * 
 * @author sander
 *
 */
public class CostModelBasic extends CostModelAbstract {

	private final int numberOfParameters;
	private final int[] node2index;
	private final TObjectIntMap<String> logMove2index = new TObjectIntHashMap<>();

	public static final String attribute = "cost";

	public CostModelBasic(IvMModel model, IvMLogInfo logInfoFiltered) {
		super(model);

		int index = 0;
		for (String logMoveActivity : logInfoFiltered.getUnlabeledLogMoves()) {
			logMove2index.put(logMoveActivity, index);
			index++;
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
				index += 2;
			}
		}

		numberOfParameters = index;
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
			//result[node2index[node] * 2] += 1;
			String activity = complete.getActivityEventClass().toString();
			result[logMove2index.get(activity)]++;
		} else if (complete.isModelMove()) {
			result[node2index[node]]++;
		} else {
			//sync
			result[node2index[node] + 1]++;
		}
		return result;
	}

	public double getParameterSync(int node) {
		return parameters[node2index[node] + 1];
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
				(long) (getParameterSync(node)), //
				format(getParameterSync(node)));
	}

	@Override
	public String[][] getNodeRepresentationPopup(int node) {
		return new String[][] { //
				{ "cost                   " + parameters[node2index[node] + 1] }, //
				{ "cost to skip           " + parameters[node2index[node]] }, //
		};
	}

	@Override
	public List<DataRow<Object>> getModelRepresentation() {
		List<DataRow<Object>> result = new ArrayList<>();

		result.addAll(super.getModelRepresentation());

		for (String activity : logMove2index.keySet()) {
			int index = logMove2index.get(activity);
			result.add(new DataRow<Object>(DisplayType.numeric(parameters[index]), "extra step in log (log move)",
					activity));
		}

		for (int node = 0; node < node2index.length; node++) {
			if (node2index[node] >= 0) {
				result.add(new DataRow<Object>(DisplayType.numeric(parameters[node2index[node]]),
						"skip step in model (model move)", model.getActivityName(node)));
				result.add(new DataRow<Object>(DisplayType.numeric(getParameterSync(node)),
						"synchronous step in log and model", model.getActivityName(node)));
			}

		}
		
		result.add(new DataRow<Object>(DisplayType.literal("OLS multiple linear regression"),
				"cost model property", "type"));

		return result;
	}
}